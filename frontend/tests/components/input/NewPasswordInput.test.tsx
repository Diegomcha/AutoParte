import { screen } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";

import NewPasswordInput, {
	isPasswordStrongEnough
} from "~/components/input/NewPasswordInput";
import PasswordService from "~/services/PasswordService";

import { renderComponent } from "../../test-utils";

afterEach(() => {
	vi.restoreAllMocks();
});

test("renders password input and validates short passwords", () => {
	renderComponent(<NewPasswordInput aria-label="password" />);
	expect(screen.getByLabelText("password")).toBeInTheDocument();
	expect(isPasswordStrongEnough()("short")).toBeTruthy();
});

test("validates empty and weak passwords", () => {
	expect(isPasswordStrongEnough()()).toBe("La contraseña no puede estar vacía");

	const originalCheck = PasswordService.check.bind(PasswordService);
	vi.spyOn(PasswordService, "check").mockImplementation((password) => ({
		...originalCheck(password),
		score: 0
	}));
	expect(isPasswordStrongEnough()("long enough")).toBe(
		"La contraseña es demasiado débil"
	);
});
