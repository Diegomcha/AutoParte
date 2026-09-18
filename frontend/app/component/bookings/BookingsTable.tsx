import { useState } from "react";

import { Group, MultiSelect, Radio } from "@mantine/core";

import { ChatSlashIcon, PaperPlaneTiltIcon } from "@phosphor-icons/react";
import { sortBy } from "lodash";
import { DataTable, useDataTableColumns } from "mantine-datatable";
import { useTranslation } from "react-i18next";

import TimeService from "~/services/TimeService";

import BooleanBadge from "../BooleanBadge";
import WifiBadge from "../WifiBadge";
import BookingStatusBadge from "./BookingStatusBadge";

import type {
	AccommodationDtoResponse,
	BookingDtoResponse
} from "~/@types/api";
import type { DataTableSortStatus } from "mantine-datatable";
import type { Dispatch, SetStateAction } from "react";

type BookingData = BookingDtoResponse & {
	accommodation: AccommodationDtoResponse;
};

interface BookingsTableProps {
	accommodations: AccommodationDtoResponse[];
	data: Map<AccommodationDtoResponse, BookingDtoResponse[]>;
	showBookingDetails: (accommodationId: string, bookingId: string) => void;
	filters: {
		accommodations: [string[], Dispatch<SetStateAction<string[]>>];
		selfCheckIn: [
			"true" | "false" | "null",
			Dispatch<SetStateAction<"true" | "false" | "null">>
		];
		status: [
			BookingDtoResponse["status"][],
			Dispatch<SetStateAction<BookingDtoResponse["status"][]>>
		];
	};
}

const COLUMNS_STATE_KEY = "bookings-table-columns";

export default function BookingsTable({
	accommodations,
	data,
	showBookingDetails,
	filters: {
		accommodations: [accommodationsFilter, setAccommodationsFilter],
		selfCheckIn: [selfCheckInFilter, setSelfCheckInFilter],
		status: [statusFilter, setStatusFilter]
	}
}: Readonly<BookingsTableProps>) {
	const { t } = useTranslation("components", { keyPrefix: "bookingsTable" });
	const { t: tBooking } = useTranslation("entities", { keyPrefix: "booking" });

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

	const { effectiveColumns } = useDataTableColumns<BookingData>({
		key: COLUMNS_STATE_KEY,
		columns: [
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "accommodation",
				title: tBooking(($) => $.details.accommodation.label),
				render: (booking) => booking.accommodation.name,
				filtering: accommodationsFilter.length > 0,
				filter: (
					<MultiSelect
						maw={300}
						label={t(($) => $.filters.accommodation.label)}
						description={t(($) => $.filters.accommodation.description)}
						placeholder={t(($) => $.filters.accommodation.placeholder)}
						data={Array.from(accommodations).map((accommodation) => ({
							value: accommodation.id,
							label: accommodation.name
						}))}
						value={accommodationsFilter}
						onChange={setAccommodationsFilter}
						comboboxProps={{ withinPortal: false }}
						searchable
						clearable
						hidePickedOptions
					/>
				)
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
				),
				filtering: selfCheckInFilter !== "null",
				filter: (
					<Radio.Group
						label={t(($) => $.filters.selfCheckInRequested.label)}
						description={t(($) => $.filters.selfCheckInRequested.description)}
						value={selfCheckInFilter}
						onChange={setSelfCheckInFilter}
					>
						<Group mt="xs">
							<Radio
								value={"null"}
								label={t(($) => $.filters.selfCheckInRequested.options.all)}
							/>
							<Radio
								value={"true"}
								label={t(($) => $.filters.selfCheckInRequested.options.yes)}
							/>
							<Radio
								value={"false"}
								label={t(($) => $.filters.selfCheckInRequested.options.no)}
							/>
						</Group>
					</Radio.Group>
				)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "status",
				title: tBooking(($) => $.details.status.label),
				render: (booking) => <BookingStatusBadge status={booking.status} />,
				filtering: statusFilter.length > 0,
				filter: (
					<MultiSelect
						maw={300}
						label={t(($) => $.filters.status.label)}
						description={t(($) => $.filters.status.description)}
						placeholder={t(($) => $.filters.status.placeholder)}
						data={Object.entries(
							tBooking(($) => $.details.status.states, {
								returnObjects: true
							})
						).map(([value, { label }]) => ({
							value: value as BookingDtoResponse["status"],
							label
						}))}
						value={statusFilter}
						onChange={setStatusFilter}
						comboboxProps={{ withinPortal: false }}
						searchable
						clearable
						hidePickedOptions
						renderOption={({ option }) => (
							<BookingStatusBadge status={option.value} />
						)}
					/>
				)
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

	const tableRecords = sortBy(bookings, tableSortStatus.columnAccessor);
	if (tableSortStatus.direction === "desc") tableRecords.reverse();

	return (
		<DataTable
			h="100%"
			highlightOnHover
			noRecordsText={t(($) => $.errors.noBookingsInPeriod)}
			storeColumnsKey={COLUMNS_STATE_KEY}
			columns={effectiveColumns}
			records={tableRecords}
			onRowClick={({ record: booking }) => {
				showBookingDetails(booking.accommodation.id, booking.id);
			}}
			sortStatus={tableSortStatus}
			onSortStatusChange={setTableSortStatus}
		/>
	);
}
