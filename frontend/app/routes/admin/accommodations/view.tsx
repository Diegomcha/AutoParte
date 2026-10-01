import { Link, useNavigate } from "react-router";

import { Badge, DataList, Divider, Modal, Stack } from "@mantine/core";

import { useTranslation } from "react-i18next";

import WifiBadge from "~/component/badge/WifiBadge";
import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import TimeService from "~/services/TimeService";
import Validators from "~/services/Validators";

import type { Route } from "./+types/view";

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	Validators.validateUuids(id);

	return {
		accommodation: await queryClient.query(
			queryFactory.accommodations.detail(id)
		)
	};
}

export default function ViewAccommodation({
	loaderData: { accommodation }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation("routes", {
		keyPrefix: "admin.accommodations.view"
	});
	const { t: tAccommodation } = useTranslation("entities", {
		keyPrefix: "accommodation"
	});
	const { t: tEntity } = useTranslation("entities", {
		keyPrefix: "common"
	});

	const { opened, close } = useStaticModalTransition(
		() => void navigate("/admin/accommodations")
	);

	return (
		<Modal opened={opened} onClose={close} title={t(($) => $.title)}>
			<DataList labelWidth={160}>
				<DataList.Item>
					<DataList.ItemLabel>
						{tEntity(($) => $.createdAt.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{TimeService(accommodation.createdAt).fromNow()}
					</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{tEntity(($) => $.updatedAt.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{TimeService(accommodation.updatedAt).fromNow()}
					</DataList.ItemValue>
				</DataList.Item>
				<Divider />
				<DataList.Item>
					<DataList.ItemLabel>
						{tAccommodation(($) => $.name.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{accommodation.name}</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{tAccommodation(($) => $.sesCode.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{accommodation.sesCode}</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{tAccommodation(($) => $.internetConnection.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						<WifiBadge value={accommodation.internetConnection} />
					</DataList.ItemValue>
				</DataList.Item>
				<Divider />
				<DataList.Item>
					<DataList.ItemLabel>
						{tAccommodation(($) => $.employees.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{accommodation.employees.length === 0 ? (
							tAccommodation(($) => $.employees.value, { count: 0 })
						) : (
							<Stack gap={4}>
								{accommodation.employees.map((employee) => (
									<Badge
										component={Link}
										to={`/admin/employees/${employee.id}`}
										key={employee.id}
										variant="dot"
										style={{ cursor: "pointer" }}
									>
										{employee.name} &lt;{employee.email}&gt;
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
