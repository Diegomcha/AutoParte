import '@tanstack/react-query';
import type { ApiErrorResponse } from '~/services/Api'; // Tu interfaz

declare module '@tanstack/react-query' {
	interface Register {
		defaultError: ApiErrorResponse;
	}
}
