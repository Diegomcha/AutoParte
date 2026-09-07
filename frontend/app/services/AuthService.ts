import { redirect } from "react-router";

import { executeMutation, queryClient, queryFactory } from "./Api";

import type { AccountDto } from "~/@types/api";

class AuthService {
	/**
	 * Checks if the currently logged in user has the "ROLE_ADMIN" authority.
	 * @returns True if the user has the "ROLE_ADMIN" authority, false otherwise.
	 */
	async isAdmin(): Promise<boolean> {
		const user = await this.getLoggedInUser();
		return user?.roles.includes("ROLE_ADMIN") ?? false;
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
	 * @returns The logged-in user if available, or null if not logged-in.
	 */
	async getLoggedInUser(): Promise<AccountDto | null> {
		return await queryClient.query(queryFactory.auth.me());
	}

	/**
	 * Refreshes the cached logged-in user by making an API call to retrieve the latest user information and updating sessionStorage.
	 */
	async refreshLoggedInUser() {
		await queryClient.invalidateQueries(queryFactory.auth.me());
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
	 * Gets the redirection to use based on the "redirectTo" query parameter in the request URL, or defaults to "/".
	 * @param request The request containing the URL with potential "redirectTo" query parameter.
	 * @returns A redirect response to the specified URL or "/" if not specified.
	 */
	getSuccessRedirection(request: Request): ReturnType<typeof redirect> {
		return redirect(new URL(request.url).searchParams.get("redirect") ?? "/");
	}

	/**
	 * Gets the redirection to the login page with a "redirectTo" query parameter set to the current page, so that after successful login, the user can be redirected back to where they were.
	 * @param request The request containing the URL of the current page to redirect back to after login.
	 * @returns A redirect response to the login page with the appropriate "redirectTo" query parameter.
	 */
	getLoginRedirection(request: Request): ReturnType<typeof redirect> {
		return redirect(
			"/auth/login?redirectTo=" +
				encodeURIComponent(new URL(request.url).pathname)
		);
	}
}

export default new AuthService();
