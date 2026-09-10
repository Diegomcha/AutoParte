import AuthService from "../../services/AuthService";

export async function clientLoader() {
	// Perform logout if the user is authenticated
	if (await AuthService.isAuthenticated(true))
		await AuthService.performLogout();

	return AuthService.getLoginRedirection();
}
