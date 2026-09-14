import { useNavigate } from "react-router";

import { Button, Group, Modal } from "@mantine/core";

import { XIcon } from "@phosphor-icons/react";
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

	if (
		booking.status === "PENDING_CANCELLATION" ||
		booking.status === "CANCELLED"
	)
		throw Validators.throwValidationErrorResponse(
			"Booking is not in a state that allows cancellation."
		);
}

export default function CancelBooking({
	params: { accommodationId, bookingId }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const { opened, close } = useStaticModalTransition(() => void navigate(".."));

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.bookings.cancel(accommodationId, bookingId)
	);

	return (
		<Modal
			opened={opened}
			onClose={close}
			title={t(($) => $.bookings.cancel.title)}
		>
			{t(($) => $.bookings.cancel.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={close} color="gray">
					{t(($) => $.buttons.back)}
				</Button>
				<Button
					color={t(($) => $.bookings.cancel.color)}
					loading={isPending}
					leftSection={<XIcon weight="bold" />}
					onClick={() => {
						mutate(undefined, {
							onSuccess: close
						});
					}}
				>
					{t(($) => $.bookings.cancel.button)}
				</Button>
			</Group>
		</Modal>
	);
}
