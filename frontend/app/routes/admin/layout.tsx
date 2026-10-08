import { Outlet, redirect, NavLink as RouterNavLink } from "react-router";

import { AppShell, Box, NavLink } from "@mantine/core";

import {
	BedIcon,
	BroadcastIcon,
	FileTextIcon,
	FingerprintSimpleIcon,
	GearIcon,
	SignOutIcon,
	UserCircleIcon,
	UserListIcon
} from "@phosphor-icons/react";
import { useTranslation } from "react-i18next";

import AuthService from "~/services/AuthService";

import type { Route } from "./+types/layout";

// Ensure the user is authenticated & admin before allowing access to any protected routes.
export async function clientLoader({ request }: Route.ClientLoaderArgs) {
	if (!(await AuthService.isAuthenticated()))
		return AuthService.getLoginRedirection(request);

	if (!(await AuthService.isAdmin())) return redirect("/");
}

export default function ProtectedAdminLayout() {
	const { t } = useTranslation("routes", { keyPrefix: "admin" });

	return (
		<AppShell
			navbar={{
				width: 200,
				collapsed: {
					mobile: true
				},
				breakpoint: "xs"
			}}
			padding="md"
		>
			<AppShell.Navbar>
				<NavLink
					component={RouterNavLink}
					to="/admin/employees"
					leftSection={<UserCircleIcon weight="bold" />}
					label={t(($) => $.nav.employees.label)}
				/>
				<NavLink
					component={RouterNavLink}
					to="/admin/accommodations"
					leftSection={<BedIcon weight="bold" />}
					label={t(($) => $.nav.accommodations.label)}
				/>
				<NavLink
					component={RouterNavLink}
					to="/admin/logs"
					leftSection={<FileTextIcon weight="bold" />}
					label={t(($) => $.nav.logs.label)}
					defaultOpened
				>
					<NavLink
						component={RouterNavLink}
						to="/admin/logs/accounts"
						leftSection={<UserListIcon weight="bold" />}
						label={t(($) => $.nav.logs.subRoutes.accounts.label)}
					/>
					<NavLink
						component={RouterNavLink}
						to="/admin/logs/security"
						leftSection={<FingerprintSimpleIcon weight="bold" />}
						label={t(($) => $.nav.logs.subRoutes.security.label)}
					/>
					<NavLink
						component={RouterNavLink}
						to="/admin/logs/communications"
						leftSection={<BroadcastIcon weight="bold" />}
						label={t(($) => $.nav.logs.subRoutes.communications.label)}
					/>
				</NavLink>

				<Box style={{ flex: 1 }} />
				<NavLink
					component={RouterNavLink}
					to="/admin/configuration"
					leftSection={<GearIcon weight="bold" />}
					label={t(($) => $.nav.configuration.label)}
				/>
				<NavLink
					component={RouterNavLink}
					to="/"
					leftSection={<SignOutIcon weight="bold" />}
					label={t(($) => $.nav.exit.label)}
				/>
			</AppShell.Navbar>
			<AppShell.Main>
				<Outlet />
			</AppShell.Main>
		</AppShell>
	);
}
