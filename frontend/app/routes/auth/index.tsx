import { redirect } from 'react-router';

export function clientLoader() {
	// Redirect to the login page by default when accessing the auth route.
	return redirect('/auth/login');
}
