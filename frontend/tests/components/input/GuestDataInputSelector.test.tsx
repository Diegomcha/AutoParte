import { fireEvent, screen } from "@testing-library/react";
import { expect, test, vi } from "vitest";

import GuestDataInputSelector from "~/components/input/GuestDataInputSelector";

import { renderComponent } from "../../test-utils";

test("reports manual selection", () => {
	const onSelection = vi.fn();
	renderComponent(<GuestDataInputSelector onSelection={onSelection} />);
	fireEvent.click(screen.getByRole("button", { name: /manual/i }));
	expect(onSelection).toHaveBeenCalledWith("manual");
});
