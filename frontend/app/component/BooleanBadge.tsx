import { Badge } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import type { ReactNode } from 'react';

export default function BooleanBadge({
	value,
	icons,
}: Readonly<{
	value?: boolean;
	icons?: { true: ReactNode; false: ReactNode; undefined?: ReactNode };
}>) {
	const { t } = useTranslation();

	let color = 'gray';
	let text = t(($) => $.common.boolean.undefined);
	let icon = icons?.undefined;

	if (value === true) {
		color = 'green';
		text = t(($) => $.common.boolean.true);
		icon = icons?.true;
	} else if (value === false) {
		color = 'red';
		text = t(($) => $.common.boolean.false);
		icon = icons?.false;
	}

	return (
		<Badge color={color} variant="light" leftSection={icon}>
			{text}
		</Badge>
	);
}
