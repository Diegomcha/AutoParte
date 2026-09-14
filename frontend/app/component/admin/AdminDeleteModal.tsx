import { Button, Group, Modal } from "@mantine/core";

import { TrashIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import type { AnyUseMutationOptions } from "@tanstack/react-query";

export interface AdminDeleteModalProps {
	mutation: AnyUseMutationOptions;
	selected: { id: string }[];
	setSelected: (selected: []) => void;
	deleteModalOpen: boolean;
	setDeleteModalOpen: (open: false) => void;
	messages: {
		title: string;
		description: (count: number) => string;
	};
}

export default function AdminDeleteModal({
	mutation,
	selected,
	setSelected,
	deleteModalOpen,
	setDeleteModalOpen,
	messages: { title, description }
}: Readonly<AdminDeleteModalProps>) {
	const { t } = useTranslation();

	const { mutate: deleteSelected, isPending: isDeleting } =
		useMutation(mutation);

	return (
		<Modal
			opened={deleteModalOpen}
			onClose={() => {
				setDeleteModalOpen(false);
			}}
			title={title}
		>
			{description(selected.length)}
			<Group justify="right" mt="md" gap="xs">
				<Button
					disabled={isDeleting}
					onClick={() => {
						setDeleteModalOpen(false);
					}}
					color="gray"
				>
					{t(($) => $.buttons.cancel)}
				</Button>
				<Button
					color="red"
					onClick={() => {
						deleteSelected(
							selected.map((e) => e.id),
							{
								onSuccess: () => {
									setSelected([]);
									setDeleteModalOpen(false);
								}
							}
						);
					}}
					leftSection={<TrashIcon weight="bold" />}
					loading={isDeleting}
				>
					{t(($) => $.buttons.delete)}
				</Button>
			</Group>
		</Modal>
	);
}
