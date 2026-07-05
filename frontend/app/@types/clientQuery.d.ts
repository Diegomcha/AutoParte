import '@tanstack/react-query';
import type { ApiErrorResponse } from '~/api'; // Tu interfaz

declare module '@tanstack/react-query' {
	interface Register {
		defaultError: ApiErrorResponse;
	}
}
