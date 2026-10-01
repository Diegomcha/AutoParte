import { HoverCard, UnstyledButton } from "@mantine/core";
import { ResourcesSchedule } from "@mantine/schedule";

import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import { lang } from "~/i18n";
import { queryFactory } from "~/services/Api";
import NotificationsService from "~/services/NotificationsService";
import TimeService from "~/services/TimeService";

import BookingHoverCard from "./BookingHoverCard";

import type {
	ResourcesScheduleViewLevel,
	ScheduleEventData,
	ScheduleResourceData
} from "@mantine/schedule";
import type {
	AccommodationDtoResponse,
	BookingDtoResponse
} from "~/@types/api";
import type { Dispatch, SetStateAction } from "react";

interface BookingsScheduleProps {
	data: Map<AccommodationDtoResponse, BookingDtoResponse[]>;
	showBookingDetails: (accommodationId: string, bookingId: string) => void;
	state: {
		date: [string, Dispatch<SetStateAction<string>>];
		view: [
			ResourcesScheduleViewLevel,
			Dispatch<SetStateAction<ResourcesScheduleViewLevel>>
		];
	};
}

export default function BookingsSchedule({
	data,
	showBookingDetails,
	state: {
		date: [date, setDate],
		view: [view, setView]
	}
}: Readonly<BookingsScheduleProps>) {
	const { t } = useTranslation("components", { keyPrefix: "bookingsSchedule" });
	const { t: tBooking } = useTranslation("entities", { keyPrefix: "booking" });

	// * Schedule state

	const resources: ScheduleResourceData[] = Array.from(data.keys()).map(
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

	// * Actions

	const { mutate: createBooking } = useMutation(
		queryFactory.accommodations.bookings.create()
	);

	const { mutate: updateBookingRange } = useMutation(
		queryFactory.accommodations.bookings.updateRange()
	);

	return (
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
						t(($) => $.errors.cannotChangeAccommodation)
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
				showBookingDetails(event.resourceId as string, event.id as string);
			}}
			monthViewProps={{
				scrollAreaProps: {
					scrollbarSize: 6
				},
				startScrollDate: TimeService().startOf("day").format("YYYY-MM-DD"),
				renderEvent: (event, props) => (
					<HoverCard closeDelay={0} transitionProps={{ duration: 0 }}>
						<HoverCard.Target>
							<UnstyledButton {...props} />
						</HoverCard.Target>
						<HoverCard.Dropdown>
							<BookingHoverCard booking={event.payload as BookingDtoResponse} />
						</HoverCard.Dropdown>
					</HoverCard>
				)
			}}
			weekViewProps={{
				scrollAreaProps: {
					scrollbarSize: 6
				},
				withCurrentTimeIndicator: true
			}}
			dayViewProps={{
				scrollAreaProps: {
					scrollbarSize: 6
				},
				withCurrentTimeIndicator: true
			}}
			styles={{
				resourcesDayViewScrollArea: {},
				resourcesWeekViewScrollArea: {},
				resourcesMonthViewScrollArea: { scrollbarSize: 24 }
			}}
			events={events}
			resources={resources}
			locale={lang}
			labels={{
				...t(($) => $.labels, { returnObjects: true }),
				moreLabel: (count) => t(($) => $.labels.moreLabel, { count })
			}}
		/>
	);
}
