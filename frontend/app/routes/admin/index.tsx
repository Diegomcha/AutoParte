import { redirect } from 'react-router';

export function loader() {
	// Redirect to the employees page by default when accessing the admin route.
	return redirect('/admin/employees');
}
