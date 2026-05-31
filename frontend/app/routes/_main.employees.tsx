import {
	ActionIcon,
	ActionIconGroup,
	Badge,
	Button,
	Checkbox,
	Loader,
	Table,
	Title,
	Tooltip,
} from '@mantine/core';
import api from '~/api';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import InfiniteScroll from 'react-infinite-scroll-component';
import { Link, Outlet } from 'react-router';
import type { EmployeeDtoResponse, PageMetadata } from '~/@types/api';

export default function EmployeesPage() {
	const { t } = useTranslation();

	const [employees, setEmployees] = useState<EmployeeDtoResponse[]>([]);
	const [page, setPage] = useState(0);
	const [hasMore, setHasMore] = useState(true);

	return (
		<>
			<Title>{t(($) => $.employees.title)}</Title>
			<Link to="/employees/new">
				<Button>{t(($) => $.employees.new.button)}</Button>
			</Link>

			<InfiniteScroll
				dataLength={employees.length}
				next={async () => {
					setPage((prev) => prev + 1);

					const { data } = await api.GET('/api/employees', {
						params: { query: { page } },
					});

					setEmployees((prev) => [...prev, ...(data?.content ?? [])]);

					const pageData = data?.page as Required<PageMetadata>;
					if (pageData.number >= pageData.totalPages - 1) setHasMore(false);
				}}
				hasMore={hasMore}
				loader={<Loader />}
			>
				<Table>
					<Table.Thead>
						<Table.Tr>
							<Table.Th />
							<Table.Th>
								{t(($) => $.employees.properties.status.title)}
							</Table.Th>
							<Table.Th>{t(($) => $.employees.properties.name)}</Table.Th>
							<Table.Th>{t(($) => $.employees.properties.surname)}</Table.Th>
							<Table.Th>{t(($) => $.employees.properties.email)}</Table.Th>
							<Table.Th>{t(($) => $.employees.properties.createdAt)}</Table.Th>
							<Table.Th>{t(($) => $.employees.properties.updatedAt)}</Table.Th>
							<Table.Th />
						</Table.Tr>
					</Table.Thead>
					<Table.Tbody>
						{employees.map((employee) => (
							<Table.Tr key={employee.id}>
								<Table.Td>
									<Checkbox />
								</Table.Td>
								<Table.Td>
									<Tooltip
										label={
											employee.disabledAt &&
											new Date(employee.disabledAt).toLocaleString()
										}
										withArrow
										hidden={!employee.disabledAt}
									>
										<Badge color={employee.enabled ? 'green' : 'red'}>
											{t(($) =>
												employee.enabled
													? $.employees.properties.status.options.active
													: $.employees.properties.status.options.inactive
											)}
										</Badge>
									</Tooltip>
								</Table.Td>
								<Table.Td>{employee.name}</Table.Td>
								<Table.Td>{employee.surname}</Table.Td>
								<Table.Td>{employee.email}</Table.Td>
								<Table.Td>
									{new Date(employee.createdAt).toLocaleString()}
								</Table.Td>
								<Table.Td>
									{new Date(employee.updatedAt).toLocaleString()}
								</Table.Td>
								<Table.Td>
									<ActionIconGroup>
										<Link to={`/employees/${employee.id}`}>
											<ActionIcon>View</ActionIcon>
										</Link>
										<Link to={`/employees/${employee.id}/edit`}>
											<ActionIcon>Edit</ActionIcon>
										</Link>
										<Link to={`/employees/${employee.id}/delete`}>
											<ActionIcon>Delete</ActionIcon>
										</Link>
									</ActionIconGroup>
								</Table.Td>
							</Table.Tr>
						))}
					</Table.Tbody>
				</Table>
			</InfiniteScroll>
			<Outlet />
		</>
	);
}
