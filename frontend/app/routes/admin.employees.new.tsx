import { Button, Group, Modal, TextInput, useModalsStack } from '@mantine/core';
import { isEmail, isNotEmpty, useForm } from '@mantine/form';
import { UserCirclePlusIcon } from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import EmployeeCredsModal from '~/component/EmployeeCredsModal';
import { useTranslation } from 'react-i18next';
import { useNavigate, useRevalidator } from 'react-router';
import type { EmployeeDtoCreate } from '~/@types/api';

export default function NewEmployee() {
	const navigate = useNavigate();
	const revalidator = useRevalidator();
	const { t } = useTranslation();

	const stack = useModalsStack(['new', 'created']);
	const form = useForm({
		initialValues: {
			name: '',
			surname: '',
			email: '',
		},
		validate: {
			name: isNotEmpty(
				t(($) => $.admin.employees.properties.name.errors.noName)
			),
			surname: isNotEmpty(
				t(($) => $.admin.employees.properties.surname.errors.noSurname)
			),
			email: isEmail(
				t(($) => $.admin.employees.properties.email.errors.invalidEmail)
			),
		},
	});

	const {
		mutate,
		data: created,
		isPending,
	} = useMutation({
		throwOnError: true,
		mutationFn: async (values: EmployeeDtoCreate) => {
			const response = await api.POST('/api/employees', {
				body: values,
			});

			// Handle email conflict error (409)
			if (!response.response.ok && response.response.status === 409) {
				form.setFieldError(
					'email',
					t(($) => $.admin.employees.properties.email.errors.emailInUse)
				);
				return false;
			}

			return throwErrors(response);
		},
		onSuccess: async (success) => {
			if (success) {
				stack.open('created');

				await queryClient.invalidateQueries({ queryKey: ['employees'] });
				await revalidator.revalidate();
			}
		},
	});

	return (
		<>
			<div hidden={revalidator.state === 'idle'}>Revalidating...</div>
			<Modal.Stack>
				<Modal
					{...stack.register('new')}
					opened
					onClose={() => void navigate('/admin/employees')}
					title={t(($) => $.admin.employees.new.title)}
				>
					<form
						onSubmit={form.onSubmit((data) => {
							mutate(data);
						})}
					>
						<Group grow>
							<TextInput
								key={form.key('name')}
								name="name"
								label={t(($) => $.admin.employees.properties.name.label)}
								{...form.getInputProps('name')}
							/>
							<TextInput
								key={form.key('surname')}
								name="surname"
								label={t(($) => $.admin.employees.properties.surname.label)}
								{...form.getInputProps('surname')}
							/>
						</Group>
						<TextInput
							key={form.key('email')}
							name="email"
							label={t(($) => $.admin.employees.properties.email.label)}
							{...form.getInputProps('email')}
						/>
						<Group justify="right" mt="md">
							<Button
								type="submit"
								loading={isPending}
								leftSection={<UserCirclePlusIcon />}
							>
								{t(($) => $.common.buttons.create)}
							</Button>
						</Group>
					</form>
				</Modal>
				{created && (
					<EmployeeCredsModal
						{...stack.register('created')}
						creds={created}
						onClose={() => void navigate('/admin/employees')}
						title={t(($) => $.admin.employees.new.created.title)}
						description={t(($) => $.admin.employees.new.created.description)}
					/>
				)}
			</Modal.Stack>
		</>
	);
}
