import { screen } from "@testing-library/react";
import { expect, test } from "vitest";

import CountrySelect from "~/component/input/CountrySelect";

import { renderComponent } from "../../test-utils";

test("renders the country combobox", () => {
	renderComponent(<CountrySelect countries={["ESP", "FRA"]} />);
	expect(screen.getByRole("combobox")).toHaveAttribute(
		"autocomplete",
		"country-name"
	);
});
