import { Button, Group, Modal } from '@mantine/core';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import Validators from '~/services/Validators';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import type { Route } from './+types/delete';

export async function clientLoader({
	params: { accommodationId, bookingId, id },
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId, id);

	await queryClient.fetchQuery({
		queryKey: ['bookings', accommodationId, bookingId, 'people', id],
		queryFn: async () =>
			throwErrors(
				await api.GET(
					'/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}',
					{
						params: { path: { accommodationId, bookingId, id } },
					}
				)
			),
	});
}

export default function DeletePerson({
	params: { accommodationId, bookingId, id },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	function goBack() {
		void navigate('..');
	}

	const { mutate, isPending } = useMutation({
		throwOnError: true,
		mutationFn: async () =>
			throwErrors(
				await api.DELETE(
					'/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}',
					{
						params: { path: { accommodationId, bookingId, id } },
					}
				)
			),
		onSuccess: async () => {
			await queryClient.invalidateQueries({
				queryKey: ['bookings', accommodationId, bookingId, 'people'],
			});

			goBack();
		},
	});

	return (
		<Modal opened onClose={goBack} title={t(($) => $.people.delete.title)}>
			{t(($) => $.people.delete.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={goBack} color="gray">
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
	);
}
