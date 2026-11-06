import React from "react";

import { cleanup, fireEvent, screen } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";

import BookingsTable from "~/components/booking/BookingsTable";

import { renderComponent } from "../../test-utils";

afterEach(cleanup);

vi.mock("mantine-datatable", () => ({
	DataTable: (props: {
		records?: unknown[];
		columns?: {
			accessor: string;
			render?: (record: unknown) => React.ReactNode;
			filter?: React.ReactNode;
		}[];
		onRowClick?: (event: { record: { id: string } }) => void;
	}) =>
		React.createElement(
			"div",
			null,
			props.records?.map((record) =>
				React.createElement(
					"button",
					{
						key: (record as { id: string }).id,
						onClick: () =>
							props.onRowClick?.({
								record: record as { id: string }
							})
					},
					(record as { id: string }).id
				)
			),
			props.columns?.map((column) =>
				React.createElement(
					"div",
					{ key: column.accessor },
					props.records?.map((record) => column.render?.(record)),
					column.filter
				)
			)
		),
	useDataTableColumns: ({ columns }: { columns: unknown[] }) => ({
		effectiveColumns: columns
	})
}));

const booking = {
	id: "b1",
	status: "CONFIRMED",
	selfCheckInRequested: true,
	holderName: "Ada Lovelace",
	startTime: "2025-01-01T10:00:00Z",
	endTime: "2025-01-02T10:00:00Z",
	numberOfPeople: 2,
	numberOfRooms: 1,
	internetConnection: true
} as never;

test("renders booking records", () => {
	const accommodation = { id: "a1", name: "Hotel" } as never;
	const bookings = [booking];
	renderComponent(
		<BookingsTable
			accommodations={[accommodation]}
			data={new Map([[accommodation, bookings]])}
			showBookingDetails={vi.fn()}
			filters={{
				accommodations: [[], vi.fn()],
				selfCheckIn: ["null", vi.fn()],
				status: [[], vi.fn()]
			}}
		/>
	);
	expect(screen.getByRole("button", { name: "b1" })).toBeInTheDocument();
});

test("opens booking details when a row is selected", () => {
	const showBookingDetails = vi.fn();
	const accommodation = { id: "a1", name: "Hotel" } as never;
	const bookings = [booking];

	renderComponent(
		<BookingsTable
			accommodations={[accommodation]}
			data={new Map([[accommodation, bookings]])}
			showBookingDetails={showBookingDetails}
			filters={{
				accommodations: [[], vi.fn()],
				selfCheckIn: ["null", vi.fn()],
				status: [[], vi.fn()]
			}}
		/>
	);

	fireEvent.click(screen.getByRole("button", { name: "b1" }));

	expect(showBookingDetails).toHaveBeenCalledWith("a1", "b1");
});
