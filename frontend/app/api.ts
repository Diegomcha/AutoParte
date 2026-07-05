import { QueryClient } from '@tanstack/react-query';
import createFetchClient from 'openapi-fetch';
import type { paths, ProblemDetail } from './@types/api';
import type { FetchResponse } from 'openapi-fetch';
import type { ErrorResponse } from 'react-router';

const api = createFetchClient<paths, '*/*'>({
	querySerializer: {
		object: {
			style: 'form',
			explode: true,
		},
	},
	headers: {
		'X-Requested-With': 'XMLHttpRequest',
		Accept: 'application/json',
	},
	credentials: 'include',
});

// Middleware to include CSRF token from cookies in the request headers
api.use({
	async onRequest({ request }) {
		const csrfToken = await cookieStore.get('XSRF-TOKEN');
		if (csrfToken?.value) request.headers.set('X-XSRF-TOKEN', csrfToken.value);
	},
});

// Exports

/**
 * Handles API responses by throwing an ApiErrorResponse for non-OK responses, or returning the response data for OK responses.
 * @template T The type of the response data.
 * @param param0 The FetchResponse object containing the response data, error, and response.
 * @returns The response data if the response is OK.
 * @throws ApiErrorResponse if the response is not OK.
 */
export function throwErrors<T extends Record<string | number, unknown>>({
	data: resData,
	error,
	response,
}: FetchResponse<T, unknown, '*/*'>) {
	// Handle API errors in a standardized way
	if (!response.ok) throw new ApiErrorResponse(response, error);

	// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
	return resData!;
}

export default api;

export const queryClient = new QueryClient({
	defaultOptions: {
		queries: {
			retry: (failureCount, error) => {
				if (error.status >= 400 && error.status < 500) return false; // Don't retry for client errors
				return failureCount < 3; // Retry up to 3 times for server errors
			},
		},
	},
});

export class ApiErrorResponse extends Error implements ErrorResponse {
	/**
	 * Whether the given error is an instance of ApiErrorResponse.
	 * @param error The object to check.
	 * @returns  True if the error is an instance of ApiErrorResponse, false otherwise.
	 */
	static isApiErrorResponse(error: unknown): error is ApiErrorResponse {
		return error instanceof ApiErrorResponse;
	}

	readonly status: number;
	readonly statusText: string;
	readonly data: ProblemDetail | undefined;

	constructor(response: Response, error?: ProblemDetail) {
		super(
			`ApiErrorResponse: [${String(response.status)} ${response.statusText}] ${error?.title ?? ''}`
		);

		this.status = response.status;
		this.statusText = response.statusText;
		this.data = error;
	}
}
