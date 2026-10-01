import { Link, useNavigate } from "react-router";

import { Badge, Chip, DataList, Divider, Modal, Stack } from "@mantine/core";

import { CheckCircleIcon } from "@phosphor-icons/react";
import { useTranslation } from "react-i18next";

import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import TimeService from "~/services/TimeService";
import Validators from "~/services/Validators";

import type { Route } from "./+types/view";

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	Validators.validateUuids(id);

	return {
		employee: await queryClient.query(queryFactory.employees.detail(id))
	};
}

export default function ViewEmployee({
	loaderData: { employee }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation("routes", {
		keyPrefix: "admin.employees.view"
	});
	const { t: tEmployee } = useTranslation("entities", {
		keyPrefix: "employee"
	});
	const { t: tEntity } = useTranslation("entities", {
		keyPrefix: "common"
	});

	const { opened, close } = useStaticModalTransition(
		() => void navigate("/admin/employees")
	);

	return (
		<Modal opened={opened} onClose={close} title={t(($) => $.title)}>
			<DataList labelWidth={160}>
				<DataList.Item>
					<DataList.ItemLabel>
						{tEmployee(($) => $.enabled.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						<Chip
							readOnly
							checked={employee.enabled}
							icon={<CheckCircleIcon />}
							color="green"
							variant="light"
						>
							{tEmployee(($) =>
								employee.enabled
									? $.enabled.states.enabled
									: $.enabled.states.disabled
							)}
						</Chip>
					</DataList.ItemValue>
				</DataList.Item>
				<Divider />
				<DataList.Item>
					<DataList.ItemLabel>
						{tEntity(($) => $.createdAt.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{TimeService(employee.createdAt).fromNow()}
					</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{tEntity(($) => $.updatedAt.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{TimeService(employee.updatedAt).fromNow()}
					</DataList.ItemValue>
				</DataList.Item>
				<Divider />
				<DataList.Item>
					<DataList.ItemLabel>
						{tEmployee(($) => $.name.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{employee.name}</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{tEmployee(($) => $.surname.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{employee.surname}</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{tEmployee(($) => $.email.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{employee.email}</DataList.ItemValue>
				</DataList.Item>
				<Divider />
				<DataList.Item>
					<DataList.ItemLabel>
						{tEmployee(($) => $.accommodations.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{employee.accommodations.length === 0 ? (
							tEmployee(($) => $.accommodations.value, { count: 0 })
						) : (
							<Stack gap={4}>
								{employee.accommodations.map((accommodation) => (
									<Badge
										component={Link}
										to={`/admin/accommodations/${accommodation.id}`}
										key={accommodation.id}
										variant="dot"
										style={{ cursor: "pointer" }}
									>
										{accommodation.name}
									</Badge>
								))}
							</Stack>
						)}
					</DataList.ItemValue>
				</DataList.Item>
			</DataList>
		</Modal>
	);
}
