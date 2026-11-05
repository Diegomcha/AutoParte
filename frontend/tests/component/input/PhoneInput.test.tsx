import { screen } from "@testing-library/react";
import { expect, test } from "vitest";

import PhoneInput, { isValidPhoneNumber } from "~/component/input/PhoneInput";

import { renderComponent } from "../../test-utils";

test("renders phone input and validates invalid values", () => {
	renderComponent(<PhoneInput aria-label="phone" />);
	expect(screen.getByLabelText("phone")).toBeInTheDocument();
	expect(isValidPhoneNumber()("invalid")).toBeTruthy();
});
