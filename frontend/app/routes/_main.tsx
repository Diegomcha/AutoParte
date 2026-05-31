import { AppShell, Box, NavLink } from '@mantine/core';
import { Outlet, NavLink as RouterNavLink } from 'react-router';

// Ensure the user is authenticated before allowing access to any protected routes.
// This should only be required in dev mode, as in production the server will handle route protection.
export { clientLoader } from './auth.logout';

export default function ProtectedLayout() {
	return (
		<AppShell navbar={{ width: 100, breakpoint: 'sm' }}>
			{/* <AppShell.Header>AutoParte!</AppShell.Header> */}
			<AppShell.Navbar>
				{/* <Select
					data={[
						{ value: 'id1', label: 'Establishment 1' },
						{ value: 'id2', label: 'Establishment 2' },
					]}
					allowDeselect={false}
					onChange={(value) => void navigate(`/establishments/${value ?? ''}`)}
				/> */}
				<NavLink component={RouterNavLink} to="/bookings" label="Bookings" />

				{/* TODO: Admin links */}
				<NavLink component={RouterNavLink} to="/employees" label="Employees" />
				<NavLink
					component={RouterNavLink}
					to="/configuration"
					label="Configuration"
				/>

				<Box style={{ flex: 1 }} />

				{/* Authentication links */}
				<NavLink component={RouterNavLink} to="/auth/logout" label="Logout" />
			</AppShell.Navbar>
			<AppShell.Main>
				<Outlet />
			</AppShell.Main>
			{/* <AppShell.Footer>FOOTER!</AppShell.Footer> */}
		</AppShell>
	);
}
