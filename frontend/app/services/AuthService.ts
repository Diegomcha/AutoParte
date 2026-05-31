import api from '~/api';
import { redirect } from 'react-router';
import type { UserDetails } from '~/@types/api';

const USER_KEY = 'user';

class AuthService {
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
	async getLoggedInUser(): Promise<UserDetails | null> {
		// First check sessionStorage for cached user
		const storedUser = sessionStorage.getItem(USER_KEY);
		let user = storedUser ? (JSON.parse(storedUser) as UserDetails) : null;

		// If not found in sessionStorage, make API call to get logged in user & cache it in sessionStorage
		if (!user) {
			const { data } = await api.GET('/api/auth/me');
			user = data ?? null;
			if (user) sessionStorage.setItem(USER_KEY, JSON.stringify(user));
		}

		return user;
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
		return (
			await api.POST('/api/auth/login', {
				body: credentials,
				headers: {
					'Content-Type': 'application/x-www-form-urlencoded',
				},
			})
		).response.ok;
	}

	/**
	 * Performs logout for the current user.
	 * @returns True if logout was successful, false otherwise.
	 */
	async performLogout() {
		sessionStorage.removeItem(USER_KEY);
		return (await api.POST('/api/auth/logout')).response.ok;
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
