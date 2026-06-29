import { Center } from '@mantine/core';
import { useTranslation } from 'react-i18next';

export default function Dashboard() {
	const { t } = useTranslation();
	return (
		<Center>
			<p>{t(($) => $.bookings.selectAccommodation)}</p>
		</Center>
	);
}
