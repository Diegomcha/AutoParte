import {
	ActionIcon,
	Badge,
	Button,
	Center,
	Divider,
	Group,
	Modal,
	Title,
} from '@mantine/core';
import {
	CursorClickIcon,
	EyeIcon,
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
import type { Route } from './+types/_main.$id';
import type { BookingDtoResponse } from '~/@types/api';
import type { DataTableColumn, DataTableSortStatus } from 'mantine-datatable';

const PAGE_SIZE = 50;

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	return await queryClient.fetchQuery({
		queryKey: ['accommodations', id],
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/accommodations/{id}', {
					params: { path: { id } },
				})
			),
	});
}

export default function BookingsPage({ params: { id } }: Route.ComponentProps) {
	const { t } = useTranslation();
	const revalidator = useRevalidator();

	// Async data fetching

	const [page, setPage] = useState(0);
	const [sortStatus, setSortStatus] = useState<
		DataTableSortStatus<BookingDtoResponse>
	>({
		columnAccessor: 'id',
		direction: 'asc',
	});

	const { data, isLoading } = useQuery({
		queryKey: ['bookings', page, sortStatus],
		throwOnError: true,
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/accommodations/{accommodationId}/bookings', {
					params: {
						path: {
							accommodationId: id,
						},
						query: {
							page: page,
							size: PAGE_SIZE,
							sort: [`${sortStatus.columnAccessor},${sortStatus.direction}`],
						},
					},
				})
			),
	});

	const [selected, setSelected] = useState<BookingDtoResponse[]>([]);

	const columns: DataTableColumn<BookingDtoResponse>[] = [
		{
			// TODO: Add filtering
			resizable: true,
			accessor: 'status',
			title: t(($) => $.bookings.properties.status.label),
			render: (booking) => (
				<Badge
					color={t(
						($) => $.bookings.properties.status.states[booking.status].color
					)}
				>
					{t(($) => $.bookings.properties.status.states[booking.status].label)}
				</Badge>
			),
		},
		{
			// TODO: Add filtering
			resizable: true,
			accessor: 'canBeConfirmed',
			title: t(($) => $.bookings.properties.canBeConfirmed.label),
			render: (booking) => (
				<Badge color={booking.canBeConfirmed ? 'green' : 'red'} variant="light">
					{booking.canBeConfirmed
						? t(($) => $.bookings.properties.canBeConfirmed.states.yes)
						: t(($) => $.bookings.properties.canBeConfirmed.states.no)}
				</Badge>
			),
		},
		{
			// TODO: Add filtering
			resizable: true,
			accessor: 'canBeCheckedIn',
			title: t(($) => $.bookings.properties.canBeCheckedIn.label),
			render: (booking) => (
				<Badge color={booking.canBeCheckedIn ? 'green' : 'red'} variant="light">
					{booking.canBeCheckedIn
						? t(($) => $.bookings.properties.canBeCheckedIn.states.yes)
						: t(($) => $.bookings.properties.canBeCheckedIn.states.no)}
				</Badge>
			),
		},
		{
			sortable: true,
			resizable: true,
			accessor: 'startTime',
			title: t(($) => $.bookings.properties.startTime),
			render: (booking) => dayjs(booking.startTime).format('LL'),
		},
		{
			sortable: true,
			resizable: true,
			accessor: 'endTime',
			title: t(($) => $.bookings.properties.endTime),
			render: (booking) => dayjs(booking.endTime).format('LL'),
		},
		{
			sortable: true,
			resizable: true,
			accessor: 'numberOfPeople',
			title: t(($) => $.bookings.properties.numberOfPeople),
		},
		{
			sortable: true,
			resizable: true,
			accessor: 'numberOfRooms',
			title: t(($) => $.bookings.properties.numberOfRooms),
		},
		{
			sortable: true,
			resizable: true,
			accessor: 'internetConnection',
			title: t(($) => $.bookings.properties.internetConnection.label),
			render: (booking) => (
				<Badge
					color={booking.internetConnection ? 'green' : 'red'}
					variant="light"
				>
					{booking.internetConnection
						? t(($) => $.bookings.properties.internetConnection.states.yes)
						: t(($) => $.bookings.properties.internetConnection.states.no)}
				</Badge>
			),
		},
		// {
		// 	sortable: true,
		// 	resizable: true,
		// 	accessor: 'name',
		// 	title: t(($) => $.admin.accommodations.properties.name.label),
		// },
		// {
		// 	sortable: true,
		// 	resizable: true,
		// 	accessor: 'sesCode',
		// 	title: t(($) => $.admin.accommodations.properties.sesCode.label),
		// },
		// {
		// 	sortable: true,
		// 	resizable: true,
		// 	accessor: 'internetConnection',
		// 	title: t(
		// 		($) => $.admin.accommodations.properties.internetConnection.label
		// 	),
		// 	render: (accommodation) => (
		// 		<Badge
		// 			color={accommodation.internetConnection ? 'green' : 'red'}
		// 			variant="light"
		// 		>
		// 			{accommodation.internetConnection
		// 				? t(
		// 						($) =>
		// 							$.admin.accommodations.properties.internetConnection.states
		// 								.yes
		// 					)
		// 				: t(
		// 						($) =>
		// 							$.admin.accommodations.properties.internetConnection.states.no
		// 					)}
		// 		</Badge>
		// 	),
		// },
		// {
		// 	sortable: true,
		// 	resizable: true,
		// 	accessor: 'createdAt',
		// 	title: t(($) => $.common.properties.createdAt),
		// 	render: (entity) => dayjs(entity.createdAt).format('LLLL'),
		// },
		// {
		// 	sortable: true,
		// 	resizable: true,
		// 	accessor: 'updatedAt',
		// 	title: t(($) => $.common.properties.updatedAt),
		// 	render: (entity) => dayjs(entity.updatedAt).format('LLLL'),
		// },
		// {
		// 	accessor: 'employees',
		// 	title: t(($) => $.admin.accommodations.properties.employees.label),
		// 	render: (accommodation) =>
		// 		accommodation.employees.length === 0 ? (
		// 			t(($) => $.admin.accommodations.properties.employees.none)
		// 		) : (
		// 			<Badge variant="light">
		// 				{t(($) => $.admin.accommodations.properties.employees.some, {
		// 					count: accommodation.employees.length,
		// 				})}
		// 			</Badge>
		// 		),
		// },
		{
			accessor: 'actions',
			title: (
				<Center>
					<CursorClickIcon weight="bold" />
				</Center>
			),
			textAlign: 'center',
			width: '0%',
			render: (accommodation) => (
				<Group gap={4} wrap="nowrap" justify="center">
					<ActionIcon
						component={Link}
						to={`/admin/accommodations/${accommodation.id}`}
						size="sm"
						variant="subtle"
						color="green"
					>
						<EyeIcon weight="bold" />
					</ActionIcon>
					<ActionIcon
						component={Link}
						to={`/admin/accommodations/${accommodation.id}/edit`}
						size="sm"
						variant="subtle"
						color="blue"
					>
						<PencilIcon />
					</ActionIcon>
					<ActionIcon
						component={Link}
						to={`/admin/accommodations/${accommodation.id}/delete`}
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

	const [deleteModalOpen, setDeleteModalOpen] = useState(false);
	const { mutate: deleteSelected, isPending: isDeleting } = useMutation({
		throwOnError: true,
		mutationFn: async () => {
			await Promise.all(
				selected.map(async (accommodation) => {
					throwErrors(
						await api.DELETE('/api/accommodations/{id}', {
							params: { path: { id: accommodation.id } },
						})
					);
				})
			);
		},
		onSuccess: async () => {
			await queryClient.invalidateQueries({ queryKey: ['accommodations'] });
			await revalidator.revalidate();

			setSelected([]);
			setDeleteModalOpen(false);
		},
	});

	return (
		<>
			<div hidden={revalidator.state === 'idle'}>Revalidating...</div>
			<Group justify="space-between">
				<Title order={2} size="h3" fw={'normal'}>
					{t(($) => $.bookings.title)}
				</Title>
				<Group>
					<Button
						color="red"
						leftSection={<TrashIcon weight="bold" size={16} />}
						disabled={selected.length === 0}
						onClick={() => {
							setDeleteModalOpen(true);
						}}
					>
						{t(($) => $.common.buttons.deleteSelected, {
							count: selected.length,
						})}
					</Button>
					<Button
						component={Link}
						to="/admin/accommodations/new"
						color="green"
						leftSection={<PlusIcon weight="bold" size={16} />}
					>
						{t(($) => $.admin.accommodations.new.button)}
					</Button>
				</Group>
			</Group>
			<Divider my="sm" />
			<DataTable
				height={'calc(100vh - 153px)'}
				noRecordsText={t(($) => $.admin.accommodations.noRecords)}
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
					setDeleteModalOpen(false);
				}}
				title={t(($) => $.admin.accommodations.deleteMultiple.title)}
			>
				{t(($) => $.admin.accommodations.deleteMultiple.description, {
					count: selected.length,
				})}

				<Group justify="right" mt="md" gap="xs">
					<Button
						disabled={isDeleting}
						onClick={() => {
							setDeleteModalOpen(false);
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
