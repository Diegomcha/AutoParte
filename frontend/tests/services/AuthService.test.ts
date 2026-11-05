import { expect, test, vi } from "vitest";

import { queryClient } from "~/services/Api";
import AuthService from "~/services/AuthService";

test("builds a login redirect with the current path", () => {
	expect(AuthService.getLoginRedirectionPath("/admin/employees?id=1")).toBe(
		"/auth/login?redirect=%2Fadmin%2Femployees%3Fid%3D1"
	);
});

test("preserves successful redirect query parameters", () => {
	expect(
		AuthService.getSuccessRedirectionPath(
			"https://example.test/auth/login?redirect=%2Fadmin"
		)
	).toBe("/admin");
	expect(
		AuthService.getSuccessRedirectionPath("https://example.test/auth/login")
	).toBe("/");
});

test("encodes credentials in the create-password redirect", () => {
	expect(
		AuthService.getCreatePasswordRedirectionPath(
			{
				username: "user@example.test",
				password: "p&ss word",
				rememberMe: false
			},
			"/admin"
		)
	).toBe(
		"/auth/create-password?username=user%40example.test&currentPassword=p%26ss%20word&redirect=%2Fadmin"
	);
});

test("checks authentication and admin role from the cached account", async () => {
	const query = vi.spyOn(queryClient, "query");
	query.mockResolvedValue({
		roles: ["ROLE_ADMIN"],
		username: "admin"
	});

	await expect(AuthService.isAuthenticated()).resolves.toBe(true);
	await expect(AuthService.isAdmin()).resolves.toBe(true);

	query.mockResolvedValue(null);
	await expect(AuthService.isAuthenticated()).resolves.toBe(false);
	await expect(AuthService.isAdmin()).resolves.toBe(false);
});
