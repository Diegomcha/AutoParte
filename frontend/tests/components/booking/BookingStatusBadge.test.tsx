import { screen } from "@testing-library/react";
import { expect, test } from "vitest";

import BookingStatusBadge from "~/components/booking/BookingStatusBadge";

import { renderComponent } from "../../test-utils";

test("renders a booking status", () => {
	renderComponent(<BookingStatusBadge status="CONFIRMED" />);
	expect(screen.getByText("Confirmada")).toBeInTheDocument();
});
