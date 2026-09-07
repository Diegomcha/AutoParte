import { useNavigate } from "react-router";

import { Button, Group, Modal } from "@mantine/core";

import { PaperPlaneTiltIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { Route } from "./+types/requestSelfCheckIn";

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const booking = await queryClient.query(
		queryFactory.accommodations.bookings.detail(accommodationId, bookingId)
	);

	if (["PENDING_CANCELLATION", "CANCELLED"].includes(booking.status))
		throw Validators.throwValidationErrorResponse(
			"Booking is not in a state that allows requesting self-check-in."
		);
}

export default function RequestSelfCheckInForBooking({
	params: { accommodationId, bookingId }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const { opened, close } = useStaticModalTransition(() => void navigate(".."));

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.bookings.requestSelfCheckIn(
			accommodationId,
			bookingId
		)
	);

	return (
		<Modal
			opened={opened}
			onClose={close}
			title={t(($) => $.bookings.requestSelfCheckIn.title)}
		>
			{t(($) => $.bookings.requestSelfCheckIn.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={close} color="gray">
					{t(($) => $.common.buttons.cancel)}
				</Button>
				<Button
					leftSection={<PaperPlaneTiltIcon weight="bold" />}
					color="violet"
					loading={isPending}
					onClick={() => {
						mutate(undefined, {
							onSuccess: close
						});
					}}
				>
					{t(($) => $.bookings.requestSelfCheckIn.button)}
				</Button>
			</Group>
		</Modal>
	);
}
