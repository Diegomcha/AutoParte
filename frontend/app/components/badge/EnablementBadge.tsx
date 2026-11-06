import { Badge, Tooltip } from "@mantine/core";

import { LockIcon, LockOpenIcon } from "@phosphor-icons/react";
import { useTranslation } from "react-i18next";

import TimeService from "~/services/TimeService";

const ICONS = {
	enabled: <LockOpenIcon weight="bold" />,
	disabled: <LockIcon weight="bold" />
};

export default function EnablementBadge({
	enabled,
	disabledAt
}: Readonly<{
	enabled: boolean;
	disabledAt?: Date | null;
}>) {
	const { t } = useTranslation("components", {
		keyPrefix: "enablementBadge"
	});

	const parsedValue = enabled ? "enabled" : "disabled";

	const stateTranslations = t(($) => $.states[parsedValue], {
		returnObjects: true
	});

	return (
		<Tooltip
			label={t(($) => $.disabledTooltip, {
				date: disabledAt && TimeService(disabledAt).format("LLL")
			})}
			withArrow
			disabled={!disabledAt}
		>
			<Badge
				color={stateTranslations.color}
				variant="light"
				leftSection={ICONS[parsedValue]}
			>
				{stateTranslations.label}
			</Badge>
		</Tooltip>
	);
}
