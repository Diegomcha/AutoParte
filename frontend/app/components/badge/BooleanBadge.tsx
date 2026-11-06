import { Badge } from "@mantine/core";

import { merge } from "lodash";
import { useTranslation } from "react-i18next";

import type { ReactNode } from "react";

export default function BooleanBadge({
	value,
	displayOpts
}: Readonly<{
	value?: boolean;
	displayOpts?: {
		true?: { label?: string; color?: string; icon?: ReactNode };
		false?: { label?: string; color?: string; icon?: ReactNode };
		null?: { label?: string; color?: string; icon?: ReactNode };
	};
}>) {
	const { t } = useTranslation("components", {
		keyPrefix: "booleanBadge"
	});

	const parsedValue = String(value) as "true" | "false" | "null";

	const display = merge(
		{},
		t(($) => $.states[parsedValue], { returnObjects: true }),
		displayOpts?.[parsedValue]
	);

	return (
		<Badge color={display.color} variant="light" leftSection={display.icon}>
			{display.label}
		</Badge>
	);
}
