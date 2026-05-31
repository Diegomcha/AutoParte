// Ensure the user is authenticated before allowing access to any protected routes.
// This should only be required in dev mode, as in production the server will handle route protection.
export { clientLoader } from './auth.logout';

export default function Dashboard() {
	return (
		<>
			{/* TODO: Dashboard */}
			<p>TODO: Dashboard</p>
		</>
	);
}
