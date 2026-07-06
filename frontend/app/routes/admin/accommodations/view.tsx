import { Badge, DataList, Divider, Modal, Stack } from '@mantine/core';
import api, { queryClient, throwErrors } from '~/api';
import WifiBadge from '~/component/WifiBadge';
import Validators from '~/services/Validators';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router';
import type { Route } from './+types/view';

dayjs.extend(relativeTime);

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	Validators.validateUuids(id);

	return {
		accommodation: await queryClient.fetchQuery({
			queryKey: ['accommodations', id],
			queryFn: async () =>
				throwErrors(
					await api.GET('/api/accommodations/{id}', {
						params: { path: { id } },
					})
				),
		}),
	};
}

export default function ViewAccommodation({
	loaderData: { accommodation },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	return (
		<Modal
			opened
			onClose={() => void navigate('/admin/accommodations')}
			title={t(($) => $.admin.accommodations.view.title)}
		>
			<DataList labelWidth={160}>
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.common.properties.createdAt)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{dayjs(accommodation.createdAt).fromNow()}
					</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.common.properties.updatedAt)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{dayjs(accommodation.updatedAt).fromNow()}
					</DataList.ItemValue>
				</DataList.Item>
				<Divider />
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.admin.accommodations.properties.name.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{accommodation.name}</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.admin.accommodations.properties.sesCode.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{accommodation.sesCode}</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{t(
							($) => $.admin.accommodations.properties.internetConnection.label
						)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						<WifiBadge value={accommodation.internetConnection} />
					</DataList.ItemValue>
				</DataList.Item>
				<Divider />
				<DataList.Item>
					<DataList.ItemLabel>
						{t(($) => $.admin.accommodations.properties.employees.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{accommodation.employees.length === 0 ? (
							t(($) => $.admin.accommodations.properties.employees.none)
						) : (
							<Stack gap={4}>
								{accommodation.employees.map((employee) => (
									<Badge
										component={Link}
										to={`/admin/employees/${employee.id}`}
										key={employee.id}
										variant="dot"
										className="cursor-pointer!"
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
