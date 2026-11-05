import React from "react";

import { cleanup, fireEvent, screen } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";

import BookingsSchedule from "~/component/booking/BookingsSchedule";
import NotificationsService from "~/services/NotificationsService";

import { renderComponent } from "../../test-utils";

afterEach(cleanup);

vi.mock("@mantine/schedule", () => ({
	ResourcesSchedule: (props: {
		onTimeSlotClick: (event: {
			resourceId: string;
			slotStart: string;
			slotEnd: string;
		}) => void;
		onDayClick: (event: { resourceId: string; date: string }) => void;
		onSlotDragEnd: (event: {
			resourceId: string;
			rangeStart: string;
			rangeEnd: string;
		}) => void;
		onEventDrop: (event: {
			event: { resourceId: string; payload: unknown };
			resourceId: string;
			newStart: string;
			newEnd: string;
		}) => void;
		onEventResize: (event: {
			event: { resourceId: string; payload: unknown };
			newStart: string;
			newEnd: string;
		}) => void;
		onEventClick: (event: { resourceId: string; id: string }) => void;
		events?: { id: string }[];
	}) =>
		React.createElement(
			"div",
			null,
			React.createElement(
				"button",
				{
					onClick: () => {
						props.onTimeSlotClick({
							resourceId: "a1",
							slotStart: "2025-01-01",
							slotEnd: "2025-01-02"
						});
					}
				},
				"slot"
			),
			React.createElement(
				"button",
				{
					onClick: () => {
						props.onDayClick({ resourceId: "a1", date: "2025-01-01" });
					}
				},
				"day"
			),
			React.createElement(
				"button",
				{
					onClick: () => {
						props.onSlotDragEnd({
							resourceId: "a1",
							rangeStart: "2025-01-01",
							rangeEnd: "2025-01-02"
						});
					}
				},
				"drag"
			),
			React.createElement(
				"button",
				{
					onClick: () => {
						props.onEventDrop({
							event: { resourceId: "a1", payload: {} },
							resourceId: "a1",
							newStart: "2025-01-01",
							newEnd: "2025-01-02"
						});
					}
				},
				"drop"
			),
			React.createElement(
				"button",
				{
					onClick: () => {
						props.onEventDrop({
							event: { resourceId: "a1", payload: {} },
							resourceId: "a2",
							newStart: "2025-01-01",
							newEnd: "2025-01-02"
						});
					}
				},
				"invalid drop"
			),
			React.createElement(
				"button",
				{
					onClick: () => {
						props.onEventResize({
							event: { resourceId: "a1", payload: {} },
							newStart: "2025-01-01",
							newEnd: "2025-01-02"
						});
					}
				},
				"resize"
			),
			props.events?.map((event) =>
				React.createElement(
					"button",
					{
						key: event.id,
						onClick: () => {
							props.onEventClick({ resourceId: "a1", id: event.id });
						}
					},
					event.id
				)
			)
		)
}));

test("forwards event selection", () => {
	const showBookingDetails = vi.fn();
	const accommodation = { id: "a1", name: "Hotel" } as never;
	const bookings = [{ id: "b1", status: "CONFIRMED" } as never];
	renderComponent(
		<BookingsSchedule
			data={new Map([[accommodation, bookings]])}
			showBookingDetails={showBookingDetails}
			state={{
				date: ["2025-01-01", vi.fn()],
				view: ["month", vi.fn()] as never
			}}
		/>
	);
	expect(screen.getByRole("button", { name: "b1" })).toBeInTheDocument();
});

test("handles schedule creation, range updates, and invalid accommodation moves", () => {
	const showBookingDetails = vi.fn();
	const error = vi
		.spyOn(NotificationsService, "error")
		.mockImplementation(() => undefined);
	const accommodation = { id: "a1", name: "Hotel" } as never;
	const bookings = [{ id: "b1", status: "CONFIRMED" } as never];

	renderComponent(
		<BookingsSchedule
			data={new Map([[accommodation, bookings]])}
			showBookingDetails={showBookingDetails}
			state={{
				date: ["2025-01-01", vi.fn()],
				view: ["month", vi.fn()] as never
			}}
		/>
	);

	for (const name of ["slot", "day", "drag", "drop", "resize"]) {
		fireEvent.click(screen.getByRole("button", { name }));
	}
	fireEvent.click(screen.getByRole("button", { name: "invalid drop" }));
	fireEvent.click(screen.getByRole("button", { name: "b1" }));

	expect(error).toHaveBeenCalled();
	expect(showBookingDetails).toHaveBeenCalledWith("a1", "b1");
});
