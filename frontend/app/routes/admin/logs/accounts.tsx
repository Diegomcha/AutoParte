import { useState } from "react";
import { Link, Outlet } from "react-router";

import { Badge, Center, Divider, Group, Title } from "@mantine/core";

import {
	ClockCountdownIcon,
	CursorClickIcon,
	ListMagnifyingGlassIcon,
	PasswordIcon,
	UserGearIcon,
	UserIcon
} from "@phosphor-icons/react";
import { useQuery } from "@tanstack/react-query";
import { DataTable, useDataTableColumns } from "mantine-datatable";
import { useTranslation } from "react-i18next";

import TableActionButton from "~/component/admin/TableActionButton";
import BooleanBadge from "~/component/badge/BooleanBadge";
import EnablementBadge from "~/component/badge/EnablementBadge";
import {
	DEFAULT_PAGE,
	DEFAULT_PAGE_SIZE,
	DEFAULT_SORTING,
	queryClient,
	queryFactory
} from "~/services/Api";
import TimeService from "~/services/TimeService";

import type { AccountDtoFull } from "~/@types/api";
import type { DataTableSortStatus } from "mantine-datatable";

const COLUMNS_STATE_KEY = "logs-account-table-columns";

export async function clientLoader() {
	await queryClient.query(
		queryFactory.accounts.pagedList({
			page: DEFAULT_PAGE,
			sorting: [DEFAULT_SORTING]
		})
	);
}

export default function LogsAccountsPage() {
	const { t } = useTranslation("routes", {
		keyPrefix: "admin.logs.accounts"
	});
	const { t: tAccount } = useTranslation("entities", {
		keyPrefix: "account"
	});
	const { t: tEntity } = useTranslation("entities");

	const [page, setPage] = useState(DEFAULT_PAGE);
	const [sortStatus, setSortStatus] =
		useState<DataTableSortStatus<AccountDtoFull>>(DEFAULT_SORTING);

	const { data, isLoading } = useQuery(
		queryFactory.accounts.pagedList({
			page,
			sorting: [sortStatus]
		})
	);

	const { effectiveColumns } = useDataTableColumns<AccountDtoFull>({
		key: COLUMNS_STATE_KEY,
		columns: [
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "enabled",
				title: tAccount(($) => $.enabled.label),
				render: (account) => (
					<EnablementBadge
						enabled={account.enabled}
						disabledAt={
							account.disabledAt
								? TimeService(account.disabledAt).toDate()
								: null
						}
					/>
				)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "roles",
				title: tAccount(($) => $.roles.label),
				render: (account) =>
					account.roles.length === 0
						? tAccount(($) => $.roles.none)
						: account.roles.map((role) => (
								<Badge
									variant="light"
									color={tAccount(
										($) => $.roles.options[role as "ROLE_ADMIN"].color
									)}
									key={role}
									leftSection={
										{
											ROLE_ADMIN: <UserGearIcon weight="bold" />,
											ROLE_EMPLOYEE: <UserIcon weight="bold" />
										}[role as "ROLE_ADMIN" | "ROLE_EMPLOYEE"]
									}
								>
									{tAccount(($) => $.roles.options[role as "ROLE_ADMIN"].label)}
								</Badge>
							))
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "requiresReset",
				title: tAccount(($) => $.requiresReset.label),
				render: (account) => (
					<BooleanBadge
						value={account.requiresReset}
						displayOpts={{
							true: {
								color: "yellow",
								icon: <ClockCountdownIcon weight="bold" />
							},
							false: {
								color: "green",
								icon: <PasswordIcon weight="bold" />
							}
						}}
					/>
				)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "username",
				title: tAccount(($) => $.username.label)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "createdAt",
				title: tEntity(($) => $.common.createdAt.label),
				render: (entity) =>
					TimeService(entity.createdAt).format(
						tEntity(($) => $.common.createdAt.format)
					)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "updatedAt",
				title: tEntity(($) => $.common.updatedAt.label),
				render: (entity) =>
					TimeService(entity.updatedAt).format(
						tEntity(($) => $.common.updatedAt.format)
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
				render: (account) => (
					<Group gap={4} wrap="nowrap" justify="center">
						<TableActionButton
							component={Link}
							to={`/admin/logs/security/${account.id}`}
							size="sm"
							variant="subtle"
							color={t(($) => $.tableButtons.viewLogs.color)}
							tooltip={t(($) => $.tableButtons.viewLogs.tooltip)}
						>
							<ListMagnifyingGlassIcon />
						</TableActionButton>
						<TableActionButton
							disabled={!account.employeeId}
							component={account.employeeId ? Link : undefined}
							to={`/admin/employees/${String(account.employeeId)}`}
							size="sm"
							variant="subtle"
							color={t(($) => $.tableButtons.viewEmployee.color)}
							tooltip={t(($) => $.tableButtons.viewEmployee.tooltip)}
						>
							<UserIcon />
						</TableActionButton>
					</Group>
				)
			}
		]
	});

	return (
		<>
			<Title order={2}>{t(($) => $.title)}</Title>
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
			/>
			<Outlet />
		</>
	);
}
