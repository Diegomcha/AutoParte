import { AppShell, Box, NavLink } from '@mantine/core';
import {
	BedIcon,
	FileTextIcon,
	GearIcon,
	SignOutIcon,
	UserCircleIcon,
} from '@phosphor-icons/react';
import AuthService from '~/services/AuthService';
import { useTranslation } from 'react-i18next';
import { Outlet, NavLink as RouterNavLink } from 'react-router';
import type { Route } from './+types/admin';

// Ensure the user is authenticated & admin before allowing access to any protected routes.
// This should only be required in dev mode, as in production the server will handle route protection.
export async function clientLoader({ request }: Route.ClientLoaderArgs) {
	if (!(await AuthService.isAuthenticated()))
		return AuthService.getLoginRedirection(request);

	if (!(await AuthService.isAdmin()))
		return AuthService.getLoginRedirection(request);
}

export default function ProtectedAdminLayout() {
	const { t } = useTranslation();

	return (
		<AppShell navbar={{ width: 150, breakpoint: 'sm' }} padding="md">
			<AppShell.Navbar>
				<NavLink
					component={RouterNavLink}
					to="/admin/employees"
					leftSection={<UserCircleIcon weight="bold" />}
					label={t(($) => $.admin.nav.employees)}
				/>
				<NavLink
					component={RouterNavLink}
					to="/admin/accommodations"
					leftSection={<BedIcon weight="bold" />}
					label={t(($) => $.admin.nav.accommodations)}
				/>
				<NavLink
					component={RouterNavLink}
					to="/admin/configuration"
					leftSection={<GearIcon weight="bold" />}
					label={t(($) => $.admin.nav.configuration)}
				/>
				<NavLink
					component={RouterNavLink}
					to="/admin/logs"
					leftSection={<FileTextIcon weight="bold" />}
					label={t(($) => $.admin.nav.logs)}
				/>
				<Box style={{ flex: 1 }} />
				<NavLink
					component={RouterNavLink}
					to="/"
					leftSection={<SignOutIcon weight="bold" />}
					label={t(($) => $.admin.nav.exit)}
				/>
			</AppShell.Navbar>
			<AppShell.Main>
				<Outlet />
			</AppShell.Main>
		</AppShell>
	);
}
