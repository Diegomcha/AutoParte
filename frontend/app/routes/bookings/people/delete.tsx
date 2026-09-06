import { Button, Group, Modal } from '@mantine/core';
import { useMutation } from '@tanstack/react-query';
import { queryFactory, queryClient } from '~/services/Api';
import Validators from '~/services/Validators';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import { useResetPerson } from '.';
import type { Route } from './+types/delete';

export async function clientLoader({
	params: { accommodationId, bookingId, id },
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId, id);

	await queryClient.query(
		queryFactory.accommodations.bookings.people.detail(
			accommodationId,
			bookingId,
			id
		)
	);
}

export default function DeletePerson({
	params: { accommodationId, bookingId, id },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const resetPerson = useResetPerson();

	function goBack() {
		void navigate('..');
	}

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.bookings.people.delete(
			accommodationId,
			bookingId,
			id
		)
	);

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
						mutate(undefined, {
							onSuccess: () => {
								resetPerson();
								goBack();
							},
						});
					}}
				>
					{t(($) => $.common.buttons.delete)}
				</Button>
			</Group>
		</Modal>
	);
}
