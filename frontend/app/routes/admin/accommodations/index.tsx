import { useState } from "react";
import { Link, Outlet } from "react-router";

import { Badge, Button, Center, Divider, Group, Title } from "@mantine/core";

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
import TableActionButton from "~/component/admin/TableActionButton";
import WifiBadge from "~/component/badge/WifiBadge";
import { DEFAULT_PAGE_SIZE, queryClient, queryFactory } from "~/services/Api";
import TimeService from "~/services/TimeService";

import type { AccommodationDtoResponse } from "~/@types/api";
import type { DataTableSortStatus } from "mantine-datatable";

const COLUMNS_STATE_KEY = "accommodation-table-columns";

const DEFAULT_PAGE = 0;
const DEFAULT_SORTING = {
	columnAccessor: "id",
	direction: "asc"
} as const;

export async function clientLoader() {
	await queryClient.query(
		queryFactory.accommodations.pagedList({
			page: DEFAULT_PAGE,
			sorting: [DEFAULT_SORTING]
		})
	);
}

export default function AccommodationsPage() {
	const { t } = useTranslation("routes", {
		keyPrefix: "admin.accommodations.index"
	});
	const { t: tAccommodation } = useTranslation("entities", {
		keyPrefix: "accommodation"
	});
	const { t: tEntity } = useTranslation("entities", {
		keyPrefix: "common"
	});
	const { t: tCommon } = useTranslation();

	// Async data fetching

	const [page, setPage] = useState(DEFAULT_PAGE);
	const [sortStatus, setSortStatus] =
		useState<DataTableSortStatus<AccommodationDtoResponse>>(DEFAULT_SORTING);
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
				title: tAccommodation(($) => $.name.label)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "sesCode",
				title: tAccommodation(($) => $.sesCode.label)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "internetConnection",
				title: tAccommodation(($) => $.internetConnection.label),
				render: (accommodation) => (
					<WifiBadge value={accommodation.internetConnection} />
				)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "createdAt",
				title: tEntity(($) => $.createdAt.label),
				render: (entity) =>
					TimeService(entity.createdAt).format(
						tEntity(($) => $.createdAt.format)
					)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "updatedAt",
				title: tEntity(($) => $.updatedAt.label),
				render: (entity) =>
					TimeService(entity.updatedAt).format(
						tEntity(($) => $.updatedAt.format)
					)
			},
			{
				accessor: "employees",
				title: tAccommodation(($) => $.employees.label),
				render: (accommodation) => (
					<Badge variant="light">
						{tAccommodation(($) => $.employees.value, {
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
						<TableActionButton
							component={Link}
							to={`/admin/accommodations/${accommodation.id}`}
							size="sm"
							variant="subtle"
							tooltip={t(($) => $.tableButtons.view.tooltip)}
							color={t(($) => $.tableButtons.view.color)}
						>
							<EyeIcon />
						</TableActionButton>
						<TableActionButton
							component={Link}
							to={`/admin/accommodations/${accommodation.id}/edit`}
							size="sm"
							variant="subtle"
							tooltip={t(($) => $.tableButtons.edit.tooltip)}
							color={t(($) => $.tableButtons.edit.color)}
						>
							<PencilIcon />
						</TableActionButton>
						<TableActionButton
							component={Link}
							to={`/admin/accommodations/${accommodation.id}/delete`}
							size="sm"
							variant="subtle"
							tooltip={t(($) => $.tableButtons.delete.tooltip)}
							color={t(($) => $.tableButtons.delete.color)}
						>
							<TrashIcon />
						</TableActionButton>
					</Group>
				)
			}
		]
	});

	return (
		<>
			<Group justify="space-between">
				<Title order={2}>{t(($) => $.title)}</Title>
				<Group>
					<Button
						color={tCommon(($) => $.buttons.deleteSelected.color)}
						leftSection={<TrashIcon weight="bold" size={16} />}
						disabled={selected.length === 0}
						onClick={() => {
							setDeleteModalOpen(true);
						}}
					>
						{tCommon(($) => $.buttons.deleteSelected.label, {
							count: selected.length
						})}
					</Button>
					<Button
						component={Link}
						to="/admin/accommodations/new"
						color={t(($) => $.addButton.color)}
						leftSection={<PlusIcon weight="bold" size={16} />}
					>
						{t(($) => $.addButton.label)}
					</Button>
				</Group>
			</Group>
			<Divider my="sm" />
			<DataTable
				height="calc(100vh - 93px)"
				noRecordsText={t(($) => $.noRecords)}
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
					title: t(($) => $.deleteMultiple.title),
					description: (count: number) =>
						t(($) => $.deleteMultiple.description, {
							count
						})
				}}
			/>
		</>
	);
}
