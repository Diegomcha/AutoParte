import { redirect } from "react-router";

import { afterEach, expect, test, vi } from "vitest";

import { clientLoader as authLoader } from "~/routes/auth/index";
import { clientLoader as loginLoader } from "~/routes/auth/login";
import AuthService from "~/services/AuthService";

afterEach(() => {
	vi.restoreAllMocks();
});

test("auth index redirects to login", () => {
	const response = authLoader();

	expect(response).toEqual(redirect("/auth/login"));
});

test("login loader returns the encoded destination for anonymous users", async () => {
	vi.spyOn(AuthService, "isAuthenticated").mockResolvedValue(false);

	await expect(
		loginLoader({
			request: new Request("https://example.test/auth/login?redirect=%2Fadmin")
		} as never)
	).resolves.toEqual({ successRedirectPath: "/admin" });
});

test("login loader redirects authenticated users", async () => {
	vi.spyOn(AuthService, "isAuthenticated").mockResolvedValue(true);

	await expect(
		loginLoader({
			request: new Request("https://example.test/auth/login?redirect=%2Fadmin")
		} as never)
	).resolves.toEqual(redirect("/admin"));
});
