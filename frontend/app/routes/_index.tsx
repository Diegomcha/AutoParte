import { AppShell } from '@mantine/core';
import NavBar from '~/components/NavBar';
import { Outlet } from 'react-router';

export default function Dashboard() {
	return (
		<AppShell>
			<AppShell.Header></AppShell.Header>
			<AppShell.Navbar>
				<NavBar />
			</AppShell.Navbar>
			<AppShell.Main>
				<Outlet />
			</AppShell.Main>
			<AppShell.Footer></AppShell.Footer>
		</AppShell>
	);
}
