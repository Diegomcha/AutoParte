import {
	Button,
	Chip,
	Group,
	Modal,
	MultiSelect,
	Stack,
	TextInput,
	useModalsStack,
} from '@mantine/core';
import { isEmail, isNotEmpty, useForm } from '@mantine/form';
import { CheckCircleIcon, FloppyDiskIcon } from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import Validators from '~/services/Validators';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import type { Route } from './+types/edit';
import type { EmployeeDtoPatch } from '~/@types/api';

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	Validators.validateUuids(id);

	return await queryClient.fetchQuery({
		queryKey: ['employee', id],
		queryFn: async () => {
			// Get employee
			const employee = throwErrors(
				await api.GET('/api/employees/{id}', {
					params: { path: { id } },
				})
			);

			// Get available accommodations
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			const availableAccommodations = throwErrors(
				await api.GET('/api/accommodations', {
					params: { query: { size: 0 } },
				})
			).content!;

			return {
				employee,
				availableAccommodations,
			};
		},
	});
}

export default function EditEmployee({
	loaderData: { employee, availableAccommodations },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const stack = useModalsStack(['editor', 'edited']);
	const form = useForm({
		initialValues: {
			enabled: employee.enabled,
			name: employee.name,
			surname: employee.surname,
			email: employee.email,
			accommodations: employee.accommodations.map(
				(accommodation) => accommodation.id
			),
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

	const { mutate, isPending } = useMutation({
		throwOnError: true,
		mutationFn: async (
			values: EmployeeDtoPatch & { accommodations: string[] }
		) => {
			const newAccommodations = values.accommodations.filter(
				(accommodationId) =>
					!employee.accommodations.some(
						(accommodation) => accommodation.id === accommodationId
					)
			);
			const removedAccommodations = employee.accommodations.filter(
				(accommodation) => !values.accommodations.includes(accommodation.id)
			);

			// Requests

			const res = await api.PATCH(`/api/employees/{id}`, {
				body: values,
				params: { path: { id: employee.id } },
			});

			await Promise.all([
				...newAccommodations.map(async (accommodationId) =>
					throwErrors(
						await api.POST(
							'/api/accommodations/{accommodationId}/employees/{employeeId}',
							{
								params: { path: { accommodationId, employeeId: employee.id } },
							}
						)
					)
				),
				...removedAccommodations.map(async (accommodation) =>
					throwErrors(
						await api.DELETE(
							'/api/accommodations/{accommodationId}/employees/{employeeId}',
							{
								params: {
									path: {
										accommodationId: accommodation.id,
										employeeId: employee.id,
									},
								},
							}
						)
					)
				),
			]);

			// Handle email conflict error (409)
			if (!res.response.ok && res.response.status === 409) {
				form.setFieldError(
					'email',
					t(($) => $.admin.employees.properties.email.errors.emailInUse)
				);
				return false;
			}

			throwErrors(res);

			return true;
		},
		onSuccess: async (success) => {
			if (success) {
				await queryClient.invalidateQueries({ queryKey: ['employees'] });

				await navigate('/admin/employees');
			}
		},
	});

	return (
		<Modal
			{...stack.register('editor')}
			opened
			onClose={() => void navigate('/admin/employees')}
			title={t(($) => $.admin.employees.edit.title)}
		>
			<form
				onSubmit={form.onSubmit((data) => {
					mutate(data);
				})}
			>
				<Stack gap="xs">
					<Chip
						key={form.key('enabled')}
						icon={<CheckCircleIcon />}
						color="green"
						variant="light"
						{...form.getInputProps('enabled', { type: 'checkbox' })}
					>
						{form.getValues().enabled
							? t(($) => $.admin.employees.properties.enabled.states.enabled)
							: t(($) => $.admin.employees.properties.enabled.states.disabled)}
					</Chip>
					<div>
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
					</div>
					<MultiSelect
						key={form.key('accommodations')}
						label={t(($) => $.admin.employees.properties.accommodations.label)}
						data={availableAccommodations.map((accommodation) => ({
							value: accommodation.id,
							label: accommodation.name,
						}))}
						nothingFoundMessage={t(
							($) => $.admin.employees.properties.accommodations.none
						)}
						{...form.getInputProps('accommodations')}
					/>
				</Stack>
				<Group justify="right" mt="md">
					<Button
						type="submit"
						loading={isPending}
						leftSection={<FloppyDiskIcon />}
					>
						{t(($) => $.common.buttons.save)}
					</Button>
				</Group>
			</form>
		</Modal>
	);
}
