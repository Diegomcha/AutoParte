import { expect, test } from "vitest";

import { clientLoader } from "~/routes/bookings/export/index";

test("export index redirects to the default export", () => {
	const response = clientLoader();

	expect(response.status).toBe(302);
	expect(response.headers.get("Location")).toBe("..");
});
