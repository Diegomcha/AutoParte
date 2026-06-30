import { Group, Radio } from '@mantine/core';
import { useTranslation } from 'react-i18next';

export default function BooleanInputWithUndefined(
	props: Omit<Readonly<Radio.Group.Props>, 'children'>
) {
	const { t } = useTranslation();

	return (
		<Radio.Group {...props}>
			<Group mt="xs">
				<Radio value="undefined" label={t(($) => $.common.boolean.undefined)} />
				<Radio value="false" label={t(($) => $.common.boolean.false)} />
				<Radio value="true" label={t(($) => $.common.boolean.true)} />
			</Group>
		</Radio.Group>
	);
}
