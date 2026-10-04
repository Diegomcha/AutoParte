import { useEffect, useState } from "react";
import { Outlet, useNavigate } from "react-router";

import { Badge, Divider, Group, Select, Title } from "@mantine/core";

import {
	CookieIcon,
	KeyIcon,
	LockIcon,
	PasswordIcon,
	SignInIcon,
	SignOutIcon
} from "@phosphor-icons/react";
import { useQuery } from "@tanstack/react-query";
import { DataTable, useDataTableColumns } from "mantine-datatable";
import { useTranslation } from "react-i18next";

import {
	DEFAULT_PAGE,
	DEFAULT_PAGE_SIZE,
	DEFAULT_SORTING,
	queryClient,
	queryFactory
} from "~/services/Api";
import TimeService from "~/services/TimeService";
import Validators from "~/services/Validators";

import type { SecurityEventDto } from "~/@types/api";
import type { DataTableSortStatus } from "mantine-datatable";
import type { Route } from "./+types/security";

const COLUMNS_STATE_KEY = "logs-security-table-columns";

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	if (id) Validators.validateUuids(id);

	const pageable = {
		page: DEFAULT_PAGE,
		sorting: [DEFAULT_SORTING]
	};

	await queryClient.query(
		id
			? queryFactory.accounts.securityEvents.accountPaged(id, pageable)
			: queryFactory.accounts.securityEvents.globalPaged(pageable)
	);

	return {
		accounts: await queryClient.query(queryFactory.accounts.list())
	};
}

export default function LogsSecurityPage({
	loaderData: { accounts },
	params: { id }
}: Route.ComponentProps) {
	const navigate = useNavigate();

	const { t } = useTranslation("routes", {
		keyPrefix: "admin.logs.security"
	});
	const { t: tSecurityEvent } = useTranslation("entities", {
		keyPrefix: "account.securityEvents"
	});

	const [page, setPage] = useState(DEFAULT_PAGE);
	const [sortStatus, setSortStatus] =
		useState<DataTableSortStatus<SecurityEventDto>>(DEFAULT_SORTING);

	const pageable = {
		page: page,
		sorting: [sortStatus]
	};

	const { data, isLoading } = useQuery(
		id
			? queryFactory.accounts.securityEvents.accountPaged(id, pageable)
			: queryFactory.accounts.securityEvents.globalPaged(pageable)
	);

	const { effectiveColumns } = useDataTableColumns<SecurityEventDto>({
		key: COLUMNS_STATE_KEY,
		columns: [
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "timestamp",
				title: tSecurityEvent(($) => $.timestamp.label),
				render: (event) =>
					TimeService(event.timestamp).format(
						tSecurityEvent(($) => $.timestamp.format)
					)
			},
			{
				hidden: !!id,
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "accountId",
				title: tSecurityEvent(($) => $.account.label),
				render: (event) =>
					accounts.find((account) => account.id === event.accountId)
						?.username ??
					(event.accountId
						? tSecurityEvent(($) => $.account.deleted, { id: event.accountId })
						: tSecurityEvent(($) => $.account.unknown))
			},

			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "type",
				title: tSecurityEvent(($) => $.type.label),
				render: (event) => (
					<Badge
						variant="light"
						color={tSecurityEvent(($) => $.type.options[event.type].color)}
						leftSection={
							{
								LOGIN: <SignInIcon weight="bold" />,
								LOGOUT: <SignOutIcon weight="bold" />,
								PASSWORD_CHANGE: <PasswordIcon weight="bold" />,
								LOGIN_FAILED_CREDENTIALS: <LockIcon weight="bold" />,
								LOGIN_FAILED_CREDENTIALS_EXPIRED: <LockIcon weight="bold" />,
								LOGIN_FAILED_ACCOUNT_DISABLED: <LockIcon weight="bold" />,
								LOGIN_FAILED_ACCOUNT_LOCKED: <LockIcon weight="bold" />
							}[event.type]
						}
					>
						{tSecurityEvent(($) => $.type.options[event.type].label)}
					</Badge>
				)
			},
			{
				draggable: true,
				resizable: true,
				accessor: "typeDesc",
				title: tSecurityEvent(($) => $.type.descriptionLabel),
				render: (event) =>
					tSecurityEvent(($) => $.type.options[event.type].description)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "method",
				title: tSecurityEvent(($) => $.method.label),
				render: (event) => (
					<Badge
						variant="light"
						color={tSecurityEvent(($) => $.method.options[event.method].color)}
						leftSection={
							{
								USERNAME_PASSWORD: <KeyIcon weight="bold" />,
								REMEMBER_ME: <CookieIcon weight="bold" />
							}[event.method]
						}
					>
						{tSecurityEvent(($) => $.method.options[event.method].label)}
					</Badge>
				)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "remoteAddress",
				title: tSecurityEvent(($) => $.remoteAddress.label)
			}
		]
	});

	return (
		<>
			<Group justify="space-between">
				<Title order={2}>{t(($) => $.title)}</Title>
				<Group>
					<Select
						placeholder={t(($) => $.accountSelector.label)}
						value={id}
						onChange={(value) => {
							void navigate(
								value ? `/admin/logs/security/${value}` : "/admin/logs/security"
							);
						}}
						clearable
						searchable
						data={accounts.map((account) => ({
							value: account.id,
							label: account.username
						}))}
					/>
				</Group>
			</Group>
			<Divider my="sm" />
			<DataTable
				height={"calc(100vh - 93px)"}
				noRecordsText={t(($) => $.noRecords)}
				storeColumnsKey={COLUMNS_STATE_KEY}
				columns={effectiveColumns}
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
