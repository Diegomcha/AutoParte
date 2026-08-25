import { Badge } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import type { BookingDtoResponse } from '~/@types/api';

export default function BookingStatusBadge({
	status,
}: Readonly<{ status: BookingDtoResponse['status'] }>) {
	const { t } = useTranslation();

	return (
		<Badge
			color={t(
				($) => $.bookings.properties.details.status.states[status].color
			)}
		>
			{t(($) => $.bookings.properties.details.status.states[status].label)}
		</Badge>
	);
}
