import { useState } from "react";
import { Link, Outlet } from "react-router";

import {
	ActionIcon,
	Badge,
	Button,
	Center,
	Divider,
	Group,
	Title,
	Tooltip
} from "@mantine/core";

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

import AdminDeleteModal from "~/component/AdminDeleteModal";
import { DEFAULT_PAGE_SIZE, queryClient, queryFactory } from "~/services/Api";
import TimeService from "~/services/TimeService";

import type { EmployeeDtoResponse } from "~/@types/api";
import type { DataTableSortStatus } from "mantine-datatable";

const COLUMNS_STATE_KEY = "employee-table-columns";

export async function clientLoader() {
	await queryClient.query(queryFactory.employees.pagedList());
}

export default function EmployeesPage() {
	const { t } = useTranslation();

	const [page, setPage] = useState(0);
	const [sortStatus, setSortStatus] = useState<
		DataTableSortStatus<EmployeeDtoResponse>
	>({
		columnAccessor: "id",
		direction: "asc"
	});
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
				title: t(($) => $.admin.employees.properties.enabled.label),
				render: (employee) => (
					<Tooltip
						label={t(
							($) => $.admin.employees.properties.enabled.disabledTooltip,
							{
								date: employee.disabledAt
									? TimeService(employee.disabledAt).format("LLL")
									: null
							}
						)}
						withArrow
						disabled={!employee.disabledAt}
					>
						<Badge color={employee.enabled ? "green" : "gray"} variant="light">
							{employee.enabled
								? t(($) => $.admin.employees.properties.enabled.states.enabled)
								: t(
										($) => $.admin.employees.properties.enabled.states.disabled
									)}
						</Badge>
					</Tooltip>
				)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "name",
				title: t(($) => $.admin.employees.properties.name.label),
				render: (employee) => `${employee.name} ${employee.surname}`
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "email",
				title: t(($) => $.admin.employees.properties.email.label)
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
				accessor: "accommodations",
				title: t(($) => $.admin.employees.properties.accommodations.label),
				render: (employee) =>
					employee.accommodations.length === 0 ? (
						t(($) => $.admin.employees.properties.accommodations.none)
					) : (
						<Badge variant="light">
							{t(($) => $.admin.employees.properties.accommodations.some, {
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
				)
			}
		]
	});

	return (
		<>
			<Group justify="space-between">
				<Title order={2}>{t(($) => $.admin.employees.title)}</Title>
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
				height={"calc(100vh - 93px)"}
				noRecordsText={t(($) => $.admin.employees.noRecords)}
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
					title: t(($) => $.admin.employees.deleteMultiple.title),
					description: (count: number) =>
						t(($) => $.admin.employees.deleteMultiple.description, { count })
				}}
			/>
		</>
	);
}
