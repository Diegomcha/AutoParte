import { useState } from "react";
import { Link, Outlet } from "react-router";

import {
	ActionIcon,
	Badge,
	Button,
	Center,
	Divider,
	Group,
	Title
} from "@mantine/core";

import {
	CursorClickIcon,
	EyeIcon,
	PencilIcon,
	PlusIcon,
	TrashIcon
} from "@phosphor-icons/react";
import { useQuery } from "@tanstack/react-query";
import { DataTable, useDataTableColumns } from "mantine-datatable";
import { useTranslation } from "react-i18next";

import AdminDeleteModal from "~/component/admin/AdminDeleteModal";
import WifiBadge from "~/component/WifiBadge";
import { DEFAULT_PAGE_SIZE, queryClient, queryFactory } from "~/services/Api";
import TimeService from "~/services/TimeService";

import type { AccommodationDtoResponse } from "~/@types/api";
import type { DataTableSortStatus } from "mantine-datatable";

const COLUMNS_STATE_KEY = "accommodation-table-columns";

export async function clientLoader() {
	await queryClient.query(queryFactory.accommodations.pagedList());
}

export default function AccommodationsPage() {
	const { t } = useTranslation();

	// Async data fetching

	const [page, setPage] = useState(0);
	const [sortStatus, setSortStatus] = useState<
		DataTableSortStatus<AccommodationDtoResponse>
	>({
		columnAccessor: "id",
		direction: "asc"
	});
	const [selected, setSelected] = useState<AccommodationDtoResponse[]>([]);
	const [deleteModalOpen, setDeleteModalOpen] = useState(false);

	const { data, isLoading } = useQuery(
		queryFactory.accommodations.pagedList({
			page,
			sorting: [sortStatus]
		})
	);

	const { effectiveColumns } = useDataTableColumns<AccommodationDtoResponse>({
		key: COLUMNS_STATE_KEY,
		columns: [
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "name",
				title: t(($) => $.admin.accommodations.properties.name.label)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "sesCode",
				title: t(($) => $.admin.accommodations.properties.sesCode.label)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "internetConnection",
				title: t(
					($) => $.admin.accommodations.properties.internetConnection.label
				),
				render: (accommodation) => (
					<WifiBadge value={accommodation.internetConnection} />
				)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "createdAt",
				title: t(($) => $.common.properties.createdAt),
				render: (entity) => TimeService(entity.createdAt).format("LLLL")
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "updatedAt",
				title: t(($) => $.common.properties.updatedAt),
				render: (entity) => TimeService(entity.updatedAt).format("LLLL")
			},
			{
				accessor: "employees",
				title: t(($) => $.admin.accommodations.properties.employees.label),
				render: (accommodation) =>
					accommodation.employees.length === 0 ? (
						t(($) => $.admin.accommodations.properties.employees.none)
					) : (
						<Badge variant="light">
							{t(($) => $.admin.accommodations.properties.employees.some, {
								count: accommodation.employees.length
							})}
						</Badge>
					)
			},
			{
				accessor: "actions",
				title: (
					<Center>
						<CursorClickIcon weight="bold" />
					</Center>
				),
				textAlign: "center",
				width: "0%",
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
				)
			}
		]
	});

	return (
		<>
			<Group justify="space-between">
				<Title order={2}>{t(($) => $.admin.accommodations.title)}</Title>
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
							count: selected.length
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
				height="calc(100vh - 93px)"
				noRecordsText={t(($) => $.admin.accommodations.noRecords)}
				columns={effectiveColumns}
				storeColumnsKey={COLUMNS_STATE_KEY}
				pinLastColumn
				records={data?.content}
				page={page}
				onPageChange={setPage}
				fetching={isLoading}
				totalRecords={data?.page.totalElements}
				recordsPerPage={data?.page.size ?? DEFAULT_PAGE_SIZE}
				sortStatus={sortStatus}
				onSortStatusChange={setSortStatus}
				selectedRecords={selected}
				onSelectedRecordsChange={setSelected}
			/>
			<Outlet />
			<AdminDeleteModal
				mutation={queryFactory.employees.deleteMultiple()}
				selected={selected}
				setSelected={setSelected}
				deleteModalOpen={deleteModalOpen}
				setDeleteModalOpen={setDeleteModalOpen}
				messages={{
					title: t(($) => $.admin.accommodations.deleteMultiple.title),
					description: (count: number) =>
						t(($) => $.admin.accommodations.deleteMultiple.description, {
							count
						})
				}}
			/>
		</>
	);
}
