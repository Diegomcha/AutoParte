import { useNavigate } from "react-router";

import { Button, Group, Modal } from "@mantine/core";

import { CheckCircleIcon } from "@phosphor-icons/react";
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

	if (!booking.canBeConfirmed)
		throw Validators.throwValidationErrorResponse(
			"Booking is not in a state that allows confirmation."
		);
}

export default function ConfirmBooking({
	params: { accommodationId, bookingId }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation("routes", {
		keyPrefix: "bookings.confirm"
	});
	const { t: tCommon } = useTranslation();

	const { opened, close } = useStaticModalTransition(() => void navigate(".."));

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.bookings.confirm(accommodationId, bookingId)
	);

	return (
		<Modal opened={opened} onClose={close} title={t(($) => $.title)}>
			{t(($) => $.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={close} color="gray">
					{tCommon(($) => $.buttons.cancel)}
				</Button>
				<Button
					leftSection={<CheckCircleIcon weight="bold" />}
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
