import { screen } from "@testing-library/react";
import { expect, test } from "vitest";

import CheckInBookingDetails from "~/components/checkIn/CheckInBookingDetails";

import { renderComponent } from "../../test-utils";

test("renders booking accommodation", () => {
	renderComponent(
		<CheckInBookingDetails
			booking={
				{
					accommodationName: "Hotel",
					startTime: "2025-01-01",
					endTime: "2025-01-02",
					numberOfPeople: 2,
					numberOfRooms: 1
				} as never
			}
		/>
	);
	expect(screen.getByText("Hotel")).toBeInTheDocument();
	expect(screen.getByText("Detalles de la reserva")).toBeInTheDocument();
});
