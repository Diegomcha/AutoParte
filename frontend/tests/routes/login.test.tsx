import { cleanup, fireEvent, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, expect, test, vi } from "vitest";

import LoginPage from "~/routes/auth/login";
import AuthService from "~/services/AuthService";

import { renderComponent } from "../test-utils";

const mocks = vi.hoisted(() => ({
	mutate: vi.fn(),
	navigate: vi.fn()
}));

vi.mock("@tanstack/react-query", async () => ({
	...(await vi.importActual<typeof import("@tanstack/react-query")>(
		"@tanstack/react-query"
	)),
	useMutation: () => ({
		mutate: mocks.mutate,
		isPending: false
	})
}));

vi.mock("react-router", async () => ({
	...(await vi.importActual<typeof import("react-router")>("react-router")),
	useNavigate: () => mocks.navigate
}));

beforeEach(() => {
	mocks.mutate.mockReset();
	mocks.navigate.mockReset();
});

afterEach(() => {
	cleanup();
	vi.restoreAllMocks();
});

function renderLogin() {
	const props = {
		loaderData: { successRedirectPath: "/admin" }
	} as Parameters<typeof LoginPage>[0];
	renderComponent(<LoginPage {...props} />);
}

test("shows required-field errors without submitting incomplete credentials", async () => {
	renderLogin();

	fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

	expect(
		await screen.findByText("El nombre de usuario es obligatorio")
	).toBeInTheDocument();
	expect(
		await screen.findByText("La contraseña es obligatoria")
	).toBeInTheDocument();
	expect(mocks.mutate).not.toHaveBeenCalled();
});

test("navigates to the requested destination after successful login", async () => {
	mocks.mutate.mockImplementation(
		(_credentials: unknown, options: { onSuccess: (result: true) => void }) => {
			options.onSuccess(true);
		}
	);
	renderLogin();

	fireEvent.change(
		screen.getByLabelText("Nombre de usuario", { selector: "input" }),
		{ target: { value: "ada" } }
	);
	fireEvent.change(screen.getByLabelText("Contraseña", { selector: "input" }), {
		target: { value: "secret" }
	});
	fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

	await waitFor(() => {
		expect(mocks.navigate).toHaveBeenCalledWith("/admin");
	});
});

test("navigates to password creation when credentials are expired", async () => {
	mocks.mutate.mockImplementation(
		(
			_credentials: { username: string; password: string },
			options: { onSuccess: (result: string) => void }
		) => {
			options.onSuccess("USER_CREDENTIALS_EXPIRED");
		}
	);
	vi.spyOn(AuthService, "getCreatePasswordRedirectionPath").mockReturnValue(
		"/auth/create-password"
	);
	renderLogin();

	fireEvent.change(
		screen.getByLabelText("Nombre de usuario", { selector: "input" }),
		{ target: { value: "ada" } }
	);
	fireEvent.change(screen.getByLabelText("Contraseña", { selector: "input" }), {
		target: { value: "secret" }
	});
	fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

	await waitFor(() => {
		expect(mocks.navigate).toHaveBeenCalledWith("/auth/create-password");
	});
});

test("shows a backend error on the password field", async () => {
	mocks.mutate.mockImplementation(
		(
			_credentials: unknown,
			options: { onSuccess: (result: string) => void }
		) => {
			options.onSuccess("BAD_CREDENTIALS");
		}
	);
	renderLogin();

	fireEvent.change(
		screen.getByLabelText("Nombre de usuario", { selector: "input" }),
		{ target: { value: "ada" } }
	);
	fireEvent.change(screen.getByLabelText("Contraseña", { selector: "input" }), {
		target: { value: "wrong" }
	});
	fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

	await waitFor(() => {
		expect(
			screen.getByText("Nombre de usuario o contraseña incorrectos")
		).toBeInTheDocument();
	});
	expect(mocks.navigate).not.toHaveBeenCalled();
});
