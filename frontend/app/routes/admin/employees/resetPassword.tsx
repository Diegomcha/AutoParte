import { Button, Group, Modal, Text, useModalsStack } from '@mantine/core';
import { ClockClockwiseIcon } from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import EmployeeCredsModal from '~/component/EmployeeCredsModal';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import type { Route } from './+types/resetPassword';

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	return await queryClient.fetchQuery({
		queryKey: ['employee', id],
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/employees/{id}', {
					params: { path: { id } },
				})
			),
	});
}

export default function ResetPasswordEmployee({
	params: { id },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const stack = useModalsStack(['confirmation', 'credentials']);

	const {
		mutate,
		isPending,
		data: creds,
	} = useMutation({
		throwOnError: true,
		mutationFn: async () =>
			throwErrors(
				await api.POST('/api/employees/{id}/reset-password', {
					params: { path: { id } },
				})
			),
		onSuccess: () => {
			stack.open('credentials');
		},
	});

	return (
		<Modal.Stack>
			<Modal
				{...stack.register('confirmation')}
				opened
				onClose={() => void navigate('/admin/employees')}
				title={t(($) => $.admin.employees.resetPassword.title)}
			>
				<Text>{t(($) => $.admin.employees.resetPassword.description)}</Text>
				<Group justify="right" mt="md">
					<Button
						color="red"
						loading={isPending}
						leftSection={<ClockClockwiseIcon />}
						onClick={() => {
							mutate();
						}}
					>
						{t(($) => $.admin.employees.resetPassword.button)}
					</Button>
				</Group>
			</Modal>
			{creds && (
				<EmployeeCredsModal
					{...stack.register('credentials')}
					creds={creds}
					onClose={() => void navigate('/admin/employees')}
					title={t(($) => $.admin.employees.resetPassword.done.title)}
					description={t(
						($) => $.admin.employees.resetPassword.done.description
					)}
				/>
			)}
		</Modal.Stack>
	);
}
