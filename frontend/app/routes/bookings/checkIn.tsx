import { Button, Group, Modal } from '@mantine/core';
import { SuitcaseIcon } from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import Validators from '~/services/Validators';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import type { Route } from './+types/confirm';

export async function clientLoader({
	params: { accommodationId, bookingId },
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const booking = await queryClient.fetchQuery({
		queryKey: ['bookings', accommodationId, bookingId],
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/accommodations/{accommodationId}/bookings/{id}', {
					params: { path: { accommodationId, id: bookingId } },
				})
			),
	});

	if (booking.status !== 'CHECK_IN_READY')
		throw Validators.throwValidationErrorResponse(
			'Booking is not in a state that allows check-in.'
		);
}

export default function CheckInBooking({
	params: { accommodationId, bookingId },
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
				await api.POST(
					'/api/accommodations/{accommodationId}/bookings/{id}/check-in',
					{
						params: { path: { accommodationId, id: bookingId } },
					}
				)
			),
		onSuccess: async () => {
			await queryClient.invalidateQueries({
				queryKey: ['bookings'],
			});

			goBack();
		},
	});

	return (
		<Modal opened onClose={goBack} title={t(($) => $.bookings.checkIn.title)}>
			{t(($) => $.bookings.checkIn.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={goBack} color="gray">
					{t(($) => $.common.buttons.cancel)}
				</Button>
				<Button
					color="cyan"
					leftSection={<SuitcaseIcon weight="bold" />}
					loading={isPending}
					onClick={() => {
						mutate();
					}}
				>
					{t(($) => $.bookings.checkIn.button)}
				</Button>
			</Group>
		</Modal>
	);
}
