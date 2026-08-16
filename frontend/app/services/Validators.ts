import { validate as uuidValidate } from 'uuid';
import type { ErrorResponse } from 'react-router';

class Validators {
	/**
	 * Validates that the provided strings are valid UUIDs.
	 * @param uuids The strings to validate as UUIDs.
	 * @throws ValidationErrorResponse if any of the provided strings is not a valid UUID.
	 */
	validateUuids(...uuids: string[]): void {
		for (const uuid of uuids) {
			if (!uuidValidate(uuid)) {
				throw new ValidationErrorResponse(`Not valid UUID: ${uuid}`);
			}
		}
	}

	/**
	 * Creates a ValidationErrorResponse with the provided detail message.
	 * @param detail The detail message for the validation error.
	 * @throws ValidationErrorResponse with the provided detail message.
	 */
	throwValidationErrorResponse(detail: string): ValidationErrorResponse {
		throw new ValidationErrorResponse(detail);
	}
}

export default new Validators();

export class ValidationErrorResponse extends Error implements ErrorResponse {
	/**
	 * Whether the given error is an instance of ValidationErrorResponse.
	 * @param error The object to check.
	 * @returns  True if the error is an instance of ValidationErrorResponse, false otherwise.
	 */
	static isValidationErrorResponse(
		error: unknown
	): error is ValidationErrorResponse {
		return error instanceof ValidationErrorResponse;
	}

	readonly status = 400;
	readonly statusText = 'Bad Request';
	readonly data: string;

	constructor(detail: string) {
		super(`ValidationErrorResponse: ${detail}`);
		this.data = detail;
	}
}
