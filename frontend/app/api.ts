import { QueryClient } from '@tanstack/react-query';
import createFetchClient from 'openapi-fetch';
import { data } from 'react-router';
import type { paths } from './@types/api';
import type { FetchResponse } from 'openapi-fetch';

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

export function throwErrors<T extends Record<string | number, unknown>>({
	data: resData,
	error,
	response,
}: FetchResponse<T, unknown, '*/*'>) {
	// Handle API errors in a standardized way
	if (!response.ok) {
		// Handle 404 error
		if (response.status.toString().startsWith('4'))
			// eslint-disable-next-line @typescript-eslint/only-throw-error
			throw data(response.statusText, { status: response.status });

		throw new Error(
			`APIError: ${response.status.toString()} [${response.statusText}]`,
			{
				cause: error,
			}
		);
	}

	// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
	return resData!;
}

export const queryClient = new QueryClient();

export default api;
