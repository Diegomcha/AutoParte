import { Button, Group, Modal } from '@mantine/core';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import { useTranslation } from 'react-i18next';
import { useNavigate, useRevalidator } from 'react-router';
import type { Route } from './+types/delete';
import Validators from '~/services/Validators';

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	Validators.validateUuids(id);

	return await queryClient.fetchQuery({
		queryKey: ['employee', id],
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/employees/{id}', {
					params: { path: { id } },
				})
			),
	});
}

export default function DeleteEmployee({
	params: { id },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const revalidator = useRevalidator();
	const { t } = useTranslation();

	const { mutate, isPending } = useMutation({
		throwOnError: true,
		mutationFn: async () =>
			throwErrors(
				await api.DELETE('/api/employees/{id}', {
					params: { path: { id } },
				})
			),
		onSuccess: async () => {
			await queryClient.invalidateQueries({ queryKey: ['employees'] });
			await revalidator.revalidate();

			await navigate('/admin/employees');
		},
	});

	return (
		<>
			<div hidden={revalidator.state === 'idle'}>Revalidating...</div>
			<Modal
				opened
				onClose={() => void navigate('/admin/employees')}
				title={t(($) => $.admin.employees.delete.title)}
			>
				{t(($) => $.admin.employees.delete.description)}

				<Group justify="right" mt="md" gap="xs">
					<Button
						onClick={() => void navigate('/admin/employees')}
						color="gray"
					>
						{t(($) => $.common.buttons.cancel)}
					</Button>
					<Button
						color="red"
						loading={isPending}
						onClick={() => {
							mutate();
						}}
					>
						{t(($) => $.common.buttons.delete)}
					</Button>
				</Group>
			</Modal>
		</>
	);
}
