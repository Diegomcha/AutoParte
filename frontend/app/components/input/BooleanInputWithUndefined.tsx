import { Group, Radio } from "@mantine/core";

import { useTranslation } from "react-i18next";

export default function BooleanInputWithUndefined(
	props: Omit<Readonly<Radio.Group.Props>, "children">
) {
	const { t } = useTranslation("components", {
		keyPrefix: "booleanInputWithUndefined"
	});

	return (
		<Radio.Group {...props}>
			<Group mt="xs">
				<Radio value="undefined" label={t(($) => $.options.undefined)} />
				<Radio value="false" label={t(($) => $.options.false)} />
				<Radio value="true" label={t(($) => $.options.true)} />
			</Group>
		</Radio.Group>
	);
}
