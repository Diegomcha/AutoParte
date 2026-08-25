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
	UnstyledButton,
} from '@mantine/core';
import { ResourcesSchedule } from '@mantine/schedule';
import { CaretRightIcon, UserCircleIcon } from '@phosphor-icons/react';
import { useMutation, useSuspenseQuery } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import BookingHoverCard from '~/component/BookingHoverCard';
import BookingStatusBadge from '~/component/BookingStatusBadge';
import { lang } from '~/i18n';
import AuthService from '~/services/AuthService';
import NotificationsService from '~/services/NotificationsService';
import TimeService from '~/services/TimeService';
import { DataTable, useDataTableColumns } from 'mantine-datatable';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, Outlet, useNavigate } from 'react-router';
import type { Route } from './+types/index';
import type {
	ResourcesScheduleViewLevel,
	ScheduleEventData,
	ScheduleResourceData,
} from '@mantine/schedule';
import type {
	AccommodationDtoResponse,
	BookingDtoResponse,
} from '~/@types/api';

// Ensure the user is authenticated before allowing access to any protected routes.
export async function clientLoader({ request }: Route.ClientLoaderArgs) {
	if (!(await AuthService.isAuthenticated())) {
		return AuthService.getLoginRedirection(request);
	}

	return {
		account: await AuthService.getLoggedInUser(),
		isAdmin: await AuthService.isAdmin(),
	};
}

export default function ProtectedLayout({ loaderData }: Route.ComponentProps) {
	const { account, isAdmin } = loaderData;

	const { t } = useTranslation();
	const navigate = useNavigate();

	const [accountMenuOpened, setAccountMenuOpened] = useState(false);
	const [view, setView] = useState<ResourcesScheduleViewLevel>('month');
	const [date, setDate] = useState<string>(new Date().toUTCString());

	const { data: accommodations } = useSuspenseQuery({
		queryKey: ['accommodations'],
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/accommodations', {
					params: { query: { page: 0, size: 0 } },
				})
			).content ?? [],
	});

	const { data } = useSuspenseQuery({
		queryKey: ['bookings', view, date],
		queryFn: async () => {
			const map = new Map<AccommodationDtoResponse, BookingDtoResponse[]>();

			for (const accommodation of accommodations)
				map.set(
					accommodation,
					throwErrors(
						await api.GET('/api/accommodations/{accommodationId}/bookings', {
							params: {
								path: { accommodationId: accommodation.id },
								query: { page: 0, size: 0, ...getDateRange(view, date) },
							},
						})
					).content ?? []
				);

			return map;
		},
	});

	const resources: ScheduleResourceData[] = Array.from(accommodations).map(
		(accommodation) => ({
			id: accommodation.id,
			label: accommodation.name,
			payload: accommodation,
		})
	);

	const events: ScheduleEventData[] = Array.from(data.entries()).flatMap(
		([accommodation, bookings]) =>
			bookings.map<ScheduleEventData>((booking) => ({
				resourceId: accommodation.id,
				id: booking.id,
				title: t(
					($) =>
						booking.holderName
							? $.bookings.properties.details.name.withHolder
							: $.bookings.properties.details.name.noHolder,
					{
						status: booking.status,
						numberOfPeople: booking.numberOfPeople,
						holderName: booking.holderName,
					}
				),
				start: TimeService(booking.startTime).toDate(),
				end: TimeService(booking.endTime).toDate(),
				color: t(
					($) =>
						$.bookings.properties.details.status.states[booking.status].color
				),
				display: booking.canBeModified ? 'default' : 'background',
				payload: booking,
			}))
	);

	// Table state

	const columnsKey = 'bookings-table-columns';
	const { effectiveColumns } = useDataTableColumns<
		BookingDtoResponse & { accommodation: AccommodationDtoResponse }
	>({
		key: columnsKey,
		columns: [
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: 'accommodation',
				title: t(($) => $.bookings.properties.details.accommodation),
				render: (booking) => booking.accommodation.name,
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: 'status',
				title: t(($) => $.bookings.properties.details.status.label),
				render: (booking) => <BookingStatusBadge status={booking.status} />,
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: 'holderName',
				title: t(($) => $.bookings.properties.details.holderName.label),
				render: (booking) =>
					booking.holderName ??
					t(($) => $.bookings.properties.details.holderName.undefined),
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: 'startTime',
				title: t(($) => $.bookings.properties.details.startTime),
				render: (booking) => TimeService(booking.startTime).format('LLLL'),
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: 'endTime',
				title: t(($) => $.bookings.properties.details.endTime),
				render: (booking) => TimeService(booking.endTime).format('LLLL'),
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: 'numberOfPeople',
				title: t(($) => $.bookings.properties.details.numberOfPeople.label),
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: 'numberOfRooms',
				title: t(($) => $.bookings.properties.details.numberOfRooms.label),
				render: (booking) =>
					booking.numberOfRooms ??
					t(($) => $.bookings.properties.details.numberOfRooms.undefined),
			},
		],
	});

	const { mutate: createBooking } = useMutation({
		throwOnError: true,
		mutationFn: async ({
			accommodationId,
			startTime,
			endTime,
		}: {
			accommodationId: string;
			startTime: Date;
			endTime: Date;
		}) =>
			throwErrors(
				await api.POST('/api/accommodations/{accommodationId}/bookings', {
					params: { path: { accommodationId } },
					body: {
						startTime: startTime.toISOString(),
						endTime: endTime.toISOString(),
						numberOfPeople: 1,
					},
				})
			),
		onSuccess: async () => {
			await queryClient.invalidateQueries({
				queryKey: ['bookings'],
			});
		},
	});

	const { mutate: updateBooking } = useMutation({
		throwOnError: true,
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
		}) =>
			throwErrors(
				await api.PUT('/api/accommodations/{accommodationId}/bookings/{id}', {
					params: { path: { accommodationId, id: booking.id } },
					body: {
						...booking,
						startTime: newStart.toISOString(),
						endTime: newEnd.toISOString(),
					},
				})
			),
		onSuccess: async () => {
			await queryClient.invalidateQueries({
				queryKey: ['bookings'],
			});
		},
	});

	function showBookingDetails(accommodationId: string, bookingId: string) {
		void navigate(`/accommodations/${accommodationId}/bookings/${bookingId}`);
	}

	return (
		<AppShell header={{ height: 60 }} padding="md">
			<AppShell.Header px="md">
				<Group justify="space-between" className="h-full">
					<Title size="h2">{t(($) => $.meta.name)}</Title>

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
										className={`${accountMenuOpened ? 'rotate-90' : ''} transition-transform`}
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
										{t(($) => $.header.admin)}
									</Menu.Item>
									<Menu.Divider />
								</>
							)}
							<Menu.Item color="red" component={Link} to="/auth/logout">
								{t(($) => $.header.logout)}
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
									? t(($) => $.bookings.errors.noAccommodationsAdmin)
									: t(($) => $.bookings.errors.noAccommodations)}
							</Text>
						</Stack>
					</Center>
				) : (
					<Splitter
						orientation="vertical"
						h="calc(100vh - 92px)"
						handleColor="gray.3"
					>
						<Splitter.Pane defaultSize={30} min={10} collapsible mb={'sm'}>
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
										endTime: TimeService(slotEnd).toDate(),
									});
								}}
								onDayClick={({ resourceId, date }) => {
									createBooking({
										accommodationId: resourceId as string,
										startTime: TimeService(date).toDate(),
										endTime: TimeService(date).add(1, 'day').toDate(),
									});
								}}
								onSlotDragEnd={({ resourceId, rangeStart, rangeEnd }) => {
									createBooking({
										accommodationId: resourceId as string,
										startTime: TimeService(rangeStart).toDate(),
										endTime: TimeService(rangeEnd).toDate(),
									});
								}}
								onEventDrop={({ newEnd, newStart, resourceId, event }) => {
									if (event.resourceId !== resourceId)
										NotificationsService.error(
											t(($) => $.bookings.errors.cannotChangeAccommodation)
										);
									else
										updateBooking({
											accommodationId: event.resourceId as string,
											booking: event.payload as BookingDtoResponse,
											newStart: TimeService(newStart).toDate(),
											newEnd: TimeService(newEnd).toDate(),
										});
								}}
								onEventResize={({ newEnd, newStart, event }) => {
									updateBooking({
										accommodationId: event.resourceId as string,
										booking: event.payload as BookingDtoResponse,
										newStart: TimeService(newStart).toDate(),
										newEnd: TimeService(newEnd).toDate(),
									});
								}}
								onEventClick={(event) => {
									showBookingDetails(
										event.resourceId as string,
										event.id as string
									);
								}}
								monthViewProps={{
									renderEvent: renderHoverCard,
								}}
								events={events}
								resources={resources}
								locale={lang}
								labels={{
									...t(($) => $.schedule, { returnObjects: true }),
									moreLabel: (count) =>
										t(($) => $.schedule.moreLabel, { count }),
								}}
							/>
						</Splitter.Pane>
						<Splitter.Pane defaultSize={70} min={10} collapsible mt={'sm'}>
							<DataTable
								height="100%"
								noRecordsText={t(($) => $.bookings.errors.noBookingsInPeriod)}
								storeColumnsKey={columnsKey}
								columns={effectiveColumns}
								records={Array.from(data.entries()).flatMap(
									([accommodation, bookings]) =>
										bookings.map((booking) => ({
											...booking,
											accommodation,
										}))
								)}
								onRowClick={({ record: booking }) => {
									showBookingDetails(booking.accommodation.id, booking.id);
								}}
								// TODO: Implement client-sided sorting for the table
								// sortStatus={sortStatus}
								// onSortStatusChange={setSortStatus}
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
 * @returns A tuple containing the start and end date of the range in ISO string format.
 */
function getDateRange(unit: ResourcesScheduleViewLevel, date: Date | string) {
	const baseDate = TimeService(date);

	// Makes sure that the week starts on Monday instead of Sunday
	const rangeUnit = unit === 'week' ? 'isoWeek' : unit;
	return {
		startRange: baseDate.startOf(rangeUnit).toISOString(),
		endRange: baseDate.endOf(rangeUnit).toISOString(),
	};
}

function renderHoverCard(
	event: ScheduleEventData,
	props: React.ComponentPropsWithoutRef<'button'> & {
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
