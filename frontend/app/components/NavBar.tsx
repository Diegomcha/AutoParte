import { AppShell, Select } from '@mantine/core';
import { NavLink, useNavigate } from 'react-router';

export default function NavBar() {
	const navigate = useNavigate();

	return (
		<AppShell.Navbar>
			<Select
				data={[
					{ value: 'id1', label: 'Establishment 1' },
					{ value: 'id2', label: 'Establishment 2' },
				]}
				allowDeselect={false}
				onChange={(value) => void navigate(`/establishments/${value ?? ''}`)}
			/>
			<NavLink to="/bookings">Bookings</NavLink>

			{/* TODO: Admin links */}
			<NavLink to="/employees">Employees</NavLink>
			<NavLink to="/configuration">Configuration</NavLink>

			{/* Authentication links */}
			{<NavLink to="/auth/logout">Logout</NavLink>}
		</AppShell.Navbar>
	);
}
