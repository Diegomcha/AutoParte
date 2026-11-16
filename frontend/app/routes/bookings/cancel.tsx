import { useNavigate } from "react-router";

import { Button, Group, Modal } from "@mantine/core";

import { XIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import NotificationsService from "~/services/NotificationsService";
import Validators from "~/services/Validators";

import type { Route } from "./+types/cancel";

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const booking = await queryClient.query(
		queryFactory.accommodations.bookings.detail(accommodationId, bookingId)
	);

	if (!booking.canBeCancelled)
		throw Validators.throwValidationErrorResponse(
			"Booking is not in a state that allows cancellation."
		);
}

export default function CancelBooking({
	params: { accommodationId, bookingId }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation("routes", {
		keyPrefix: "bookings.cancel"
	});
	const { t: tCommon } = useTranslation();

	const { opened, close } = useStaticModalTransition(() => void navigate(".."));

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.bookings.cancel(accommodationId, bookingId)
	);

	return (
		<Modal opened={opened} onClose={close} title={t(($) => $.title)}>
			{t(($) => $.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={close} color="gray">
					{tCommon(($) => $.buttons.back)}
				</Button>
				<Button
					leftSection={<XIcon weight="bold" />}
					color={t(($) => $.color)}
					loading={isPending}
					onClick={() => {
						mutate(undefined, {
							onSuccess: () => {
								NotificationsService.success(t(($) => $.doneFeedback));
								close();
							}
						});
					}}
				>
					{t(($) => $.button)}
				</Button>
			</Group>
		</Modal>
	);
}
