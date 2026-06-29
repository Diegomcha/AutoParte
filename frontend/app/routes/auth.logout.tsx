import AuthService from '../services/AuthService';
import type { Route } from './+types/auth.logout';

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
	// Ensure the user is authenticated before allowing access to the logout page.
	if (!(await AuthService.isAuthenticated()))
		return AuthService.getLoginRedirection(request);

	// Perform logout and redirect to the appropriate page based on the result.
	return (await AuthService.performLogout())
		? AuthService.getSuccessRedirection(request)
		: AuthService.getLoginRedirection(request);
}
