import { Button, Group, Modal } from '@mantine/core';
import { TrashIcon } from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import useStaticModalTransition from '~/hooks/useStaticModalTransition';
import { queryClient, queryFactory } from '~/services/Api';
import Validators from '~/services/Validators';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import type { Route } from './+types/delete';

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	Validators.validateUuids(id);

	await queryClient.query(queryFactory.employees.detail(id));
}

export default function DeleteEmployee({
	params: { id },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const { opened, close } = useStaticModalTransition(
		() => void navigate('/admin/employees')
	);

	const { mutate, isPending } = useMutation(queryFactory.employees.delete(id));

	return (
		<Modal
			opened={opened}
			onClose={close}
			title={t(($) => $.admin.employees.delete.title)}
		>
			{t(($) => $.admin.employees.delete.description)}

			<Group justify="right" mt="md" gap="xs">
				<Button onClick={close} color="gray">
					{t(($) => $.common.buttons.cancel)}
				</Button>
				<Button
					color="red"
					loading={isPending}
					onClick={() => {
						mutate(undefined, {
							onSuccess: close,
						});
					}}
					leftSection={<TrashIcon weight="bold" />}
				>
					{t(($) => $.common.buttons.delete)}
				</Button>
			</Group>
		</Modal>
	);
}
