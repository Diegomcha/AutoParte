import api, { throwErrors } from '~/api';
import { redirect } from 'react-router';
import type { AccountDto } from '~/@types/api';

const USER_KEY = 'user';

class AuthService {
	/**
	 * Checks if the currently logged in user has the "ROLE_ADMIN" authority.
	 * @returns True if the user has the "ROLE_ADMIN" authority, false otherwise.
	 */
	async isAdmin(): Promise<boolean> {
		const user = await this.getLoggedInUser();
		return user?.roles.includes('ROLE_ADMIN') ?? false;
	}

	/**
	 * Checks if the user is currently authenticated.
	 * @returns True if the user is authenticated, false otherwise.
	 */
	async isAuthenticated(): Promise<boolean> {
		return (await this.getLoggedInUser()) != null;
	}

	/**
	 * Gets the currently logged in user.
	 * First checks sessionStorage, and if not found, makes an API call to try and retrieve the user.
	 * @returns The logged-in user if available, or null if not logged in.
	 */
	async getLoggedInUser(): Promise<AccountDto | null> {
		return (
			this.getLoggedInUserFromCache() ?? (await this.getLoggedInUserFromApi())
		);
	}

	/**
	 * Gets the currently logged in user from sessionStorage if available.
	 * @returns  The logged-in user if available in sessionStorage, or null if not found.
	 */
	getLoggedInUserFromCache(): AccountDto | null {
		const storedUser = sessionStorage.getItem(USER_KEY);
		return storedUser ? (JSON.parse(storedUser) as AccountDto) : null;
	}

	/**
	 * Gets the currently logged in user from the API and caches it in sessionStorage.
	 * @returns The logged-in user if available from the API, or null if not logged in.
	 */
	async getLoggedInUserFromApi(): Promise<AccountDto | null> {
		// Get user
		const user = throwErrors(await api.GET('/api/auth/me')) as
			| AccountDto
			| undefined;

		// Cache the user in sessionStorage for future retrieval
		if (user) sessionStorage.setItem(USER_KEY, JSON.stringify(user));
		else sessionStorage.removeItem(USER_KEY);

		return user ?? null;
	}

	/**
	 * Refreshes the cached logged-in user by making an API call to retrieve the latest user information and updating sessionStorage.
	 */
	async refreshLoggedInUser() {
		await this.getLoggedInUserFromApi();
	}

	/**
	 * Performs login with the given credentials.
	 * @param credentials The login credentials, including username, password, and an optional rememberMe flag.
	 * @returns True if login was successful, false otherwise.
	 */
	async performLogin(credentials: {
		username: string;
		password: string;
		rememberMe?: boolean;
	}): Promise<boolean> {
		const req = await api.POST('/api/auth/login', {
			body: credentials,
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
		});

		// Handle 401 Unauthorized response (invalid credentials)
		if (req.response.status === 401) return false;
		// Handle other non-OK responses
		throwErrors(req);

		return true;
	}

	/**
	 * Performs logout for the current user.
	 * @returns True if logout was successful, false otherwise.
	 */
	async performLogout() {
		sessionStorage.removeItem(USER_KEY);

		const req = await api.POST('/api/auth/logout');
		// Handle 401 Unauthorized response (user not logged in)
		if (req.response.status === 401) return false;
		// Handle other non-OK responses
		throwErrors(req);

		return true;
	}

	/**
	 * Gets the redirection to use based on the "redirectTo" query parameter in the request URL, or defaults to "/".
	 * @param request The request containing the URL with potential "redirectTo" query parameter.
	 * @returns A redirect response to the specified URL or "/" if not specified.
	 */
	getSuccessRedirection(request: Request): ReturnType<typeof redirect> {
		return redirect(new URL(request.url).searchParams.get('redirect') ?? '/');
	}

	/**
	 * Gets the redirection to the login page with a "redirectTo" query parameter set to the current page, so that after successful login, the user can be redirected back to where they were.
	 * @param request The request containing the URL of the current page to redirect back to after login.
	 * @returns A redirect response to the login page with the appropriate "redirectTo" query parameter.
	 */
	getLoginRedirection(request: Request): ReturnType<typeof redirect> {
		return redirect(
			'/auth/login?redirectTo=' +
				encodeURIComponent(new URL(request.url).pathname)
		);
	}
}

export default new AuthService();
