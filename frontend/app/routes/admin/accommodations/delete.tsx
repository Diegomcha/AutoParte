import { Button, Group, Modal } from '@mantine/core';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import { useTranslation } from 'react-i18next';
import { useNavigate, useRevalidator } from 'react-router';
import type { Route } from './+types/delete';

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	return await queryClient.fetchQuery({
		queryKey: ['accommodations', id],
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/accommodations/{id}', {
					params: { path: { id } },
				})
			),
	});
}

export default function DeleteAccommodation({
	params: { id },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const revalidator = useRevalidator();
	const { t } = useTranslation();

	const { mutate, isPending } = useMutation({
		throwOnError: true,
		mutationFn: async () =>
			throwErrors(
				await api.DELETE('/api/accommodations/{id}', {
					params: { path: { id } },
				})
			),
		onSuccess: async () => {
			await queryClient.invalidateQueries({ queryKey: ['accommodations'] });
			await revalidator.revalidate();

			await navigate('/admin/accommodations');
		},
	});

	return (
		<>
			<div hidden={revalidator.state === 'idle'}>Revalidating...</div>
			<Modal
				opened
				onClose={() => void navigate('/admin/accommodations')}
				title={t(($) => $.admin.accommodations.delete.title)}
			>
				{t(($) => $.admin.accommodations.delete.description)}

				<Group justify="right" mt="md" gap="xs">
					<Button
						onClick={() => void navigate('/admin/accommodations')}
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
