import { expect, test } from "vitest";

import { clientLoader as authLoader } from "~/routes/auth/index";
import { clientLoader as logoutLoader } from "~/routes/auth/logout";

test("auth entry redirects to login", () => {
	const response = authLoader();

	expect(response.status).toBe(302);
	expect(response.headers.get("Location")).toBe("/auth/login");
});

test("logout route redirects to the login page", async () => {
	const response = await logoutLoader();

	expect(response.status).toBe(302);
	expect(response.headers.get("Location")).toBe("/auth/login");
});
