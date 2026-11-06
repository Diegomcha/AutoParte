import { Text, Tooltip } from "@mantine/core";

import { useTranslation } from "react-i18next";

interface ComplexRequiredAsteriskProps {
	action: "confirm" | "checkIn";
}

export default function ComplexRequiredAsterisk({
	action
}: Readonly<ComplexRequiredAsteriskProps>) {
	const { t } = useTranslation("components", {
		keyPrefix: "complexRequiredAsterisk"
	});
	const { t: tBookingRoute } = useTranslation("routes", {
		keyPrefix: "bookings"
	});

	return (
		<Tooltip label={t(($) => $.tooltips[action])}>
			<Text
				component="span"
				style={{ cursor: "help" }}
				c={tBookingRoute(($) => $[action].color)}
				aria-hidden
			>
				{" "}
				*
			</Text>
		</Tooltip>
	);
}
