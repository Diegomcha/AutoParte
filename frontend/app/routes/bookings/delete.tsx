import { Button, Group, Modal } from '@mantine/core';
import { TrashIcon } from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import { queryClient, queryFactory } from '~/services/Api';
import Validators from '~/services/Validators';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import type { Route } from './+types/confirm';

export async function clientLoader({
	params: { accommodationId, bookingId },
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const booking = await queryClient.query(
		queryFactory.accommodations.bookings.detail(accommodationId, bookingId)
	);

	if (!booking.canBeDeleted)
		throw Validators.throwValidationErrorResponse(
			'Booking is not in a state that allows deletion.'
		);
}

export default function DeleteBooking({
	params: { accommodationId, bookingId },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	function goBack() {
		void navigate('/');
	}

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.bookings.delete(accommodationId, bookingId)
	);

	return (
		<Modal opened onClose={goBack} title={t(($) => $.bookings.delete.title)}>
			{t(($) => $.bookings.delete.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={goBack} color="gray">
					{t(($) => $.common.buttons.cancel)}
				</Button>
				<Button
					color={t(($) => $.bookings.delete.color)}
					loading={isPending}
					leftSection={<TrashIcon weight="bold" />}
					onClick={() => {
						mutate(undefined, {
							onSuccess: goBack,
						});
					}}
				>
					{t(($) => $.bookings.delete.button)}
				</Button>
			</Group>
		</Modal>
	);
}
