import { cleanup, fireEvent, screen } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";

import EmployeeCredsModal from "~/components/admin/employees/EmployeeCredsModal";

import { renderComponent } from "../../../test-utils";

afterEach(cleanup);

test("shows generated credentials", () => {
	renderComponent(
		<EmployeeCredsModal
			opened
			onClose={vi.fn()}
			description="Save these values"
			creds={{ email: "a@example.com", password: "secret" } as never}
		/>
	);
	expect(screen.getByDisplayValue("a@example.com")).toBeInTheDocument();
	expect(screen.getByDisplayValue("secret")).toBeInTheDocument();
});

test("selects credential values when clicked", () => {
	renderComponent(
		<EmployeeCredsModal
			opened
			onClose={vi.fn()}
			description="Save these values"
			creds={{ email: "a@example.com", password: "secret" } as never}
		/>
	);

	const email = screen.getByDisplayValue<HTMLInputElement>("a@example.com");
	const password = screen.getByDisplayValue<HTMLInputElement>("secret");
	const select = vi.spyOn(email, "select");
	const selectPassword = vi.spyOn(password, "select");

	fireEvent.click(email);
	fireEvent.click(password);

	expect(select).toHaveBeenCalled();
	expect(selectPassword).toHaveBeenCalled();
});
