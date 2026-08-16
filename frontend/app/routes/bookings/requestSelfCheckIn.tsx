import { Button, Group, Modal } from '@mantine/core';
import { PaperPlaneTiltIcon } from '@phosphor-icons/react';
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

	if (['PENDING_CANCELLATION', 'CANCELLED'].includes(booking.status))
		throw Validators.throwValidationErrorResponse(
			'Booking is not in a state that allows requesting self-check-in.'
		);
}

export default function RequestSelfCheckInForBooking({
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
					'/api/accommodations/{accommodationId}/bookings/{id}/request-self-check-in',
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
		<Modal
			opened
			onClose={goBack}
			title={t(($) => $.bookings.requestSelfCheckIn.title)}
		>
			{t(($) => $.bookings.requestSelfCheckIn.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={goBack} color="gray">
					{t(($) => $.common.buttons.cancel)}
				</Button>
				<Button
					leftSection={<PaperPlaneTiltIcon weight="bold" />}
					color="violet"
					loading={isPending}
					onClick={() => {
						mutate();
					}}
				>
					{t(($) => $.bookings.requestSelfCheckIn.button)}
				</Button>
			</Group>
		</Modal>
	);
}
