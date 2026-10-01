import { useState } from "react";
import { Link, Outlet, useNavigate } from "react-router";

import { Badge, Center, Divider, Group, Select, Title } from "@mantine/core";

import { BedIcon, CalendarIcon, CursorClickIcon } from "@phosphor-icons/react";
import { useQuery } from "@tanstack/react-query";
import { DataTable, useDataTableColumns } from "mantine-datatable";
import { useTranslation } from "react-i18next";

import TableActionButton from "~/component/admin/TableActionButton";
import { DEFAULT_PAGE_SIZE, queryClient, queryFactory } from "~/services/Api";
import TimeService from "~/services/TimeService";
import Validators from "~/services/Validators";

import type { CommunicationDtoResponse } from "~/@types/api";
import type { DataTableSortStatus } from "mantine-datatable";
import type { Route } from "./+types/communications";

const COLUMNS_STATE_KEY = "logs-communications-table-columns";

const DEFAULT_PAGE = 0;
const DEFAULT_SORTING = {
	columnAccessor: "id",
	direction: "asc"
} as const;

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	if (id) Validators.validateUuids(id);

	const pageable = {
		page: DEFAULT_PAGE,
		sorting: [DEFAULT_SORTING]
	};

	await queryClient.query(
		id
			? queryFactory.accommodations.communications.pagedList(id, pageable)
			: queryFactory.communications.pagedList(pageable)
	);

	return {
		accommodations: await queryClient.query(queryFactory.accommodations.list())
	};
}

export default function LogsCommunicationsPage({
	loaderData: { accommodations },
	params: { id }
}: Route.ComponentProps) {
	const navigate = useNavigate();

	const { t } = useTranslation("routes", {
		keyPrefix: "admin.logs.communications"
	});
	const { t: tCommunication } = useTranslation("entities", {
		keyPrefix: "booking.communications"
	});
	const { t: tEntity } = useTranslation("entities");

	const [page, setPage] = useState(DEFAULT_PAGE);
	const [sortStatus, setSortStatus] =
		useState<DataTableSortStatus<CommunicationDtoResponse>>(DEFAULT_SORTING);

	const pageable = {
		page: page,
		sorting: [sortStatus]
	};

	const { data, isLoading } = useQuery(
		id
			? queryFactory.accommodations.communications.pagedList(id, pageable)
			: queryFactory.communications.pagedList(pageable)
	);

	const { effectiveColumns } = useDataTableColumns<CommunicationDtoResponse>({
		key: COLUMNS_STATE_KEY,
		columns: [
			{
				hidden: !!id,
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "accommodationName",
				title: tCommunication(($) => $.accommodation.label)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "type",
				title: tCommunication(($) => $.type.label),
				render: (communication) => (
					<Badge
						variant="light"
						color={tCommunication(
							($) => $.type.options[communication.type].color
						)}
					>
						{tCommunication(($) => $.type.options[communication.type].label)}
					</Badge>
				)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "status",
				title: tCommunication(($) => $.status.label),
				render: (communication) => (
					<Badge
						variant="light"
						color={tCommunication(
							($) => $.status.states[communication.status].color
						)}
					>
						{tCommunication(($) => $.status.states[communication.status].label)}
					</Badge>
				)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "sentTimestamp",
				title: tCommunication(($) => $.sentTimestamp.label),
				render: (communication) =>
					communication.sentTimestamp
						? TimeService(communication.sentTimestamp).format("LLL")
						: tCommunication(($) => $.sentTimestamp.none)
			},
			{
				draggable: true,
				sortable: true,
				resizable: true,
				accessor: "error",
				title: tCommunication(($) => $.error.label),
				render: (communication) =>
					communication.error ?? tCommunication(($) => $.error.none)
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
						tEntity(($) => $.common.createdAt.format)
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
				render: (communication) => (
					<Group gap={4} wrap="nowrap" justify="center">
						<TableActionButton
							component={Link}
							to={`/admin/accommodations/${communication.accommodationId}`}
							size="sm"
							variant="subtle"
							color={t(($) => $.tableButtons.viewAccommodation.color)}
							tooltip={t(($) => $.tableButtons.viewAccommodation.tooltip)}
						>
							<BedIcon />
						</TableActionButton>
						<TableActionButton
							component={Link}
							to={`/accommodations/${communication.accommodationId}/bookings/${communication.bookingId}`}
							size="sm"
							variant="subtle"
							color={t(($) => $.tableButtons.viewBooking.color)}
							tooltip={t(($) => $.tableButtons.viewBooking.tooltip)}
						>
							<CalendarIcon />
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
					<Select
						placeholder={t(($) => $.accommodationSelector.label)}
						value={id}
						onChange={(value) => {
							void navigate(
								value
									? `/admin/logs/communications/${value}`
									: "/admin/logs/communications"
							);
						}}
						clearable
						searchable
						data={accommodations.map((accommodation) => ({
							value: accommodation.id,
							label: accommodation.name
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
