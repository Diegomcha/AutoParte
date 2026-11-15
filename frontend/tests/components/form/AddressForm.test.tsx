import { cleanup, screen } from "@testing-library/react";
import { afterEach, expect, test } from "vitest";

import AddressForm from "~/components/form/AddressForm";

import { renderComponent } from "../../test-utils";

afterEach(cleanup);

test("renders the address form and keeps submission disabled until changes", () => {
	renderComponent(<AddressForm opened onClose={() => undefined} />);

	expect(screen.getByRole("dialog")).toBeInTheDocument();
	expect(screen.getByRole("button", { name: /añadir/i })).toBeDisabled();
});
