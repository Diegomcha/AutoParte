import { expect, test } from "vitest";

import TimeService from "~/services/TimeService";

test("supports inclusive date-range checks", () => {
	const start = TimeService("2026-01-10");
	const end = TimeService("2026-01-12");

	expect(start.isBetween(start, end, "day", "[]")).toBe(true);
	expect(TimeService("2026-01-11").isBetween(start, end, "day", "[]")).toBe(
		true
	);
	expect(TimeService("2026-01-13").isBetween(start, end, "day", "[]")).toBe(
		false
	);
});

test("formats dates deterministically when given an explicit format", () => {
	expect(TimeService("2026-01-10").format("YYYY-MM-DD")).toBe("2026-01-10");
});
