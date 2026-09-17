import { executeMutation, queryFactory } from "~/services/Api";
import AuthService from "~/services/AuthService";

export async function clientLoader() {
	// Perform logout if the user is authenticated
	if (await AuthService.isAuthenticated(true))
		await executeMutation(queryFactory.auth.logout(), undefined);

	return AuthService.getLoginRedirection();
}
