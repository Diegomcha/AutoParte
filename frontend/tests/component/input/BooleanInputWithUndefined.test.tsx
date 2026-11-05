import { screen } from "@testing-library/react";
import { expect, test } from "vitest";

import BooleanInputWithUndefined from "~/component/input/BooleanInputWithUndefined";

import { renderComponent } from "../../test-utils";

test("renders all boolean choices", () => {
	renderComponent(<BooleanInputWithUndefined aria-label="value" />);
	expect(screen.getAllByRole("radio")).toHaveLength(3);
});
