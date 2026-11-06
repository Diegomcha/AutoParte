import { screen } from "@testing-library/react";
import { expect, test } from "vitest";

import WifiBadge from "~/components/badge/WifiBadge";

import { renderComponent } from "../../test-utils";

test("renders the wifi state", () => {
	renderComponent(<WifiBadge value={false} />);
	expect(screen.getByText("No")).toBeInTheDocument();
});
