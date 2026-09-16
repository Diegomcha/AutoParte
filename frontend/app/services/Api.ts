import * as Sentry from "@sentry/react-router";
import {
	MutationObserver,
	mutationOptions,
	QueryClient,
	queryOptions
} from "@tanstack/react-query";
import Cookies from "js-cookie";
import createFetchClient from "openapi-fetch";

import NotificationsService from "./NotificationsService";

import type { MutationOptions } from "@tanstack/react-query";
import type { FetchResponse } from "openapi-fetch";
import type { ErrorResponse } from "react-router";
import type {
	AccommodationDtoRequest,
	AccommodationDtoResponse,
	AddressDtoRequest,
	BookingDtoRequest,
	BookingDtoResponse,
	ConfigDtoRequest,
	EmployeeDtoCreate,
	EmployeeDtoPatch,
	EmployeeDtoResponse,
	operations,
	PageMetadata,
	paths,
	PersonDtoRequest,
	ProblemDetail
} from "../@types/api";

const api = createFetchClient<paths, "*/*">({
	querySerializer: {
		object: {
			style: "form",
			explode: true
		}
	},
	headers: {
		"X-Requested-With": "XMLHttpRequest",
		Accept: "application/json"
	},
	credentials: "include"
});

// Middleware to include CSRF token from cookies in the request headers
api.use({
	onRequest({ request }) {
		const csrfToken = Cookies.get("XSRF-TOKEN");
		if (csrfToken) request.headers.set("X-XSRF-TOKEN", csrfToken);
	}
});

const queryClient = new QueryClient({
	defaultOptions: {
		queries: {
			retry: (failureCount, error) => {
				if (error.status >= 400 && error.status < 500) return false; // Don't retry for client errors
				return failureCount < 3; // Retry up to 3 times for server errors
			},
			throwOnError: true // Throw errors for queries to be caught
		},
		mutations: {
			onError: (error) => {
				NotificationsService.error(`An error occurred: ${error.message}`); // TODO: localize this message
				Sentry.captureException(error);
			}
		}
	}
});

interface Sort {
	columnAccessor: string;
	direction?: "asc" | "desc";
}

interface RequiredPagedModel<T> {
	content: T[];
	page: Required<PageMetadata>;
}

const DEFAULT_PAGE_SIZE = 20;

function mapSorting(sorting: Sort[] | undefined): string[] | undefined {
	return sorting?.map((s) =>
		s.direction != null
			? `${s.columnAccessor},${s.direction}`
			: s.columnAccessor
	);
}

const queryFactory = {
	configuration: {
		get: () =>
			queryOptions({
				queryKey: ["configuration"],
				queryFn: async () => unwrapResponse(await api.GET("/api/config"))
			}),
		update: () =>
			mutationOptions({
				mutationFn: async (data: ConfigDtoRequest) =>
					unwrapResponse(
						await api.PUT("/api/config", {
							body: data
						})
					),
				onSuccess: async () => {
					await queryClient.invalidateQueries(queryFactory.configuration.get());
				}
			}),
		validateSesCreds: () =>
			mutationOptions({
				mutationFn: async () => {
					const res = await api.POST("/api/config/validate-ses");

					// Handle unauthorized error (401)
					if (res.response.status === 401) return false;

					unwrapResponse(res);

					return true;
				},
				onSuccess: async () => {
					await queryClient.invalidateQueries(queryFactory.configuration.get());
				}
			})
	},
	employees: {
		list: () =>
			queryOptions({
				queryKey: ["employees"],
				queryFn: async () =>
					unwrapResponse(
						await api.GET("/api/employees", {
							params: { query: { page: 0, size: 0 } }
						})
					).content ?? []
			}),
		pagedList: ({
			page,
			size,
			sorting
		}: {
			page?: number;
			size?: number;
			sorting?: Sort[];
		} = {}) =>
			queryOptions({
				queryKey: ["employees", { page, size, sorting }],
				queryFn: async () =>
					unwrapResponse(
						await api.GET("/api/employees", {
							params: {
								query: {
									page,
									size,
									sort: mapSorting(sorting)
								}
							}
						})
					) as RequiredPagedModel<EmployeeDtoResponse>
			}),
		detail: (employeeId: string) =>
			queryOptions({
				queryKey: ["employees", employeeId],
				queryFn: async () =>
					unwrapResponse(
						await api.GET("/api/employees/{id}", {
							params: { path: { id: employeeId } }
						})
					)
			}),
		update: (employeeId: string) =>
			mutationOptions({
				mutationFn: async (values: EmployeeDtoPatch) => {
					const response = await api.PATCH("/api/employees/{id}", {
						params: { path: { id: employeeId } },
						body: values
					});

					// Handle email conflict error (409)
					if (!response.response.ok && response.response.status === 409)
						return false;

					unwrapResponse(response);

					return true;
				},
				onSuccess: async (success) => {
					if (success)
						await queryClient.invalidateQueries(queryFactory.employees.list());
				}
			}),
		create: () =>
			mutationOptions({
				mutationFn: async (employee: EmployeeDtoCreate) => {
					const response = await api.POST("/api/employees", {
						body: employee
					});

					// Handle email conflict error (409)
					if (!response.response.ok && response.response.status === 409)
						return false;

					return unwrapResponse(response);
				},
				onSuccess: async (createdEmployee) => {
					if (createdEmployee)
						await queryClient.invalidateQueries(queryFactory.employees.list());
				}
			}),
		delete: (employeeId: string) =>
			mutationOptions({
				mutationFn: async () =>
					unwrapResponse(
						await api.DELETE("/api/employees/{id}", {
							params: { path: { id: employeeId } }
						})
					),
				onSuccess: async () => {
					queryClient.removeQueries(queryFactory.employees.detail(employeeId));
					await queryClient.invalidateQueries(queryFactory.employees.list());
				}
			}),
		deleteMultiple: () =>
			mutationOptions({
				mutationFn: async (employeeIds: string[]) =>
					await Promise.all(
						employeeIds.map(async (employeeId) =>
							unwrapResponse(
								await api.DELETE("/api/employees/{id}", {
									params: { path: { id: employeeId } }
								})
							)
						)
					),
				onSuccess: async (_, employeeIds) => {
					employeeIds.forEach((employeeId) => {
						queryClient.removeQueries(
							queryFactory.employees.detail(employeeId)
						);
					});
					await queryClient.invalidateQueries(queryFactory.employees.list());
				}
			}),
		resetPassword: (employeeId: string) =>
			mutationOptions({
				mutationFn: async () =>
					unwrapResponse(
						await api.POST("/api/employees/{id}/reset-password", {
							params: { path: { id: employeeId } }
						})
					)
			}),
		accommodations: {
			link: (employeeId: string) =>
				mutationOptions({
					mutationFn: async (accommodationId: string) =>
						unwrapResponse(
							await api.POST(
								"/api/accommodations/{accommodationId}/employees/{employeeId}",
								{
									params: { path: { accommodationId, employeeId } }
								}
							)
						),
					onSuccess: async () => {
						await Promise.all([
							queryClient.invalidateQueries(queryFactory.accommodations.list()),
							queryClient.invalidateQueries(queryFactory.employees.list())
						]);
					}
				}),
			linkMultiple: (employeeId: string) =>
				mutationOptions({
					mutationFn: async (accommodationIds: string[]) => {
						await Promise.all(
							accommodationIds.map(async (accommodationId) =>
								unwrapResponse(
									await api.POST(
										"/api/accommodations/{accommodationId}/employees/{employeeId}",
										{
											params: { path: { accommodationId, employeeId } }
										}
									)
								)
							)
						);
					},
					onSuccess: async () => {
						await Promise.all([
							queryClient.invalidateQueries(queryFactory.accommodations.list()),
							queryClient.invalidateQueries(queryFactory.employees.list())
						]);
					}
				}),
			unlink: (employeeId: string) =>
				mutationOptions({
					mutationFn: async (accommodationId: string) =>
						unwrapResponse(
							await api.DELETE(
								"/api/accommodations/{accommodationId}/employees/{employeeId}",
								{
									params: { path: { accommodationId, employeeId } }
								}
							)
						),
					onSuccess: async () => {
						await Promise.all([
							queryClient.invalidateQueries(queryFactory.accommodations.list()),
							queryClient.invalidateQueries(queryFactory.employees.list())
						]);
					}
				}),
			unlinkMultiple: (employeeId: string) =>
				mutationOptions({
					mutationFn: async (accommodationIds: string[]) => {
						await Promise.all(
							accommodationIds.map(async (accommodationId) =>
								unwrapResponse(
									await api.DELETE(
										"/api/accommodations/{accommodationId}/employees/{employeeId}",
										{
											params: { path: { accommodationId, employeeId } }
										}
									)
								)
							)
						);
					},
					onSuccess: async () => {
						await Promise.all([
							queryClient.invalidateQueries(queryFactory.accommodations.list()),
							queryClient.invalidateQueries(queryFactory.employees.list())
						]);
					}
				})
		}
	},
	accommodations: {
		list: () =>
			queryOptions({
				queryKey: ["accommodations"],
				queryFn: async () =>
					unwrapResponse(
						await api.GET("/api/accommodations", {
							params: { query: { page: 0, size: 0 } }
						})
					).content ?? []
			}),
		orderedList: () =>
			queryOptions({
				queryKey: ["accommodations", { ordered: true }],
				queryFn: async () =>
					unwrapResponse(
						await api.GET("/api/accommodations", {
							params: { query: { page: 0, size: 0, sort: ["id,asc"] } }
						})
					).content ?? []
			}),
		pagedList: ({
			page,
			size,
			sorting
		}: {
			page?: number;
			size?: number;
			sorting?: Sort[];
		} = {}) =>
			queryOptions({
				queryKey: ["accommodations", { page, size, sorting }],
				queryFn: async () =>
					unwrapResponse(
						await api.GET("/api/accommodations", {
							params: {
								query: {
									page,
									size,
									sort: mapSorting(sorting)
								}
							}
						})
					) as RequiredPagedModel<AccommodationDtoResponse>
			}),
		detail: (accommodationId: string) =>
			queryOptions({
				queryKey: ["accommodations", accommodationId],
				queryFn: async () =>
					unwrapResponse(
						await api.GET("/api/accommodations/{id}", {
							params: { path: { id: accommodationId } }
						})
					)
			}),
		create: () =>
			mutationOptions({
				mutationFn: async (accommodation: AccommodationDtoRequest) => {
					const response = await api.POST("/api/accommodations", {
						body: accommodation
					});

					// Handle conflicts error (409)
					if (!response.response.ok && response.response.status === 409) {
						const problem = response.error as ProblemDetail;

						if (problem.detail?.includes("name"))
							return [false, "NAME_IN_USE"] as const;
						else return [false, "SES_CODE_IN_USE"] as const;
					}

					return [unwrapResponse(response), null] as const;
				},
				onSuccess: async ([createdEmployee]) => {
					if (createdEmployee)
						await queryClient.invalidateQueries(
							queryFactory.accommodations.list()
						);
				}
			}),
		update: (accommodationId: string) =>
			mutationOptions({
				mutationFn: async (accommodation: AccommodationDtoRequest) => {
					const response = await api.PUT("/api/accommodations/{id}", {
						params: { path: { id: accommodationId } },
						body: accommodation
					});

					// Handle conflicts error (409)
					if (!response.response.ok && response.response.status === 409) {
						const problem = response.error as ProblemDetail;

						if (problem.detail?.includes("name"))
							return [false, "NAME_IN_USE"] as const;
						else return [false, "SES_CODE_IN_USE"] as const;
					}

					unwrapResponse(response);

					return [true, null] as const;
				},
				onSuccess: async ([updatedEmployee]) => {
					if (updatedEmployee)
						await queryClient.invalidateQueries(
							queryFactory.accommodations.list()
						);
				}
			}),
		delete: (accommodationId: string) =>
			mutationOptions({
				mutationFn: async () =>
					unwrapResponse(
						await api.DELETE("/api/accommodations/{id}", {
							params: { path: { id: accommodationId } }
						})
					),
				onSuccess: async () => {
					queryClient.removeQueries(
						queryFactory.accommodations.detail(accommodationId)
					);
					await queryClient.invalidateQueries(
						queryFactory.accommodations.list()
					);
				}
			}),
		deleteMultiple: () =>
			mutationOptions({
				mutationFn: async (accommodationIds: string[]) =>
					await Promise.all(
						accommodationIds.map(async (accommodationId) =>
							unwrapResponse(
								await api.DELETE("/api/accommodations/{id}", {
									params: { path: { id: accommodationId } }
								})
							)
						)
					),
				onSuccess: async (_, accommodationIds) => {
					accommodationIds.forEach((accommodationId) => {
						queryClient.removeQueries(
							queryFactory.accommodations.detail(accommodationId)
						);
					});
					await queryClient.invalidateQueries(
						queryFactory.accommodations.list()
					);
				}
			}),
		employees: {
			link: (accommodationId: string) =>
				mutationOptions({
					mutationFn: async (employeeId: string) =>
						unwrapResponse(
							await api.POST(
								"/api/accommodations/{accommodationId}/employees/{employeeId}",
								{
									params: { path: { accommodationId, employeeId } }
								}
							)
						),
					onSuccess: async () => {
						await Promise.all([
							queryClient.invalidateQueries(queryFactory.accommodations.list()),
							queryClient.invalidateQueries(queryFactory.employees.list())
						]);
					}
				}),
			linkMultiple: (accommodationId: string) =>
				mutationOptions({
					mutationFn: async (employeeIds: string[]) => {
						await Promise.all(
							employeeIds.map(async (employeeId) =>
								unwrapResponse(
									await api.POST(
										"/api/accommodations/{accommodationId}/employees/{employeeId}",
										{
											params: { path: { accommodationId, employeeId } }
										}
									)
								)
							)
						);
					},
					onSuccess: async () => {
						await Promise.all([
							queryClient.invalidateQueries(queryFactory.accommodations.list()),
							queryClient.invalidateQueries(queryFactory.employees.list())
						]);
					}
				}),
			unlink: (accommodationId: string) =>
				mutationOptions({
					mutationFn: async (employeeId: string) =>
						unwrapResponse(
							await api.DELETE(
								"/api/accommodations/{accommodationId}/employees/{employeeId}",
								{
									params: { path: { accommodationId, employeeId } }
								}
							)
						),
					onSuccess: async () => {
						await Promise.all([
							queryClient.invalidateQueries(queryFactory.accommodations.list()),
							queryClient.invalidateQueries(queryFactory.employees.list())
						]);
					}
				}),
			unlinkMultiple: (accommodationId: string) =>
				mutationOptions({
					mutationFn: async (employeeIds: string[]) => {
						await Promise.all(
							employeeIds.map(async (employeeId) =>
								unwrapResponse(
									await api.DELETE(
										"/api/accommodations/{accommodationId}/employees/{employeeId}",
										{
											params: { path: { accommodationId, employeeId } }
										}
									)
								)
							)
						);
					},
					onSuccess: async () => {
						await Promise.all([
							queryClient.invalidateQueries(queryFactory.accommodations.list()),
							queryClient.invalidateQueries(queryFactory.employees.list())
						]);
					}
				})
		},
		bookings: {
			list: (accommodationId: string) =>
				queryOptions({
					queryKey: [
						...queryFactory.accommodations.detail(accommodationId).queryKey,
						"bookings"
					],
					queryFn: async () =>
						unwrapResponse(
							await api.GET("/api/accommodations/{accommodationId}/bookings", {
								params: {
									path: { accommodationId },
									query: { page: 0, size: 0 }
								}
							})
						).content ?? []
				}),
			detail: (accommodationId: string, bookingId: string) =>
				queryOptions({
					queryKey: [
						...queryFactory.accommodations.detail(accommodationId).queryKey,
						"bookings",
						bookingId
					],
					queryFn: async () =>
						unwrapResponse(
							await api.GET(
								"/api/accommodations/{accommodationId}/bookings/{id}",
								{
									params: { path: { accommodationId, id: bookingId } }
								}
							)
						)
				}),
			create: () =>
				mutationOptions({
					mutationFn: async ({
						accommodationId,
						startTime,
						endTime
					}: {
						accommodationId: string;
						startTime: Date;
						endTime: Date;
					}) =>
						unwrapResponse(
							await api.POST("/api/accommodations/{accommodationId}/bookings", {
								params: { path: { accommodationId } },
								body: {
									startTime: startTime.toISOString(),
									endTime: endTime.toISOString(),
									numberOfPeople: 1
								}
							})
						),
					onSuccess: async (_, { accommodationId }) => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					}
				}),
			update: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async (values: BookingDtoRequest) => {
						const response = await api.PUT(
							"/api/accommodations/{accommodationId}/bookings/{id}",
							{
								params: {
									path: { accommodationId, id: bookingId }
								},
								body: values
							}
						);

						// Handle more people info. than slots (409)
						if (!response.response.ok && response.response.status === 409)
							return false;

						unwrapResponse(response);
						return true;
					},
					onSuccess: async (success) => {
						if (success)
							await queryClient.invalidateQueries(
								queryFactory.accommodations.bookings.list(accommodationId)
							);
					}
				}),
			updateRange: () =>
				mutationOptions({
					mutationFn: async ({
						accommodationId,
						booking,
						newStart,
						newEnd
					}: {
						accommodationId: string;
						booking: BookingDtoResponse;
						newStart: Date;
						newEnd: Date;
					}) =>
						unwrapResponse(
							await api.PUT(
								"/api/accommodations/{accommodationId}/bookings/{id}",
								{
									params: { path: { accommodationId, id: booking.id } },
									body: {
										...booking,
										startTime: newStart.toISOString(),
										endTime: newEnd.toISOString()
									}
								}
							)
						),
					onSuccess: async (_, { accommodationId }) => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					}
				}),
			delete: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async () =>
						unwrapResponse(
							await api.DELETE(
								"/api/accommodations/{accommodationId}/bookings/{id}",
								{
									params: { path: { accommodationId, id: bookingId } }
								}
							)
						),
					onSuccess: async () => {
						queryClient.removeQueries(
							queryFactory.accommodations.bookings.detail(
								accommodationId,
								bookingId
							)
						);
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					}
				}),
			confirm: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async () =>
						unwrapResponse(
							await api.POST(
								"/api/accommodations/{accommodationId}/bookings/{id}/confirm",
								{
									params: { path: { accommodationId, id: bookingId } }
								}
							)
						),
					onSuccess: async () => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					}
				}),
			checkIn: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async () =>
						unwrapResponse(
							await api.POST(
								"/api/accommodations/{accommodationId}/bookings/{id}/check-in",
								{
									params: { path: { accommodationId, id: bookingId } }
								}
							)
						),
					onSuccess: async () => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					}
				}),
			cancel: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async () =>
						unwrapResponse(
							await api.POST(
								"/api/accommodations/{accommodationId}/bookings/{id}/cancel",
								{
									params: { path: { accommodationId, id: bookingId } }
								}
							)
						),
					onSuccess: async () => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					}
				}),
			requestSelfCheckIn: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async () =>
						unwrapResponse(
							await api.POST(
								"/api/accommodations/{accommodationId}/bookings/{id}/self-check-in",
								{
									params: { path: { accommodationId, id: bookingId } }
								}
							)
						),
					onSuccess: async () => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					}
				}),
			cancelSelfCheckIn: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async () =>
						unwrapResponse(
							await api.DELETE(
								"/api/accommodations/{accommodationId}/bookings/{id}/self-check-in",
								{
									params: { path: { accommodationId, id: bookingId } }
								}
							)
						),
					onSuccess: async () => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					}
				}),
			people: {
				list: (accommodationId: string, bookingId: string) =>
					queryOptions({
						queryKey: [
							...queryFactory.accommodations.bookings.detail(
								accommodationId,
								bookingId
							).queryKey,
							"people"
						],
						queryFn: async () =>
							unwrapResponse(
								await api.GET(
									"/api/accommodations/{accommodationId}/bookings/{bookingId}/people",
									{
										params: { path: { accommodationId, bookingId } }
									}
								)
							)
					}),
				detail: (
					accommodationId: string,
					bookingId: string,
					personId: string
				) =>
					queryOptions({
						queryKey: [
							...queryFactory.accommodations.bookings.detail(
								accommodationId,
								bookingId
							).queryKey,
							"people",
							personId
						],
						queryFn: async () =>
							unwrapResponse(
								await api.GET(
									"/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}",
									{
										params: {
											path: { accommodationId, bookingId, id: personId }
										}
									}
								)
							)
					}),
				create: (accommodationId: string, bookingId: string) =>
					mutationOptions({
						mutationFn: async (values: PersonDtoRequest) =>
							unwrapResponse(
								await api.POST(
									"/api/accommodations/{accommodationId}/bookings/{bookingId}/people",
									{
										params: {
											path: {
												accommodationId,
												bookingId
											}
										},
										body: values
									}
								)
							),
						onSuccess: async () => {
							await queryClient.invalidateQueries(
								queryFactory.accommodations.bookings.detail(
									accommodationId,
									bookingId
								)
							);
						}
					}),
				update: (
					accommodationId: string,
					bookingId: string,
					personId: string
				) =>
					mutationOptions({
						mutationFn: async (values: PersonDtoRequest) =>
							unwrapResponse(
								await api.PUT(
									"/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}",
									{
										params: {
											path: {
												accommodationId,
												bookingId,
												id: personId
											}
										},
										body: values
									}
								)
							),
						onSuccess: async () => {
							await queryClient.invalidateQueries(
								queryFactory.accommodations.bookings.detail(
									accommodationId,
									bookingId
								)
							);
						}
					}),
				delete: (
					accommodationId: string,
					bookingId: string,
					personId: string
				) =>
					mutationOptions({
						mutationFn: async () =>
							unwrapResponse(
								await api.DELETE(
									"/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}",
									{
										params: {
											path: {
												accommodationId,
												bookingId,
												id: personId
											}
										}
									}
								)
							),
						onSuccess: async () => {
							queryClient.removeQueries(
								queryFactory.accommodations.bookings.people.detail(
									accommodationId,
									bookingId,
									personId
								)
							);
							await queryClient.invalidateQueries(
								queryFactory.accommodations.bookings.detail(
									accommodationId,
									bookingId
								)
							);
						}
					}),
				getSignature: (
					accommodationId: string,
					bookingId: string,
					personId: string
				) =>
					queryOptions({
						queryKey: [
							...queryFactory.accommodations.bookings.people.detail(
								accommodationId,
								bookingId,
								personId
							).queryKey,
							"signature"
						],
						queryFn: async () => {
							const res = await api.GET(
								"/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}/signature",
								{
									params: {
										path: {
											accommodationId,
											bookingId,
											id: personId
										}
									}
								}
							);

							// Handle 404 Not Found response (signature not found)
							if (
								res.response.status === 404 &&
								(res.error as ProblemDetail).detail?.includes("Signature")
							)
								return null;

							return unwrapResponse(res);
						}
					}),

				sign: (accommodationId: string, bookingId: string, personId: string) =>
					mutationOptions({
						mutationFn: async (
							signaturePaths: operations["addSignature"]["requestBody"]["content"]["application/json"]
						) =>
							await Promise.all([
								api.POST(
									"/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}/signature",
									{
										params: {
											path: {
												accommodationId,
												bookingId,
												id: personId
											}
										},
										body: signaturePaths
									}
								)
							]),
						onSuccess: async () => {
							await Promise.all([
								queryClient.invalidateQueries(
									queryFactory.accommodations.bookings.people.list(
										accommodationId,
										bookingId
									)
								),
								queryClient.invalidateQueries(
									queryFactory.accommodations.bookings.people.getSignature(
										accommodationId,
										bookingId,
										personId
									)
								)
							]);
						}
					})
			},
			addresses: {
				list: (accommodationId: string, bookingId: string) =>
					queryOptions({
						queryKey: [
							...queryFactory.accommodations.bookings.detail(
								accommodationId,
								bookingId
							).queryKey,
							"addresses"
						],
						queryFn: async () =>
							unwrapResponse(
								await api.GET(
									"/api/accommodations/{accommodationId}/bookings/{bookingId}/addresses",
									{
										params: { path: { accommodationId, bookingId } }
									}
								)
							)
					})
			}
		}
	},
	addresses: {
		detail: (addressId: string) =>
			queryOptions({
				queryKey: ["addresses", addressId],
				queryFn: async () =>
					unwrapResponse(
						await api.GET("/api/addresses/{id}", {
							params: { path: { id: addressId } }
						})
					)
			}),
		create: () =>
			mutationOptions({
				mutationFn: async (address: AddressDtoRequest) =>
					unwrapResponse(
						await api.POST("/api/addresses", {
							body: address
						})
					)
			})
	},
	catalogue: {
		countries: {
			list: () =>
				queryOptions({
					staleTime: Infinity,
					queryKey: ["catalogue", "countries"],
					queryFn: async () =>
						unwrapResponse(await api.GET("/api/catalogue/countries"))
				}),
			spanishProvinces: {
				list: () =>
					queryOptions({
						staleTime: Infinity,
						queryKey: [
							...queryFactory.catalogue.countries.list().queryKey,
							"ESP",
							"provinces"
						],
						queryFn: async () =>
							unwrapResponse(
								await api.GET(`/api/catalogue/countries/ESP/provinces`)
							)
					}),
				municipalities: {
					list: (provinceCode: string) =>
						queryOptions({
							staleTime: Infinity,
							queryKey: [
								...queryFactory.catalogue.countries.spanishProvinces.list()
									.queryKey,
								provinceCode,
								"municipalities"
							],
							queryFn: async () =>
								unwrapResponse(
									await api.GET(
										`/api/catalogue/countries/ESP/provinces/{provinceCode}/municipalities`,
										{ params: { path: { provinceCode } } }
									)
								)
						}),
					postalCodes: {
						list: (provinceCode: string, municipalityCode: string) =>
							queryOptions({
								staleTime: Infinity,
								queryKey: [
									...queryFactory.catalogue.countries.spanishProvinces.municipalities.list(
										provinceCode
									).queryKey,
									municipalityCode,
									"postal-codes"
								],
								queryFn: async () =>
									unwrapResponse(
										await api.GET(
											"/api/catalogue/countries/ESP/provinces/{provinceCode}/municipalities/{municipalityCode}/postal-codes",
											{
												params: {
													// @ts-expect-error: The generated types for the API are incorrect, as they don't include the municipalityCode in the path parameters.
													path: {
														provinceCode,
														municipalityCode
													}
												}
											}
										)
									)
							})
					}
				}
			}
		},
		genders: () =>
			queryOptions({
				staleTime: Infinity,
				queryKey: ["catalogue", "genders"],
				queryFn: async () =>
					unwrapResponse(await api.GET("/api/catalogue/person/genders"))
			}),
		relationships: () =>
			queryOptions({
				staleTime: Infinity,
				queryKey: ["catalogue", "relationships"],
				queryFn: async () =>
					unwrapResponse(await api.GET("/api/catalogue/person/relationships"))
			}),
		documentTypes: () =>
			queryOptions({
				staleTime: Infinity,
				queryKey: ["catalogue", "documentTypes"],
				queryFn: async () =>
					unwrapResponse(await api.GET("/api/catalogue/document/types"))
			})
	},
	auth: {
		me: () =>
			queryOptions({
				staleTime: Infinity,
				queryKey: ["auth", "me"],
				queryFn: async () => {
					const res = await api.GET("/api/auth/me");

					// Handle 401 Unauthorized response (user not logged in)
					if (res.response.status === 401) return null;

					return unwrapResponse(res);
				}
			}),
		login: () =>
			mutationOptions({
				mutationFn: async (credentials: {
					username: string;
					password: string;
					rememberMe?: boolean;
				}) => {
					const req = await api.POST("/api/auth/login", {
						body: credentials,
						headers: {
							"Content-Type": "application/x-www-form-urlencoded"
						}
					});

					// Handle 401 Unauthorized response (invalid credentials)
					if (req.response.status === 401) return false;
					// Handle other non-OK responses
					unwrapResponse(req);

					return true;
				},
				onSuccess: async () => {
					await queryClient.invalidateQueries(queryFactory.auth.me());
				}
			}),
		logout: () =>
			mutationOptions({
				mutationFn: async () => {
					const req = await api.POST("/api/auth/logout");
					// Handle 401 Unauthorized response (user not logged in)
					if (req.response.status === 401) return false;
					// Handle other non-OK responses
					unwrapResponse(req);

					return true;
				},
				onSuccess: async () => {
					await queryClient.invalidateQueries(queryFactory.auth.me());
				}
			})
	},
	ocr: {
		mrz: () =>
			mutationOptions({
				mutationFn: async (file: Blob) => {
					const res = await api.POST("/api/ocr/mrz", {
						body: {
							file: file as unknown as string // TODO: Improve api transformers to handle the files
						},
						bodySerializer: () => {
							const fd = new FormData();
							fd.append("file", file);
							return fd;
						}
					});

					if (res.response.status === 422) return false; // Handle invalid scan

					return unwrapResponse(res);
				}
			})
	}
};

// * Utility functions

/**
 * Unwraps the response from the API and handles errors.
 * Handles API responses by throwing an ApiErrorResponse for non-OK responses, or returning the response data for OK responses.
 * @template T The type of the response data.
 * @param param0 The FetchResponse object containing the response data, error, and response.
 * @returns The response data if the response is OK.
 * @throws ApiErrorResponse if the response is not OK.
 */
function unwrapResponse<T extends Record<string | number, unknown>>({
	data: resData,
	error,
	response
}: FetchResponse<T, unknown, "*/*">) {
	// Handle API errors in a standardized way
	if (!response.ok) throw new ApiErrorResponse(response, error);

	// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
	return resData!;
}

/**
 * Executes a mutation imperatively using QueryClient.
 * @template TData The type of the mutation result data.
 * @template TVariables The type of the mutation variables.
 * @param options The mutation options, including the mutation function and any callbacks.
 * @param variables The variables to pass to the mutation function.
 * @returns A promise that resolves to the mutation result data.
 * @throws ApiErrorResponse if the mutation fails with a non-OK response.
 */
async function executeMutation<TData, TVariables>(
	options: MutationOptions<TData, ApiErrorResponse, TVariables>,
	variables: TVariables
): Promise<TData> {
	const observer = new MutationObserver(queryClient, options);
	return observer.mutate(variables);
}

// * Custom error class for API errors.

class ApiErrorResponse extends Error implements ErrorResponse {
	/**
	 * Whether the given error is an instance of ApiErrorResponse.
	 * @param error The object to check.
	 * @returns  True if the error is an instance of ApiErrorResponse, false otherwise.
	 */
	static isApiErrorResponse(error: unknown): error is ApiErrorResponse {
		return error instanceof ApiErrorResponse;
	}

	readonly status: number;
	readonly statusText: string;
	readonly data: ProblemDetail | undefined;

	constructor(response: Response, error?: ProblemDetail) {
		super(
			`ApiErrorResponse: [${String(response.status)} ${response.statusText}] ${error?.title ?? ""}`
		);

		this.status = response.status;
		this.statusText = response.statusText;
		this.data = error;
	}
}

export {
	// Should not be used directly unless custom query/mutation logic is needed. Use queryFactory and executeMutation instead.
	api as _api,
	unwrapResponse as _unwrapResponse,
	ApiErrorResponse,
	DEFAULT_PAGE_SIZE,
	executeMutation,
	queryClient,
	queryFactory
};
