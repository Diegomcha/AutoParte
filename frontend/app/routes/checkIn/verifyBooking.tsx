import { useNavigate } from "react-router";

import {
	Button,
	Checkbox,
	Code,
	DataList,
	Divider,
	Group,
	Paper,
	Stack
} from "@mantine/core";
import { Calendar } from "@mantine/dates";
import { useForm } from "@mantine/form";

import { SealCheckIcon } from "@phosphor-icons/react";
import { useTranslation } from "react-i18next";

import { lang } from "~/i18n";
import TimeService from "~/services/TimeService";

import { useCheckInRouteContext } from "./layout";

export default function CheckInVerifyBookingRoute() {
	const navigate = useNavigate();
	const { booking } = useCheckInRouteContext();

	const { t } = useTranslation("routes", {
		keyPrefix: "checkIn.verify-booking"
	});
	const { t: tEntities } = useTranslation("entities");

	const startTime = TimeService(booking.startTime);
	const endTime = TimeService(booking.endTime);

	function inRange(date: string) {
		return TimeService(date).isBetween(startTime, endTime, "day", "[]");
	}

	const form = useForm({
		mode: "uncontrolled",
		initialValues: {
			confirmation: false
		},
		validate: {
			confirmation: (value) => (value ? null : t(($) => $.confirmation.error))
		}
	});

	return (
		<Stack gap="lg">
			<Group gap="xl">
				<Paper withBorder shadow="none">
					<Calendar
						static
						fullWidth
						maw={350}
						highlightToday
						defaultDate={startTime.toDate()}
						minDate={startTime.toDate()}
						maxDate={endTime.toDate()}
						maxLevel="month"
						getDayProps={(date) => {
							const dateObj = TimeService(date);
							return {
								selected:
									dateObj.isSame(startTime, "day") ||
									dateObj.isSame(endTime, "day"),
								inRange: inRange(date),
								firstInRange: dateObj.isSame(startTime, "day"),
								lastInRange: dateObj.isSame(endTime, "day"),
								disabled: false
							};
						}}
						locale={lang}
					/>
				</Paper>
				<DataList flex={1} size="md" gap="md" labelWidth={150}>
					<DataList.Item>
						<DataList.ItemLabel>
							{tEntities(($) => $.booking.details.accommodation.label)}
						</DataList.ItemLabel>
						<DataList.ItemValue>{booking.accommodationName}</DataList.ItemValue>
					</DataList.Item>
					<Divider />
					<DataList.Item>
						<DataList.ItemLabel>
							{tEntities(($) => $.booking.details.startTime.label)}
						</DataList.ItemLabel>
						<DataList.ItemValue>{startTime.format("LL")}</DataList.ItemValue>
					</DataList.Item>
					<DataList.Item>
						<DataList.ItemLabel>
							{tEntities(($) => $.booking.details.endTime.label)}
						</DataList.ItemLabel>
						<DataList.ItemValue>{endTime.format("LL")}</DataList.ItemValue>
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
					<Divider />
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
			</Group>
			<form onSubmit={form.onSubmit(() => navigate("../input-guest-details"))}>
				<Checkbox
					size="md"
					label={t(($) => $.confirmation.label)}
					key={form.key("confirmation")}
					{...form.getInputProps("confirmation", { type: "checkbox" })}
				/>
				<Group justify="right">
					<Button
						size="md"
						color="green"
						leftSection={<SealCheckIcon weight="bold" />}
						type="submit"
					>
						{t(($) => $.buttons.verify)}
					</Button>
				</Group>
			</form>
		</Stack>
	);
}
