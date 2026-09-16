import { useNavigate } from "react-router";

import { Alert, Button, Group, Modal, Space } from "@mantine/core";

import { SuitcaseIcon, WarningIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import TimeService from "~/services/TimeService";
import Validators from "~/services/Validators";

import type { Route } from "./+types/checkIn";

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const booking = await queryClient.query(
		queryFactory.accommodations.bookings.detail(accommodationId, bookingId)
	);

	if (!booking.canBeCheckedIn)
		throw Validators.throwValidationErrorResponse(
			"Booking is not in a state that allows check-in."
		);

	return {
		isCheckInDay: TimeService(booking.startTime).isSame(TimeService(), "day")
	};
}

export default function CheckInBooking({
	params: { accommodationId, bookingId },
	loaderData: { isCheckInDay }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation("routes", {
		keyPrefix: "bookings.checkIn"
	});
	const { t: tCommon } = useTranslation();

	const { opened, close } = useStaticModalTransition(() => void navigate(".."));

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.bookings.checkIn(accommodationId, bookingId)
	);

	return (
		<Modal opened={opened} onClose={close} title={t(($) => $.title)}>
			{!isCheckInDay && (
				<>
					<Alert color="yellow" icon={<WarningIcon weight="bold" />}>
						{t(($) => $.notCheckInDayWarning)}
					</Alert>
					<Space h="md" />
				</>
			)}
			{t(($) => $.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={close} color="gray">
					{tCommon(($) => $.buttons.cancel)}
				</Button>
				<Button
					leftSection={<SuitcaseIcon weight="bold" />}
					color={t(($) => $.color)}
					loading={isPending}
					onClick={() => {
						mutate(undefined, {
							onSuccess: close
						});
					}}
				>
					{t(($) => $.button)}
				</Button>
			</Group>
		</Modal>
	);
}
