import { Badge } from "@mantine/core";

import { useTranslation } from "react-i18next";

import type { BookingDtoResponse } from "~/@types/api";

export default function BookingStatusBadge({
	status
}: Readonly<{ status: BookingDtoResponse["status"] }>) {
	const { t: tBooking } = useTranslation("entities", { keyPrefix: "booking" });

	return (
		<Badge color={tBooking(($) => $.details.status.states[status].color)}>
			{tBooking(($) => $.details.status.states[status].label)}
		</Badge>
	);
}
