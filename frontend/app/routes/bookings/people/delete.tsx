import { useNavigate } from "react-router";

import { Button, Group, Modal } from "@mantine/core";

import { TrashIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { Route } from "./+types/delete";

export async function clientLoader({
	params: { accommodationId, bookingId, id }
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId, id);

	await queryClient.query(
		queryFactory.accommodations.bookings.people.detail(
			accommodationId,
			bookingId,
			id
		)
	);
}

export default function DeletePerson({
	params: { accommodationId, bookingId, id }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const { opened, close } = useStaticModalTransition(
		() =>
			void navigate(
				`/accommodations/${accommodationId}/bookings/${bookingId}/people`
			)
	);

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.bookings.people.delete(
			accommodationId,
			bookingId,
			id
		)
	);

	return (
		<Modal
			opened={opened}
			onClose={close}
			title={t(($) => $.people.delete.title)}
		>
			{t(($) => $.people.delete.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={close} color="gray">
					{t(($) => $.buttons.cancel)}
				</Button>
				<Button
					color="red"
					loading={isPending}
					leftSection={<TrashIcon weight="bold" />}
					onClick={() => {
						mutate(undefined, {
							onSuccess: close
						});
					}}
				>
					{t(($) => $.buttons.delete)}
				</Button>
			</Group>
		</Modal>
	);
}
