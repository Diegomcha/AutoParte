import { expect, test } from "vitest";

import ComplexRequiredLabel from "~/components/ComplexRequiredLabel";

import { renderComponent } from "../test-utils";

test("renders the required marker", () => {
	renderComponent(<ComplexRequiredLabel action="confirm" />);
	expect(document.querySelector("span")).toHaveTextContent("*");
});
