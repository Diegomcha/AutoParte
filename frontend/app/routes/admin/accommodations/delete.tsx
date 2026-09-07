import { useNavigate } from "react-router";

import { Button, Group, Modal } from "@mantine/core";

import { TrashIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { Route } from "./+types/delete";

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	Validators.validateUuids(id);

	await queryClient.query(queryFactory.accommodations.detail(id));
}

export default function DeleteAccommodation({
	params: { id }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const { opened, close } = useStaticModalTransition(
		() => void navigate("/admin/accommodations")
	);

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.delete(id)
	);

	return (
		<Modal
			opened={opened}
			onClose={close}
			title={t(($) => $.admin.accommodations.delete.title)}
		>
			{t(($) => $.admin.accommodations.delete.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={close} color="gray">
					{t(($) => $.common.buttons.cancel)}
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
					{t(($) => $.common.buttons.delete)}
				</Button>
			</Group>
		</Modal>
	);
}
