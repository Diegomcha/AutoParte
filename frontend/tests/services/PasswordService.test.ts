import { expect, test, vi } from "vitest";

import PasswordService from "~/services/PasswordService";

vi.unmock("~/services/PasswordService");

test("returns password strength metrics", () => {
	const result = PasswordService.check("correct horse battery staple");

	expect(result.score).toBeGreaterThanOrEqual(0);
	expect(result.score).toBeLessThanOrEqual(4);
	expect(result.feedback).toBeDefined();
});
