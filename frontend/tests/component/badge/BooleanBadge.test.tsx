import { screen } from "@testing-library/react";
import { expect, test } from "vitest";

import BooleanBadge from "~/component/badge/BooleanBadge";

import { renderComponent } from "../../test-utils";

test("renders a boolean state", () => {
	renderComponent(<BooleanBadge value />);
	expect(screen.getByText("Sí")).toBeInTheDocument();
});
