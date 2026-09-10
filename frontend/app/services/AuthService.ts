import { redirect } from "react-router";

import { executeMutation, queryClient, queryFactory } from "./Api";

import type { AccountDto } from "~/@types/api";

class AuthService {
	/**
	 * Checks if the currently logged in user has the "ROLE_ADMIN" authority.
	 * @param fresh If true, forces a fresh check by fetching the user data from the server.
	 * @returns True if the user has the "ROLE_ADMIN" authority, false otherwise.
	 */
	async isAdmin(fresh = false): Promise<boolean> {
		const user = await this.getLoggedInUser(fresh);
		return user?.roles.includes("ROLE_ADMIN") ?? false;
	}

	/**
	 * Checks if the user is currently authenticated.
	 * @param fresh If true, forces a fresh check by fetching the user data from the server.
	 * @returns True if the user is authenticated, false otherwise.
	 */
	async isAuthenticated(fresh = false): Promise<boolean> {
		return (await this.getLoggedInUser(fresh)) != null;
	}

	/**
	 * Gets the currently logged in user.
	 * @param fresh If true, forces a fresh fetch by fetching the user data from the server.
	 * @returns The logged-in user if available, or null if not logged-in.
	 */
	async getLoggedInUser(fresh = false): Promise<AccountDto | null> {
		if (fresh) await queryClient.invalidateQueries(queryFactory.auth.me());
		return await queryClient.query(queryFactory.auth.me());
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
		return await executeMutation(queryFactory.auth.login(), credentials);
	}

	/**
	 * Performs logout for the current user.
	 * @returns True if logout was successful, false otherwise.
	 */
	async performLogout() {
		return await executeMutation(queryFactory.auth.logout(), undefined);
	}

	/**
	 * Gets the redirection to use based on the "redirect" query parameter in the request URL, or defaults to "/".
	 * @param request The request containing the URL with potential "redirect" query parameter.
	 * @returns A redirect response to the specified URL or "/" if not specified.
	 */
	getSuccessRedirection(request: Request): ReturnType<typeof redirect> {
		return redirect(new URL(request.url).searchParams.get("redirect") ?? "/");
	}

	/**
	 * Gets the redirection to the login page with a "redirect" query parameter set to the current page, so that after successful login, the user can be redirected back to where they were.
	 * @param request The request containing the URL of the current page to redirect back to after login.
	 * @returns A redirect response to the login page with the appropriate "redirect" query parameter.
	 */
	getLoginRedirection(request?: Request): ReturnType<typeof redirect> {
		const redirectPath = request ? new URL(request.url).pathname : undefined;

		return redirect(this.getLoginRedirectionPath(redirectPath));
	}

	/**
	 * Gets the URL path to redirect to the login page with an optional "redirect" query parameter.
	 * @param path Path to redirect to after successful login. If not provided, defaults to the root path ("/").
	 * @returns The URL path to redirect to the login page with an optional "redirect" query parameter.
	 */
	getLoginRedirectionPath(path?: string): string {
		return path
			? "/auth/login?redirect=" + encodeURIComponent(path)
			: "/auth/login";
	}
}

export default new AuthService();
