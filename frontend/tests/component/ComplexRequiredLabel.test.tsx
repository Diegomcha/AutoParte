import { expect, test } from "vitest";

import ComplexRequiredLabel from "~/component/ComplexRequiredLabel";

import { renderComponent } from "../test-utils";

test("renders the required marker", () => {
	renderComponent(<ComplexRequiredLabel action="confirm" />);
	expect(document.querySelector("span")).toHaveTextContent("*");
});
