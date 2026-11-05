import React from "react";

import { cleanup, fireEvent, screen } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";

import AddressSelect from "~/component/input/AddressSelect";

import { renderComponent } from "../../test-utils";

afterEach(cleanup);

vi.mock("@mantine/core", async () => {
	const actual =
		await vi.importActual<typeof import("@mantine/core")>("@mantine/core");
	return {
		...actual,
		Select: (props: {
			label?: string;
			onChange?: (value: string | null) => void;
			data?: unknown[];
		}) =>
			React.createElement(
				"div",
				null,
				React.createElement("input", { "aria-label": props.label }),
				React.createElement(
					"button",
					{ onClick: () => props.onChange?.("$new") },
					"new address"
				)
			)
	};
});

test("renders an address selector without requiring API data", () => {
	renderComponent(
		<AddressSelect
			accommodationId="accommodation-1"
			bookingId="booking-1"
			label="Address"
		/>
	);

	expect(screen.getByRole("textbox", { name: "Address" })).toBeInTheDocument();
});

test("opens the new address form from the special option", () => {
	renderComponent(
		<AddressSelect
			accommodationId="accommodation-1"
			bookingId="booking-1"
			label="Address"
		/>
	);

	fireEvent.click(screen.getByRole("button", { name: "new address" }));

	expect(document.body).toHaveAttribute("data-scroll-locked", "1");
});
