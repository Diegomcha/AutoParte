import { expect, test } from "vitest";

import Validators, { ValidationErrorResponse } from "~/services/Validators";

test("accepts valid UUIDs", () => {
	expect(() => {
		Validators.validateUuids("123e4567-e89b-12d3-a456-426614174000");
	}).not.toThrow();
});

test("rejects invalid UUIDs with a frontend validation response", () => {
	expect(() => {
		Validators.validateUuids("not-a-uuid");
	}).toThrow("Not valid UUID: not-a-uuid");

	try {
		Validators.validateUuids("not-a-uuid");
	} catch (error) {
		expect(error).toBeInstanceOf(ValidationErrorResponse);
		expect(error).toMatchObject({
			status: 400,
			statusText: "Bad Request",
			data: "Not valid UUID: not-a-uuid"
		});
	}
});

test("exposes a type guard for validation responses", () => {
	const error = new ValidationErrorResponse("invalid input");

	expect(ValidationErrorResponse.isValidationErrorResponse(error)).toBe(true);
	expect(ValidationErrorResponse.isValidationErrorResponse(new Error())).toBe(
		false
	);
});
