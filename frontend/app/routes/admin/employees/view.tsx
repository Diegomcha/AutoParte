import { Badge, Chip, DataList, Divider, Modal, Stack } from '@mantine/core';
import { CheckCircleIcon } from '@phosphor-icons/react';
import api, { queryClient, throwErrors } from '~/api';
import TimeService from '~/services/TimeService';
import Validators from '~/services/Validators';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router';
import type { Route } from './+types/view';

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	Validators.validateUuids(id);

	return {
		employee: await queryClient.fetchQuery({
			queryKey: ['employee', id],
			queryFn: async () =>
				throwErrors(
					await api.GET('/api/employees/{id}', {
						params: { path: { id } },
					})
				),
		}),
	};
}

export default function ViewEmployee({
	loaderData: { employee },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	return (
		<Modal
			opened
			onClose={() => void navigate('/admin/employees')}
			title={t(($) => $.admin.employees.view.title)}
		>
			<DataList labelWidth={160}>
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.admin.employees.properties.enabled.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						<Chip
							readOnly
							checked={employee.enabled}
							icon={<CheckCircleIcon />}
							color="green"
							variant="light"
						>
							{employee.enabled
								? t(($) => $.admin.employees.properties.enabled.states.enabled)
								: t(
										($) => $.admin.employees.properties.enabled.states.disabled
									)}
						</Chip>
					</DataList.ItemValue>
				</DataList.Item>
				<Divider />
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.common.properties.createdAt)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{TimeService(employee.createdAt).fromNow()}
					</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.common.properties.updatedAt)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{TimeService(employee.updatedAt).fromNow()}
					</DataList.ItemValue>
				</DataList.Item>
				<Divider />
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.admin.employees.properties.name.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{employee.name}</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.admin.employees.properties.surname.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{employee.surname}</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.admin.employees.properties.email.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{employee.email}</DataList.ItemValue>
				</DataList.Item>
				<Divider />
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.admin.employees.properties.accommodations.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{employee.accommodations.length === 0 ? (
							t(($) => $.admin.employees.properties.accommodations.none)
						) : (
							<Stack gap={4}>
								{employee.accommodations.map((accommodation) => (
									<Badge
										component={Link}
										to={`/admin/accommodations/${accommodation.id}`}
										key={accommodation.id}
										variant="dot"
										className="cursor-pointer!"
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
