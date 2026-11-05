import { cleanup, screen } from "@testing-library/react";
import { afterEach, expect, test } from "vitest";

import EnablementBadge from "~/component/badge/EnablementBadge";

import { renderComponent } from "../../test-utils";

afterEach(cleanup);

test("renders enabled state", () => {
	renderComponent(<EnablementBadge enabled />);
	expect(screen.getByText("Habilitado")).toBeInTheDocument();
});

test("renders disabled state with a tooltip date", () => {
	renderComponent(
		<EnablementBadge enabled={false} disabledAt={new Date("2025-01-01")} />
	);

	expect(screen.getByText("Deshabilitado")).toBeInTheDocument();
});
