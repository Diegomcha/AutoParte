import { Group, Stack, Text } from '@mantine/core';
import TimeService from '~/services/TimeService';
import { useTranslation } from 'react-i18next';
import BoookingStatusBadge from './BookingStatusBadge';
import type { BookingDtoResponse } from '~/@types/api';

export default function BookingHoverCard({
	booking,
}: Readonly<{ booking: BookingDtoResponse }>) {
	const { t } = useTranslation();

	return (
		<Stack gap="xs">
			<Group gap="sm">
				<BoookingStatusBadge status={booking.status} />
				<Text fw={'bold'} size="sm">
					{t(
						($) =>
							booking.holderName
								? $.bookings.properties.details.name.withHolder
								: $.bookings.properties.details.name.noHolder,
						{
							status: booking.status,
							numberOfPeople: booking.numberOfPeople,
							holderName: booking.holderName,
						}
					)}
				</Text>
			</Group>
			<Text size="xs" c="dimmed">
				{TimeService(booking.startTime).format('LLL')} →{' '}
				{TimeService(booking.endTime).format('LLL')}
			</Text>
		</Stack>
	);
}
