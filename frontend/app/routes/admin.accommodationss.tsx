import {
	ActionIcon,
	Badge,
	Button,
	Center,
	Divider,
	Group,
	Modal,
	Title,
	Tooltip,
} from '@mantine/core';
import {
	CursorClickIcon,
	EyeIcon,
	PasswordIcon,
	PencilIcon,
	PlusIcon,
	TrashIcon,
} from '@phosphor-icons/react';
import { useMutation, useQuery } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import dayjs from 'dayjs';
import { DataTable } from 'mantine-datatable';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, Outlet, useRevalidator } from 'react-router';
import type {
	AccommodationDtoResponse,
	EmployeeDtoResponse,
} from '~/@types/api';
import type { DataTableColumn, DataTableSortStatus } from 'mantine-datatable';

const PAGE_SIZE = 50;

export default function AccommodationsPage() {
	const { t } = useTranslation();
	const revalidator = useRevalidator();

	// Async data fetching

	const [page, setPage] = useState(0);
	const [sortStatus, setSortStatus] = useState<
		DataTableSortStatus<AccommodationDtoResponse>
	>({
		columnAccessor: 'id',
		direction: 'asc',
	});

	const { data, isLoading } = useQuery({
		queryKey: ['accommodations', page, sortStatus],
		throwOnError: true,
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/accommodations', {
					params: {
						query: {
							page: page,
							size: PAGE_SIZE,
							sort: [`${sortStatus.columnAccessor},${sortStatus.direction}`],
						},
					},
				})
			),
	});

	const [selected, setSelected] = useState<AccommodationDtoResponse[]>([]);

	const columns: DataTableColumn<AccommodationDtoResponse>[] = [
		{
			sortable: true,
			resizable: true,
			accessor: 'name',
			title: t(($) => $.admin.employees.properties.name.label),
			render: (accommodation) => accommodation.name,
		},
		{
			sortable: true,
			resizable: true,
			accessor: 'email',
			title: t(($) => $.admin.employees.properties.email.label),
		},
		{
			sortable: true,
			resizable: true,
			accessor: 'createdAt',
			title: t(($) => $.common.properties.createdAt),
			render: (employee) => dayjs(employee.createdAt).format('LLLL'),
		},
		{
			sortable: true,
			resizable: true,
			accessor: 'updatedAt',
			title: t(($) => $.common.properties.updatedAt),
			render: (employee) => dayjs(employee.updatedAt).format('LLLL'),
		},
		{
			accessor: 'accommodations',
			title: t(($) => $.admin.employees.properties.accommodations.label),
			render: (employee) =>
				employee.accommodations.length === 0 ? (
					t(($) => $.admin.employees.properties.accommodations.none)
				) : (
					<Badge variant="light">
						{t(($) => $.admin.employees.properties.accommodations.some, {
							count: employee.accommodations.length,
						})}
					</Badge>
				),
		},
		{
			accessor: 'actions',
			title: (
				<Center>
					<CursorClickIcon weight="bold" />
				</Center>
			),
			textAlign: 'center',
			width: '0%',
			render: (employee) => (
				<Group gap={4} wrap="nowrap" justify="center">
					<ActionIcon
						component={Link}
						to={`/admin/employees/${employee.id}`}
						size="sm"
						variant="subtle"
						color="green"
					>
						<EyeIcon weight="bold" />
					</ActionIcon>
					<ActionIcon
						component={Link}
						to={`/admin/employees/${employee.id}/edit`}
						size="sm"
						variant="subtle"
						color="blue"
					>
						<PencilIcon />
					</ActionIcon>
					<ActionIcon
						component={Link}
						to={`/admin/employees/${employee.id}/reset-password`}
						size="sm"
						variant="subtle"
						color="orange"
					>
						<PasswordIcon />
					</ActionIcon>
					<ActionIcon
						component={Link}
						to={`/admin/employees/${employee.id}/delete`}
						size="sm"
						variant="subtle"
						color="red"
					>
						<TrashIcon />
					</ActionIcon>
				</Group>
			),
		},
	];

	const [deleteModalOpen, setDeleteModalOpened] = useState(false);
	const { mutate: deleteSelected, isPending: isDeleting } = useMutation({
		throwOnError: true,
		mutationFn: async () => {
			await Promise.all(
				selected.map(async (employee) => {
					throwErrors(
						await api.DELETE('/api/employees/{id}', {
							params: { path: { id: employee.id } },
						})
					);
				})
			);
		},
		onSuccess: async () => {
			await queryClient.invalidateQueries({ queryKey: ['employees'] });
			await revalidator.revalidate();

			setSelected([]);
			setDeleteModalOpened(false);
		},
	});

	return (
		<>
			<div hidden={revalidator.state === 'idle'}>Revalidating...</div>
			<Group justify="space-between">
				<Title order={2}>{t(($) => $.admin.employees.title)}</Title>
				<Group>
					<Button
						color="red"
						leftSection={<TrashIcon weight="bold" size={16} />}
						disabled={selected.length === 0}
						onClick={() => {
							setDeleteModalOpened(true);
						}}
					>
						{t(($) => $.common.buttons.deleteSelected, {
							count: selected.length,
						})}
					</Button>
					<Button
						component={Link}
						to="/admin/employees/new"
						color="green"
						leftSection={<PlusIcon weight="bold" size={16} />}
					>
						{t(($) => $.admin.employees.new.button)}
					</Button>
				</Group>
			</Group>
			<Divider my="sm" />
			<DataTable
				height={'calc(100vh - 93px)'}
				noRecordsText={t(($) => $.admin.employees.noRecords)}
				columns={columns}
				pinLastColumn
				records={data?.content}
				page={page}
				onPageChange={setPage}
				fetching={isLoading}
				totalRecords={data?.page?.totalElements}
				recordsPerPage={PAGE_SIZE}
				sortStatus={sortStatus}
				onSortStatusChange={setSortStatus}
				selectedRecords={selected}
				onSelectedRecordsChange={setSelected}
			/>
			<Outlet />
			<Modal
				opened={deleteModalOpen}
				onClose={() => {
					setDeleteModalOpened(false);
				}}
				title={t(($) => $.admin.employees.deleteMultiple.title)}
			>
				{t(($) => $.admin.employees.deleteMultiple.description, {
					count: selected.length,
				})}

				<Group justify="right" mt="md" gap="xs">
					<Button
						disabled={isDeleting}
						onClick={() => {
							setDeleteModalOpened(false);
						}}
						color="gray"
					>
						{t(($) => $.common.buttons.cancel)}
					</Button>
					<Button
						color="red"
						onClick={() => {
							deleteSelected();
						}}
						loading={isDeleting}
					>
						{t(($) => $.common.buttons.delete)}
					</Button>
				</Group>
			</Modal>
		</>
	);
}
