import { Text, Tooltip } from '@mantine/core';
import { useTranslation } from 'react-i18next';

interface ComplexRequiredAsteriskProps {
	action: 'confirm' | 'checkIn';
}

export default function ComplexRequiredAsterisk({
	action,
}: Readonly<ComplexRequiredAsteriskProps>) {
	const { t } = useTranslation();

	return (
		<Tooltip label={t(($) => $.complexRequiredLabel.tooltips[action])}>
			<Text
				component="span"
				style={{ cursor: 'help' }}
				c={t(($) => $.bookings[action].color)}
				aria-hidden
			>
				{' '}
				*
			</Text>
		</Tooltip>
	);
}
