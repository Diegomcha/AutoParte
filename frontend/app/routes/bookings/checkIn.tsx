import { useNavigate } from "react-router";

import { Button, Group, Modal } from "@mantine/core";

import { SuitcaseIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { Route } from "./+types/confirm";

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const booking = await queryClient.query(
		queryFactory.accommodations.bookings.detail(accommodationId, bookingId)
	);

	if (booking.status !== "CHECK_IN_READY")
		throw Validators.throwValidationErrorResponse(
			"Booking is not in a state that allows check-in."
		);
}

export default function CheckInBooking({
	params: { accommodationId, bookingId }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const { opened, close } = useStaticModalTransition(() => void navigate(".."));

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.bookings.checkIn(accommodationId, bookingId)
	);

	return (
		<Modal
			opened={opened}
			onClose={close}
			title={t(($) => $.bookings.checkIn.title)}
		>
			{t(($) => $.bookings.checkIn.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={close} color="gray">
					{t(($) => $.common.buttons.cancel)}
				</Button>
				<Button
					color={t(($) => $.bookings.checkIn.color)}
					leftSection={<SuitcaseIcon weight="bold" />}
					loading={isPending}
					onClick={() => {
						mutate(undefined, {
							onSuccess: close
						});
					}}
				>
					{t(($) => $.bookings.checkIn.button)}
				</Button>
			</Group>
		</Modal>
	);
}
