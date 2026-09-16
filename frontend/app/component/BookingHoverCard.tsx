import { Group, Stack, Text } from "@mantine/core";

import { useTranslation } from "react-i18next";

import TimeService from "~/services/TimeService";

import BoookingStatusBadge from "./BookingStatusBadge";

import type { BookingDtoResponse } from "~/@types/api";

export default function BookingHoverCard({
	booking
}: Readonly<{ booking: BookingDtoResponse }>) {
	const { t: tBooking } = useTranslation("entities", { keyPrefix: "booking" });

	return (
		<Stack gap="xs">
			<Group gap="sm">
				<BoookingStatusBadge status={booking.status} />
				<Text fw={"bold"} size="sm">
					{tBooking(
						($) =>
							booking.holderName
								? $.details.name.withHolder
								: $.details.name.noHolder,
						{
							status: booking.status,
							numberOfPeople: booking.numberOfPeople,
							holderName: booking.holderName
						}
					)}
				</Text>
			</Group>
			<Text size="xs" c="dimmed">
				{TimeService(booking.startTime).format("LLL")} →{" "}
				{TimeService(booking.endTime).format("LLL")}
			</Text>
		</Stack>
	);
}
