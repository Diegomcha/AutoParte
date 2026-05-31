import createFetchClient from 'openapi-fetch';
import type { paths } from './@types/api';

const client = createFetchClient<paths, '*/*'>({
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
client.use({
	async onRequest({ request }) {
		const csrfToken = await cookieStore.get('XSRF-TOKEN');
		if (csrfToken?.value) request.headers.set('X-XSRF-TOKEN', csrfToken.value);
	},
});

export default client;
