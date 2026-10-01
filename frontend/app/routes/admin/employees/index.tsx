import { useState } from "react";
import { Link, Outlet } from "react-router";

import { Badge, Button, Center, Divider, Group, Title } from "@mantine/core";

import {
	CursorClickIcon,
	EyeIcon,
	PasswordIcon,
	PencilIcon,
	PlusIcon,
	TrashIcon
} from "@phosphor-icons/react";
import { useQuery } from "@tanstack/react-query";
import { DataTable, useDataTableColumns } from "mantine-datatable";
import { useTranslation } from "react-i18next";

import AdminDeleteModal from "~/component/admin/AdminDeleteModal";
import TableActionButton from "~/component/admin/TableActionButton";
import EnablementBadge from "~/component/badge/EnablementBadge";
import { DEFAULT_PAGE_SIZE, queryClient, queryFactory } from "~/services/Api";
import TimeService from "~/services/TimeService";

import type { EmployeeDtoResponse } from "~/@types/api";
import type { DataTableSortStatus } from "mantine-datatable";

const COLUMNS_STATE_KEY = "employee-table-columns";

const DEFAULT_PAGE = 0;
const DEFAULT_SORTING = {
	columnAccessor: "id",
	direction: "asc"
} as const;

export async function clientLoader() {
	await queryClient.query(
		queryFactory.employees.pagedList({
			page: DEFAULT_PAGE,
			sorting: [DEFAULT_SORTING]
		})
	);
}

export default function EmployeesPage() {
	const { t } = useTranslation("routes", {
		keyPrefix: "admin.employees.index"
	});
	const { t: tEmployee } = useTranslation("entities", {
		keyPrefix: "employee"
	});
	const { t: tEntity } = useTranslation("entities", {
		keyPrefix: "common"
	});
	const { t: tCommon } = useTranslation();

	const [page, setPage] = useState(DEFAULT_PAGE);
	const [sortStatus, setSortStatus] =
		useState<DataTableSortStatus<EmployeeDtoResponse>>(DEFAULT_SORTING);
	const [selected, setSelected] = useState<EmployeeDtoResponse[]>([]);
	const [deleteModalOpen, setDeleteModalOpen] = useState(false);

	const { data, isLoading } = useQuery(
		queryFactory.employees.pagedList({
			page,
			sorting: [sortStatus]
		})
	);

	const { effectiveColumns } = useDataTableColumns<EmployeeDtoResponse>({
		key: COLUMNS_STATE_KEY,
		columns: [
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "enabled",
				title: tEmployee(($) => $.enabled.label),
				render: (employee) => (
					<EnablementBadge
						enabled={employee.enabled}
						disabledAt={
							employee.disabledAt
								? TimeService(employee.disabledAt).toDate()
								: null
						}
					/>
				)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "name",
				title: tEmployee(($) => $.name.label),
				render: (employee) => `${employee.name} ${employee.surname}`
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "email",
				title: tEmployee(($) => $.email.label)
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
				accessor: "accommodations",
				title: tEmployee(($) => $.accommodations.label),
				render: (employee) => (
					<Badge variant="light">
						{tEmployee(($) => $.accommodations.value, {
							count: employee.accommodations.length
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
				render: (employee) => (
					<Group gap={4} wrap="nowrap" justify="center">
						<TableActionButton
							component={Link}
							to={`/admin/employees/${employee.id}`}
							size="sm"
							variant="subtle"
							tooltip={t(($) => $.tableButtons.view.tooltip)}
							color={t(($) => $.tableButtons.view.color)}
						>
							<EyeIcon />
						</TableActionButton>
						<TableActionButton
							component={Link}
							to={`/admin/employees/${employee.id}/edit`}
							size="sm"
							variant="subtle"
							tooltip={t(($) => $.tableButtons.edit.tooltip)}
							color={t(($) => $.tableButtons.edit.color)}
						>
							<PencilIcon />
						</TableActionButton>
						<TableActionButton
							component={Link}
							to={`/admin/employees/${employee.id}/reset-password`}
							size="sm"
							variant="subtle"
							tooltip={t(($) => $.tableButtons.resetPassword.tooltip)}
							color={t(($) => $.tableButtons.resetPassword.color)}
						>
							<PasswordIcon />
						</TableActionButton>
						<TableActionButton
							component={Link}
							to={`/admin/employees/${employee.id}/delete`}
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
						to="/admin/employees/new"
						color={t(($) => $.addButton.color)}
						leftSection={<PlusIcon weight="bold" size={16} />}
					>
						{t(($) => $.addButton.label)}
					</Button>
				</Group>
			</Group>
			<Divider my="sm" />
			<DataTable
				height={"calc(100vh - 93px)"}
				noRecordsText={t(($) => $.noRecords)}
				storeColumnsKey={COLUMNS_STATE_KEY}
				columns={effectiveColumns}
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
						t(($) => $.deleteMultiple.description, { count })
				}}
			/>
		</>
	);
}
