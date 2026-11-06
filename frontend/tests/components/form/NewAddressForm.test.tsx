import { cleanup, screen } from "@testing-library/react";
import { afterEach, expect, test } from "vitest";

import NewAddressForm from "~/components/form/NewAddressForm";

import { renderComponent } from "../../test-utils";

afterEach(cleanup);

test("renders the address form and keeps submission disabled until changes", () => {
	renderComponent(<NewAddressForm opened onClose={() => undefined} />);

	expect(screen.getByRole("dialog")).toBeInTheDocument();
	expect(screen.getByRole("button", { name: /añadir/i })).toBeDisabled();
});
