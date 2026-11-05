import React from "react";

import { cleanup, screen } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";

import {
	clientLoader,
	default as ProtectedAdminLayout
} from "~/routes/admin/layout";
import AuthService from "~/services/AuthService";

import { renderComponent } from "../test-utils";

vi.mock("react-router", async () => ({
	...(await vi.importActual<typeof import("react-router")>("react-router")),
	NavLink: (props: {
		to: string;
		label?: string;
		children?: React.ReactNode;
	}) =>
		React.createElement("a", { href: props.to }, props.label, props.children),
	Outlet: () => React.createElement("div", { "data-testid": "outlet" })
}));

afterEach(cleanup);

test("admin loader redirects anonymous users to login", async () => {
	vi.spyOn(AuthService, "isAuthenticated").mockResolvedValue(false);

	const result = await clientLoader({
		request: new Request("https://example.test/admin/employees")
	} as never);

	expect(result).toEqual(
		Response.redirect(
			"https://example.test/auth/login?redirect=%2Fadmin%2Femployees"
		)
	);
});

test("admin loader redirects authenticated non-admin users home", async () => {
	vi.spyOn(AuthService, "isAuthenticated").mockResolvedValue(true);
	vi.spyOn(AuthService, "isAdmin").mockResolvedValue(false);

	const result = await clientLoader({
		request: new Request("https://example.test/admin")
	} as never);

	expect(result).toEqual(Response.redirect("https://example.test/"));
});

test("admin loader allows authenticated administrators", async () => {
	vi.spyOn(AuthService, "isAuthenticated").mockResolvedValue(true);
	vi.spyOn(AuthService, "isAdmin").mockResolvedValue(true);

	await expect(
		clientLoader({
			request: new Request("https://example.test/admin")
		} as never)
	).resolves.toBeUndefined();
});

test("renders the protected admin navigation", () => {
	renderComponent(React.createElement(ProtectedAdminLayout));

	expect(screen.getByText("Empleados")).toBeInTheDocument();
	expect(screen.getByText("Alojamientos")).toBeInTheDocument();
	expect(screen.getByText("Configuración")).toBeInTheDocument();
	expect(screen.getByTestId("outlet")).toBeInTheDocument();
});
