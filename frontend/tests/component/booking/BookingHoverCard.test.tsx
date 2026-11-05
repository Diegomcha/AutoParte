import { screen } from "@testing-library/react";
import { expect, test } from "vitest";

import BookingHoverCard from "~/component/booking/BookingHoverCard";

import { renderComponent } from "../../test-utils";

test("renders booking summary", () => {
	renderComponent(
		<BookingHoverCard
			booking={
				{
					status: "CONFIRMED",
					startTime: "2025-01-01",
					endTime: "2025-01-02",
					numberOfPeople: 2,
					holderName: "Ada"
				} as never
			}
		/>
	);
	expect(screen.getByText(/2 pax\.\s*-\s*Ada/)).toBeInTheDocument();
	expect(screen.getByText(/2025/)).toBeInTheDocument();
});
