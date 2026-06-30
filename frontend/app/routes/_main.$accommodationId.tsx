import {
	ActionIcon,
	Badge,
	Button,
	Center,
	CheckIcon,
	Divider,
	Group,
	Modal,
	Title,
	Tooltip,
} from '@mantine/core';
import {
	CalendarCheckIcon,
	CheckCircleIcon,
	CursorClickIcon,
	EyeIcon,
	LockKeyIcon,
	PaperPlaneTiltIcon,
	PencilIcon,
	PlusIcon,
	ProhibitIcon,
	TrashIcon,
	UserCheckIcon,
	WifiHighIcon,
	WifiSlashIcon,
} from '@phosphor-icons/react';
import { useMutation, useQuery } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import BooleanBadge from '~/component/BooleanBadge';
import TableActionButton from '~/component/TableActionButton';
import WifiBadge from '~/component/WifiBadge';
import dayjs from 'dayjs';
import { DataTable } from 'mantine-datatable';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, Outlet, useRevalidator } from 'react-router';
import type { Route } from './+types/_main.$accommodationId';
import type { BookingDtoResponse } from '~/@types/api';
import type { DataTableColumn, DataTableSortStatus } from 'mantine-datatable';

const PAGE_SIZE = 50;

export async function clientLoader({
	params: { accommodationId },
}: Route.ClientLoaderArgs) {
	return await queryClient.fetchQuery({
		queryKey: ['accommodations', accommodationId],
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/accommodations/{id}', {
					params: { path: { id: accommodationId } },
				})
			),
	});
}

export default function BookingsPage({
	params: { accommodationId },
}: Route.ComponentProps) {
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
							accommodationId,
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

	const getQuickActionButton = (booking: BookingDtoResponse) => {
		let button: React.ReactNode | undefined;
		switch (booking.status) {
			case 'CONFIRMATION_READY':
				button = (
					<TableActionButton
						tooltip={t(($) => $.common.actionButtons.confirm)}
						color="pink"
					>
						<CalendarCheckIcon />
					</TableActionButton>
				);
				break;
			case 'CONFIRMED':
				button = (
					<TableActionButton
						tooltip={t(($) => $.common.actionButtons.publish)}
						color="pink"
					>
						<PaperPlaneTiltIcon />
					</TableActionButton>
				);
				break;
			case 'CHECK_IN_READY':
				button = (
					<TableActionButton
						tooltip={t(($) => $.common.actionButtons.checkIn)}
						color="pink"
					>
						<UserCheckIcon />
					</TableActionButton>
				);
				break;
		}
		return button ? (
			<>
				{button}
				<Divider orientation="vertical" />
			</>
		) : undefined;
	};

	const columns: DataTableColumn<BookingDtoResponse>[] = [
		{
			sortable: true,
			resizable: true,
			accessor: 'published',
			title: t(($) => $.bookings.properties.published),
			render: (booking) => (
				<BooleanBadge
					value={!!booking.published}
					icons={{ true: <PaperPlaneTiltIcon />, false: <LockKeyIcon /> }}
				/>
			),
		},
		{
			sortable: true,
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
			sortable: true,
			resizable: true,
			accessor: 'startTime',
			title: t(($) => $.bookings.properties.startTime),
			render: (booking) => dayjs(booking.startTime).format('LLLL'),
		},
		{
			sortable: true,
			resizable: true,
			accessor: 'endTime',
			title: t(($) => $.bookings.properties.endTime),
			render: (booking) => dayjs(booking.endTime).format('LLLL'),
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
			render: (booking) => <WifiBadge value={booking.internetConnection} />,
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
			render: (booking) => (
				<Group gap={4} wrap="nowrap" justify="center">
					{getQuickActionButton(booking)}
					<TableActionButton
						tooltip={t(($) => $.common.actionButtons.view)}
						color="green"
					>
						<EyeIcon />
					</TableActionButton>
					<TableActionButton
						tooltip={t(($) => $.common.actionButtons.edit)}
						disabled={!booking.canBeModified}
						color="blue"
					>
						<PencilIcon />
					</TableActionButton>
					<TableActionButton
						tooltip={t(($) => $.common.actionButtons.cancel)}
						color="red"
					>
						<ProhibitIcon />
					</TableActionButton>
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
						await api.DELETE(
							'/api/accommodations/{accommodationId}/bookings/{bookingId}',
							{
								params: { path: { id: accommodation.id } },
							}
						)
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
					{/* <Button
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
					</Button> */}
					<Button
						component={Link}
						to="/admin/accommodations/new"
						color="green"
						leftSection={<PlusIcon weight="bold" size={16} />}
					>
						{t(($) => $.bookings.new.button)}
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
