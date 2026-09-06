import {
	MutationObserver,
	mutationOptions,
	QueryClient,
	queryOptions,
} from '@tanstack/react-query';
import createFetchClient from 'openapi-fetch';
import type {
	AddressDtoRequest,
	BookingDtoRequest,
	BookingDtoResponse,
	paths,
	PersonDtoRequest,
	ProblemDetail,
} from '../@types/api';
import type { MutationOptions } from '@tanstack/react-query';
import type { FetchResponse } from 'openapi-fetch';
import type { ErrorResponse } from 'react-router';

const api = createFetchClient<paths, '*/*'>({
	querySerializer: {
		object: {
			style: 'form',
			explode: true,
		},
	},
	headers: {
		'X-Requested-With': 'XMLHttpRequest',
		Accept: 'application/json',
	},
	credentials: 'include',
});

// Middleware to include CSRF token from cookies in the request headers
api.use({
	async onRequest({ request }) {
		const csrfToken = await cookieStore.get('XSRF-TOKEN');
		if (csrfToken?.value) request.headers.set('X-XSRF-TOKEN', csrfToken.value);
	},
});

const queryClient = new QueryClient({
	defaultOptions: {
		queries: {
			retry: (failureCount, error) => {
				if (error.status >= 400 && error.status < 500) return false; // Don't retry for client errors
				return failureCount < 3; // Retry up to 3 times for server errors
			},
			throwOnError: true, // Throw errors for queries to be caught
		},
	},
});

const queryFactory = {
	accommodations: {
		list: () =>
			queryOptions({
				queryKey: ['accommodations'],
				queryFn: async () =>
					unwrapResponse(
						await api.GET('/api/accommodations', {
							params: { query: { page: 0, size: 0 } },
						})
					).content ?? [],
			}),
		detail: (accommodationId: string) =>
			queryOptions({
				queryKey: ['accommodations', accommodationId],
				queryFn: async () =>
					unwrapResponse(
						await api.GET('/api/accommodations/{id}', {
							params: { path: { id: accommodationId } },
						})
					),
			}),
		bookings: {
			list: (accommodationId: string) =>
				queryOptions({
					queryKey: [
						...queryFactory.accommodations.detail(accommodationId).queryKey,
						'bookings',
					],
					queryFn: async () =>
						unwrapResponse(
							await api.GET('/api/accommodations/{accommodationId}/bookings', {
								params: {
									path: { accommodationId },
									query: { page: 0, size: 0 },
								},
							})
						).content ?? [],
				}),
			detail: (accommodationId: string, bookingId: string) =>
				queryOptions({
					queryKey: [
						...queryFactory.accommodations.detail(accommodationId).queryKey,
						'bookings',
						bookingId,
					],
					queryFn: async () =>
						unwrapResponse(
							await api.GET(
								'/api/accommodations/{accommodationId}/bookings/{id}',
								{
									params: { path: { accommodationId, id: bookingId } },
								}
							)
						),
				}),
			create: () =>
				mutationOptions({
					mutationFn: async ({
						accommodationId,
						startTime,
						endTime,
					}: {
						accommodationId: string;
						startTime: Date;
						endTime: Date;
					}) => {
						unwrapResponse(
							await api.POST('/api/accommodations/{accommodationId}/bookings', {
								params: { path: { accommodationId } },
								body: {
									startTime: startTime.toISOString(),
									endTime: endTime.toISOString(),
									numberOfPeople: 1,
								},
							})
						);
						return accommodationId;
					},
					onSuccess: async (accommodationId) => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					},
				}),
			update: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async (values: BookingDtoRequest) => {
						const response = await api.PUT(
							'/api/accommodations/{accommodationId}/bookings/{id}',
							{
								params: {
									path: { accommodationId, id: bookingId },
								},
								body: values,
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
					},
				}),
			updateRange: () =>
				mutationOptions({
					mutationFn: async ({
						accommodationId,
						booking,
						newStart,
						newEnd,
					}: {
						accommodationId: string;
						booking: BookingDtoResponse;
						newStart: Date;
						newEnd: Date;
					}) => {
						unwrapResponse(
							await api.PUT(
								'/api/accommodations/{accommodationId}/bookings/{id}',
								{
									params: { path: { accommodationId, id: booking.id } },
									body: {
										...booking,
										startTime: newStart.toISOString(),
										endTime: newEnd.toISOString(),
									},
								}
							)
						);
						return accommodationId;
					},
					onSuccess: async (accommodationId) => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					},
				}),
			delete: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async () =>
						unwrapResponse(
							await api.DELETE(
								'/api/accommodations/{accommodationId}/bookings/{id}',
								{
									params: { path: { accommodationId, id: bookingId } },
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
					},
				}),
			confirm: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async () =>
						unwrapResponse(
							await api.POST(
								'/api/accommodations/{accommodationId}/bookings/{id}/confirm',
								{
									params: { path: { accommodationId, id: bookingId } },
								}
							)
						),
					onSuccess: async () => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					},
				}),
			checkIn: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async () =>
						unwrapResponse(
							await api.POST(
								'/api/accommodations/{accommodationId}/bookings/{id}/check-in',
								{
									params: { path: { accommodationId, id: bookingId } },
								}
							)
						),
					onSuccess: async () => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					},
				}),
			cancel: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async () =>
						unwrapResponse(
							await api.POST(
								'/api/accommodations/{accommodationId}/bookings/{id}/cancel',
								{
									params: { path: { accommodationId, id: bookingId } },
								}
							)
						),
					onSuccess: async () => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					},
				}),
			requestSelfCheckIn: (accommodationId: string, bookingId: string) =>
				mutationOptions({
					mutationFn: async () =>
						unwrapResponse(
							await api.POST(
								'/api/accommodations/{accommodationId}/bookings/{id}/request-self-check-in',
								{
									params: { path: { accommodationId, id: bookingId } },
								}
							)
						),
					onSuccess: async () => {
						await queryClient.invalidateQueries(
							queryFactory.accommodations.bookings.list(accommodationId)
						);
					},
				}),
			people: {
				list: (accommodationId: string, bookingId: string) =>
					queryOptions({
						queryKey: [
							...queryFactory.accommodations.bookings.detail(
								accommodationId,
								bookingId
							).queryKey,
							'people',
						],
						queryFn: async () =>
							unwrapResponse(
								await api.GET(
									'/api/accommodations/{accommodationId}/bookings/{bookingId}/people',
									{
										params: { path: { accommodationId, bookingId } },
									}
								)
							),
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
							'people',
							personId,
						],
						queryFn: async () =>
							unwrapResponse(
								await api.GET(
									'/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}',
									{
										params: {
											path: { accommodationId, bookingId, id: personId },
										},
									}
								)
							),
					}),
				create: (accommodationId: string, bookingId: string) =>
					mutationOptions({
						mutationFn: async (values: PersonDtoRequest) =>
							unwrapResponse(
								await api.POST(
									'/api/accommodations/{accommodationId}/bookings/{bookingId}/people',
									{
										params: {
											path: {
												accommodationId,
												bookingId,
											},
										},
										body: values,
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
						},
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
									'/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}',
									{
										params: {
											path: {
												accommodationId,
												bookingId,
												id: personId,
											},
										},
										body: values,
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
						},
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
									'/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}',
									{
										params: {
											path: {
												accommodationId,
												bookingId,
												id: personId,
											},
										},
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
						},
					}),
			},
			addresses: {
				list: (accommodationId: string, bookingId: string) =>
					queryOptions({
						queryKey: [
							...queryFactory.accommodations.bookings.detail(
								accommodationId,
								bookingId
							).queryKey,
							'addresses',
						],
						queryFn: async () =>
							unwrapResponse(
								await api.GET(
									'/api/accommodations/{accommodationId}/bookings/{bookingId}/addresses',
									{
										params: { path: { accommodationId, bookingId } },
									}
								)
							),
					}),
			},
		},
	},
	addresses: {
		detail: (addressId: string) =>
			queryOptions({
				queryKey: ['addresses', addressId],
				queryFn: async () =>
					unwrapResponse(
						await api.GET('/api/addresses/{id}', {
							params: { path: { id: addressId } },
						})
					),
			}),
		create: () =>
			mutationOptions({
				mutationFn: async (address: AddressDtoRequest) =>
					unwrapResponse(
						await api.POST('/api/addresses', {
							body: address,
						})
					),
			}),
	},
	catalogue: {
		countries: {
			list: () =>
				queryOptions({
					queryKey: ['catalogue', 'countries'],
					queryFn: async () =>
						unwrapResponse(await api.GET('/api/catalogue/countries')),
				}),
			spanishProvinces: {
				list: () =>
					queryOptions({
						queryKey: [
							...queryFactory.catalogue.countries.list().queryKey,
							'ESP',
							'provinces',
						],
						queryFn: async () =>
							unwrapResponse(
								await api.GET(`/api/catalogue/countries/ESP/provinces`)
							),
					}),
				municipalities: {
					list: (provinceCode: string) =>
						queryOptions({
							queryKey: [
								...queryFactory.catalogue.countries.spanishProvinces.list()
									.queryKey,
								provinceCode,
								'municipalities',
							],
							queryFn: async () =>
								unwrapResponse(
									await api.GET(
										`/api/catalogue/countries/ESP/provinces/{provinceCode}/municipalities`,
										{ params: { path: { provinceCode } } }
									)
								),
						}),
					postalCodes: {
						list: (provinceCode: string, municipalityCode: string) =>
							queryOptions({
								queryKey: [
									...queryFactory.catalogue.countries.spanishProvinces.municipalities.list(
										provinceCode
									).queryKey,
									municipalityCode,
									'postal-codes',
								],
								queryFn: async () =>
									unwrapResponse(
										await api.GET(
											`/api/catalogue/countries/ESP/provinces/{provinceCode}/municipalities/{municipalityCode}/postal-codes`,
											{
												params: {
													path: {
														provinceCode,
														municipalityCode,
													},
												},
											}
										)
									),
							}),
					},
				},
			},
		},
		genders: () =>
			queryOptions({
				queryKey: ['catalogue', 'genders'],
				queryFn: async () =>
					unwrapResponse(await api.GET('/api/catalogue/person/genders')),
			}),
		relationships: () =>
			queryOptions({
				queryKey: ['catalogue', 'relationships'],
				queryFn: async () =>
					unwrapResponse(await api.GET('/api/catalogue/person/relationships')),
			}),
		documentTypes: () =>
			queryOptions({
				queryKey: ['catalogue', 'documentTypes'],
				queryFn: async () =>
					unwrapResponse(await api.GET('/api/catalogue/document/types')),
			}),
	},
	auth: {
		me: () =>
			queryOptions({
				queryKey: ['auth', 'me'],
				queryFn: async () => unwrapResponse(await api.GET('/api/auth/me')),
			}),
		login: () =>
			mutationOptions({
				mutationFn: async (credentials: {
					username: string;
					password: string;
					rememberMe?: boolean;
				}) => {
					const req = await api.POST('/api/auth/login', {
						body: credentials,
						headers: {
							'Content-Type': 'application/x-www-form-urlencoded',
						},
					});

					// Handle 401 Unauthorized response (invalid credentials)
					if (req.response.status === 401) return false;
					// Handle other non-OK responses
					unwrapResponse(req);

					return true;
				},
				onSuccess: async () => {
					await queryClient.invalidateQueries(queryFactory.auth.me());
				},
			}),
		logout: () =>
			mutationOptions({
				mutationFn: async () => {
					const req = await api.POST('/api/auth/logout');
					// Handle 401 Unauthorized response (user not logged in)
					if (req.response.status === 401) return false;
					// Handle other non-OK responses
					unwrapResponse(req);

					return true;
				},
				onSuccess: async () => {
					await queryClient.invalidateQueries(queryFactory.auth.me());
				},
			}),
	},
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
	response,
}: FetchResponse<T, unknown, '*/*'>) {
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
			`ApiErrorResponse: [${String(response.status)} ${response.statusText}] ${error?.title ?? ''}`
		);

		this.status = response.status;
		this.statusText = response.statusText;
		this.data = error;
	}
}

export {
	queryClient,
	queryFactory,
	executeMutation,
	ApiErrorResponse,
	// Should not be used directly unless custom query/mutation logic is needed. Use queryFactory and executeMutation instead.
	api as _api,
	unwrapResponse as _unwrapResponse,
};
