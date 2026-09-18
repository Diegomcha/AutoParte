import { useState } from "react";
import { Link, Outlet, useNavigate } from "react-router";

import {
	AppShell,
	Button,
	Center,
	Group,
	Menu,
	Splitter,
	Stack,
	Text,
	Title
} from "@mantine/core";

import { CaretRightIcon, UserCircleIcon } from "@phosphor-icons/react";
import { useSuspenseQueries } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import BookingsSchedule from "~/component/bookings/BookingsSchedule";
import BookingsTable from "~/component/bookings/BookingsTable";
import {
	_api,
	_unwrapResponse,
	queryClient,
	queryFactory
} from "~/services/Api";
import AuthService from "~/services/AuthService";
import TimeService from "~/services/TimeService";

import type { ResourcesScheduleViewLevel } from "@mantine/schedule";
import type { BookingDtoResponse } from "~/@types/api";
import type { Route } from "./+types/index";

// Ensure the user is authenticated before allowing access to any protected routes.
export async function clientLoader({ request }: Route.ClientLoaderArgs) {
	if (!(await AuthService.isAuthenticated())) {
		return AuthService.getLoginRedirection(request);
	}

	const [accommodations, account, isAdmin] = await Promise.all([
		queryClient.query(queryFactory.accommodations.orderedList()),
		AuthService.getLoggedInUser(),
		AuthService.isAdmin()
	]);

	return {
		accommodations,
		account,
		isAdmin
	};
}

export default function ProtectedLayout({
	loaderData: { accommodations, account, isAdmin }
}: Route.ComponentProps) {
	const { t } = useTranslation("routes");
	const { t: tCommon } = useTranslation();

	const navigate = useNavigate();

	// * Backend filtering

	const [view, setView] = useState<ResourcesScheduleViewLevel>("month");
	const [date, setDate] = useState<string>(new Date().toUTCString());
	const [accommodationsFilter, setAccommodationsFilter] = useState<string[]>(
		[]
	);

	const filteredAccommodations = accommodationsFilter.length
		? accommodations.filter((accommodation) =>
				accommodationsFilter.includes(accommodation.id)
			)
		: accommodations;

	const data = useSuspenseQueries({
		// eslint-disable-next-line @tanstack/query/prefer-query-options -- Unique case
		queries: filteredAccommodations.map((accommodation) => ({
			queryKey: [
				...queryFactory.accommodations.bookings.list(accommodation.id).queryKey,
				getDateRange(view, date)
			],
			queryFn: async () =>
				[
					accommodation,
					_unwrapResponse(
						await _api.GET("/api/accommodations/{accommodationId}/bookings", {
							params: {
								path: { accommodationId: accommodation.id },
								query: { page: 0, size: 0, ...getDateRange(view, date) }
							}
						})
					).content ?? []
				] as const
		})),
		combine: (results) => new Map(results.map((result) => result.data))
	});

	// * Local filtering

	const [selfCheckInFilter, setSelfCheckInFilter] = useState<
		"true" | "false" | "null"
	>("null");
	const [statusFilter, setStatusFilter] = useState<
		BookingDtoResponse["status"][]
	>([]);

	const filteredData = new Map(
		Array.from(data.entries()).map(([accommodation, bookings]) => [
			accommodation,
			bookings.filter((booking) => {
				const selfCheckInMatch =
					selfCheckInFilter === "null" ||
					(selfCheckInFilter === "true" && booking.selfCheckInRequested) ||
					(selfCheckInFilter === "false" && !booking.selfCheckInRequested);

				const statusMatch =
					statusFilter.length === 0 || statusFilter.includes(booking.status);

				return selfCheckInMatch && statusMatch;
			})
		])
	);

	// * Navigation

	function showBookingDetails(accommodationId: string, bookingId: string) {
		void navigate(`/accommodations/${accommodationId}/bookings/${bookingId}`);
	}

	// * Render

	const [accountMenuOpened, setAccountMenuOpened] = useState(false);

	return (
		<AppShell header={{ height: 60 }} padding="md">
			<AppShell.Header px="md">
				<Group justify="space-between" h="100%">
					<Title size="h2">{tCommon(($) => $.meta.name)}</Title>

					<Menu
						shadow="xs"
						opened={accountMenuOpened}
						onChange={setAccountMenuOpened}
					>
						<Menu.Target>
							<Button
								variant="outline"
								leftSection={<UserCircleIcon size={16} />}
								rightSection={
									<CaretRightIcon
										className="transition-rotate"
										style={{
											rotate: accountMenuOpened ? "90deg" : "0deg"
										}}
									/>
								}
							>
								{account?.username}
							</Button>
						</Menu.Target>

						<Menu.Dropdown>
							{isAdmin && (
								<>
									<Menu.Item component={Link} to="/admin">
										{tCommon(($) => $.header.admin)}
									</Menu.Item>
									<Menu.Divider />
								</>
							)}
							<Menu.Label>{tCommon(($) => $.header.account.title)}</Menu.Label>
							<Menu.Item component={Link} to="/auth/update-password">
								{tCommon(($) => $.header.account.updatePassword)}
							</Menu.Item>
							<Menu.Item color="red" component={Link} to="/auth/logout">
								{tCommon(($) => $.header.account.logout)}
							</Menu.Item>
						</Menu.Dropdown>
					</Menu>
				</Group>
			</AppShell.Header>
			<AppShell.Main>
				{accommodations.length === 0 ? (
					<Center h="calc(100vh - 92px)">
						<Stack align="center">
							<Text ta="center" size="lg">
								{isAdmin
									? t(($) => $.index.errors.noAccommodationsAdmin)
									: t(($) => $.index.errors.noAccommodations)}
							</Text>
						</Stack>
					</Center>
				) : (
					<Splitter
						orientation="vertical"
						h="calc(100vh - 92px)"
						handleColor="gray.3"
					>
						<Splitter.Pane defaultSize={50} min={10} collapsible mb={"sm"}>
							<BookingsSchedule
								data={filteredData}
								showBookingDetails={showBookingDetails}
								state={{
									date: [date, setDate],
									view: [view, setView]
								}}
							/>
						</Splitter.Pane>
						<Splitter.Pane defaultSize={50} min={10} collapsible mt={"sm"}>
							<BookingsTable
								accommodations={accommodations}
								data={filteredData}
								showBookingDetails={showBookingDetails}
								filters={{
									accommodations: [
										accommodationsFilter,
										setAccommodationsFilter
									],
									selfCheckIn: [selfCheckInFilter, setSelfCheckInFilter],
									status: [statusFilter, setStatusFilter]
								}}
							/>
						</Splitter.Pane>
					</Splitter>
				)}
				<Outlet />
			</AppShell.Main>
		</AppShell>
	);
}

/**
 * Gets the start and end date of a given date range based on the specified unit (day, week, month).
 * @param unit Unit of time to determine the range (day, week, month)
 * @param date The date to determine the range for. Can be a Date object or a string.
 * @returns An object containing the start and end date of the range in ISO string format.
 */
function getDateRange(unit: ResourcesScheduleViewLevel, date: Date | string) {
	const baseDate = TimeService(date);

	// Makes sure that the week starts on Monday instead of Sunday
	const rangeUnit = unit === "week" ? "isoWeek" : unit;
	return {
		startRange: baseDate.startOf(rangeUnit).toISOString(),
		endRange: baseDate.endOf(rangeUnit).toISOString()
	};
}
