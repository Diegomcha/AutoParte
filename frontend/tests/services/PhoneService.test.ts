import { expect, test } from "vitest";

import PhoneService from "~/services/PhoneService";

test("formats valid phone numbers internationally", () => {
	expect(PhoneService.format("+34912345678")).toBe("+34 912 34 56 78");
});

test("rejects invalid phone numbers", () => {
	expect(() => PhoneService.format("invalid")).toThrow(
		"Invalid phone number: invalid"
	);
});
