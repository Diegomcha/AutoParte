import { Code, DataList, Space, Stack, Title } from "@mantine/core";

import { useTranslation } from "react-i18next";

import TimeService from "~/services/TimeService";

import type { BookingDtoResponse } from "~/@types/api";

export default function CheckInBookingDetailsSidebar({
	booking,
	...props
}: React.ComponentProps<typeof Stack> & {
	booking: BookingDtoResponse;
}) {
	const { t } = useTranslation("components", {
		keyPrefix: "checkIn.bookingDetails"
	});
	const { t: tEntities } = useTranslation("entities");

	const startTime = TimeService(booking.startTime);
	const endTime = TimeService(booking.endTime);

	return (
		<Stack p="xl" gap="lg" {...props}>
			<Title order={4} size="h5">
				{t(($) => $.title)}
			</Title>
			<DataList component="section">
				<DataList.Item>
					<DataList.ItemLabel>
						{tEntities(($) => $.booking.details.accommodation.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{booking.accommodationName}</DataList.ItemValue>
				</DataList.Item>
				<Space />
				<DataList.Item>
					<DataList.ItemLabel>
						{tEntities(($) => $.booking.details.startTime.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{startTime.format("L")}</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{tEntities(($) => $.booking.details.endTime.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>{endTime.format("L")}</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{tEntities(($) => $.booking.details.duration.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{tEntities(($) => $.booking.details.duration.value, {
							count: endTime.diff(startTime, "days")
						})}
					</DataList.ItemValue>
				</DataList.Item>
				<Space />
				<DataList.Item>
					<DataList.ItemLabel>
						{tEntities(($) => $.booking.details.numberOfPeople.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{tEntities(($) => $.booking.details.numberOfPeople.value, {
							count: booking.numberOfPeople
						})}
					</DataList.ItemValue>
				</DataList.Item>
				<DataList.Item>
					<DataList.ItemLabel>
						{tEntities(($) => $.booking.details.numberOfRooms.label)}
					</DataList.ItemLabel>
					<DataList.ItemValue>
						{tEntities(($) => $.booking.details.numberOfRooms.value, {
							count: booking.numberOfRooms ?? 0
						})}
					</DataList.ItemValue>
				</DataList.Item>
			</DataList>
		</Stack>
	);
}
