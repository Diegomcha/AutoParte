import { useState } from "react";
import { Link, Outlet, useNavigate } from "react-router";

import {
	AppShell,
	Button,
	Center,
	Group,
	HoverCard,
	Menu,
	Splitter,
	Stack,
	Text,
	Title,
	UnstyledButton
} from "@mantine/core";
import { ResourcesSchedule } from "@mantine/schedule";

import {
	CaretRightIcon,
	ChatSlashIcon,
	PaperPlaneTiltIcon,
	UserCircleIcon
} from "@phosphor-icons/react";
import { useMutation, useSuspenseQueries } from "@tanstack/react-query";
import sortBy from "lodash/sortBy";
import { DataTable, useDataTableColumns } from "mantine-datatable";
import { useTranslation } from "react-i18next";

import BookingHoverCard from "~/component/BookingHoverCard";
import BookingStatusBadge from "~/component/BookingStatusBadge";
import BooleanBadge from "~/component/BooleanBadge";
import WifiBadge from "~/component/WifiBadge";
import { lang } from "~/i18n";
import {
	_api,
	_unwrapResponse,
	queryClient,
	queryFactory
} from "~/services/Api";
import AuthService from "~/services/AuthService";
import NotificationsService from "~/services/NotificationsService";
import TimeService from "~/services/TimeService";

import type {
	ResourcesScheduleViewLevel,
	ScheduleEventData,
	ScheduleResourceData
} from "@mantine/schedule";
import type {
	AccommodationDtoResponse,
	BookingDtoResponse
} from "~/@types/api";
import type { DataTableSortStatus } from "mantine-datatable";
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

const COLUMNS_STATE_KEY = "bookings-table-columns";

export default function ProtectedLayout({
	loaderData: { accommodations, account, isAdmin }
}: Route.ComponentProps) {
	const { t } = useTranslation("routes");
	const { t: tBooking } = useTranslation("entities", { keyPrefix: "booking" });
	const { t: tComponents } = useTranslation("components");
	const { t: tCommon } = useTranslation();

	const navigate = useNavigate();

	const [accountMenuOpened, setAccountMenuOpened] = useState(false);
	const [view, setView] = useState<ResourcesScheduleViewLevel>("month");
	const [date, setDate] = useState<string>(new Date().toUTCString());

	const data = useSuspenseQueries({
		// eslint-disable-next-line @tanstack/query/prefer-query-options -- Unique case
		queries: accommodations.map((accommodation) => ({
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

	// * Schedule state

	const resources: ScheduleResourceData[] = Array.from(accommodations).map(
		(accommodation) => ({
			id: accommodation.id,
			label: accommodation.name,
			payload: accommodation
		})
	);

	const events: ScheduleEventData[] = Array.from(data.entries()).flatMap(
		([accommodation, bookings]) =>
			bookings.map<ScheduleEventData>((booking) => ({
				resourceId: accommodation.id,
				id: booking.id,
				title: tBooking(
					($) =>
						booking.holderName
							? $.details.name.withHolder
							: $.details.name.noHolder,
					{
						status: booking.status,
						numberOfPeople: booking.numberOfPeople,
						holderName: booking.holderName
					}
				),
				start: TimeService(booking.startTime).toDate(),
				end: TimeService(booking.endTime).toDate(),
				color: tBooking(($) => $.details.status.states[booking.status].color),
				display: booking.canBeModified ? "default" : "background",
				payload: booking
			}))
	);

	// * Table state

	const [tableSortStatus, setTableSortStatus] = useState<
		DataTableSortStatus<
			BookingDtoResponse & { accommodation: AccommodationDtoResponse }
		>
	>({
		columnAccessor: "id",
		direction: "asc"
	});

	const bookings = Array.from(data.entries()).flatMap(
		([accommodation, bookings]) =>
			bookings.map((booking) => ({
				...booking,
				accommodation
			}))
	);

	const tableRecords = sortBy(bookings, tableSortStatus.columnAccessor);
	if (tableSortStatus.direction === "desc") tableRecords.reverse();

	const { effectiveColumns } = useDataTableColumns<
		BookingDtoResponse & { accommodation: AccommodationDtoResponse }
	>({
		key: COLUMNS_STATE_KEY,
		columns: [
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "accommodation",
				title: tBooking(($) => $.details.accommodation.label),
				render: (booking) => booking.accommodation.name
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "selfCheckInRequested",
				title: tBooking(($) => $.selfCheckInRequested.label),
				render: (booking) => (
					<BooleanBadge
						value={booking.selfCheckInRequested}
						icons={{
							true: <PaperPlaneTiltIcon weight="bold" />,
							false: <ChatSlashIcon weight="bold" />
						}}
					/>
				)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "status",
				title: tBooking(($) => $.details.status.label),
				render: (booking) => <BookingStatusBadge status={booking.status} />
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "holderName",
				title: tBooking(($) => $.details.holderName.label),
				render: (booking) =>
					booking.holderName ?? tBooking(($) => $.details.holderName.undefined)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "startTime",
				title: tBooking(($) => $.details.startTime.label),
				render: (booking) => TimeService(booking.startTime).format("LLLL")
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "endTime",
				title: tBooking(($) => $.details.endTime.label),
				render: (booking) => TimeService(booking.endTime).format("LLLL")
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "numberOfPeople",
				title: tBooking(($) => $.details.numberOfPeople.label)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "numberOfRooms",
				title: tBooking(($) => $.details.numberOfRooms.label),
				render: (booking) =>
					tBooking(($) => $.details.numberOfRooms.value, {
						count: booking.numberOfRooms ?? 0
					})
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "internetConnection",
				title: tBooking(($) => $.details.internetConnection.label),
				render: (booking) => <WifiBadge value={booking.internetConnection} />
			}
		]
	});

	// * Actions

	const { mutate: createBooking } = useMutation(
		queryFactory.accommodations.bookings.create()
	);

	const { mutate: updateBookingRange } = useMutation(
		queryFactory.accommodations.bookings.updateRange()
	);

	function showBookingDetails(accommodationId: string, bookingId: string) {
		void navigate(`/accommodations/${accommodationId}/bookings/${bookingId}`);
	}

	// * Render

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
						<Splitter.Pane defaultSize={30} min={10} collapsible mb={"sm"}>
							<ResourcesSchedule
								date={date}
								onDateChange={setDate}
								view={view}
								onViewChange={setView}
								withDragSlotSelect
								withEventResize
								withEventsDragAndDrop
								onTimeSlotClick={({ resourceId, slotStart, slotEnd }) => {
									createBooking({
										accommodationId: resourceId as string,
										startTime: TimeService(slotStart).toDate(),
										endTime: TimeService(slotEnd).toDate()
									});
								}}
								onDayClick={({ resourceId, date }) => {
									createBooking({
										accommodationId: resourceId as string,
										startTime: TimeService(date).toDate(),
										endTime: TimeService(date).add(1, "day").toDate()
									});
								}}
								onSlotDragEnd={({ resourceId, rangeStart, rangeEnd }) => {
									createBooking({
										accommodationId: resourceId as string,
										startTime: TimeService(rangeStart).toDate(),
										endTime: TimeService(rangeEnd).toDate()
									});
								}}
								onEventDrop={({ newEnd, newStart, resourceId, event }) => {
									if (event.resourceId !== resourceId)
										NotificationsService.error(
											t(($) => $.index.errors.cannotChangeAccommodation)
										);
									else
										updateBookingRange({
											accommodationId: event.resourceId as string,
											booking: event.payload as BookingDtoResponse,
											newStart: TimeService(newStart).toDate(),
											newEnd: TimeService(newEnd).toDate()
										});
								}}
								onEventResize={({ newEnd, newStart, event }) => {
									updateBookingRange({
										accommodationId: event.resourceId as string,
										booking: event.payload as BookingDtoResponse,
										newStart: TimeService(newStart).toDate(),
										newEnd: TimeService(newEnd).toDate()
									});
								}}
								onEventClick={(event) => {
									showBookingDetails(
										event.resourceId as string,
										event.id as string
									);
								}}
								monthViewProps={{
									renderEvent: renderHoverCard
								}}
								events={events}
								resources={resources}
								locale={lang}
								labels={{
									...tComponents(($) => $.schedule, { returnObjects: true }),
									moreLabel: (count) =>
										tComponents(($) => $.schedule.moreLabel, { count })
								}}
							/>
						</Splitter.Pane>
						<Splitter.Pane defaultSize={70} min={10} collapsible mt={"sm"}>
							<DataTable
								height="100%"
								noRecordsText={t(($) => $.index.errors.noBookingsInPeriod)}
								storeColumnsKey={COLUMNS_STATE_KEY}
								columns={effectiveColumns}
								records={tableRecords}
								onRowClick={({ record: booking }) => {
									showBookingDetails(booking.accommodation.id, booking.id);
								}}
								highlightOnHover
								sortStatus={tableSortStatus}
								onSortStatusChange={setTableSortStatus}
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

function renderHoverCard(
	event: ScheduleEventData,
	props: React.ComponentPropsWithoutRef<"button"> & {
		children: React.ReactNode;
	}
): React.ReactElement {
	return (
		<HoverCard closeDelay={0} transitionProps={{ duration: 0 }}>
			<HoverCard.Target>
				<UnstyledButton {...props} />
			</HoverCard.Target>
			<HoverCard.Dropdown>
				<BookingHoverCard booking={event.payload as BookingDtoResponse} />
			</HoverCard.Dropdown>
		</HoverCard>
	);
}
