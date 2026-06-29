import {
	Button,
	Group,
	Modal,
	MultiSelect,
	Stack,
	Switch,
	TextInput,
} from '@mantine/core';
import { isNotEmpty, useForm } from '@mantine/form';
import { FloppyDiskIcon } from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import { useTranslation } from 'react-i18next';
import { useNavigate, useRevalidator } from 'react-router';
import type { Route } from './+types/admin.accommodations.$id_.edit';
import type { AccommodationDtoRequest } from '~/@types/api';

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	return await queryClient.fetchQuery({
		queryKey: ['accommodation', id],
		queryFn: async () => {
			// Get accommodation
			const accommodation = throwErrors(
				await api.GET('/api/accommodations/{id}', {
					params: { path: { id } },
				})
			);

			// Get available employees
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			const availableEmployees = throwErrors(
				await api.GET('/api/employees', {
					params: { query: { size: 0 } },
				})
			).content!;

			return {
				accommodation,
				availableEmployees,
			};
		},
	});
}

export default function EditAccommodation({
	loaderData: { accommodation, availableEmployees },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const revalidator = useRevalidator();
	const { t } = useTranslation();

	const form = useForm({
		initialValues: {
			name: accommodation.name,
			sesCode: accommodation.sesCode,
			employees: accommodation.employees,
			internetConnection: accommodation.internetConnection,
		},
		validate: {
			name: isNotEmpty(
				t(($) => $.admin.accommodations.properties.name.errors.noName)
			),
			sesCode: isNotEmpty(
				t(($) => $.admin.accommodations.properties.sesCode.errors.noSesCode)
			),
		},
	});

	const { mutate, isPending } = useMutation({
		throwOnError: true,
		mutationFn: async (
			values: AccommodationDtoRequest & { employees: string[] }
		) => {
			const newEmployees = values.employees.filter(
				(employeeId) => !accommodation.employees.includes(employeeId)
			);
			const removedEmployees = accommodation.employees.filter(
				(employeeId) => !values.employees.includes(employeeId)
			);

			// Requests

			const res = await api.PUT(`/api/accommodations/{id}`, {
				body: values,
				params: { path: { id: accommodation.id } },
			});

			await Promise.all([
				...newEmployees.map(async (employeeId) =>
					throwErrors(
						await api.POST(
							'/api/accommodations/{accommodationId}/employees/{employeeId}',
							{
								params: {
									path: { accommodationId: accommodation.id, employeeId },
								},
							}
						)
					)
				),
				...removedEmployees.map(async (employeeId) =>
					throwErrors(
						await api.DELETE(
							'/api/accommodations/{accommodationId}/employees/{employeeId}',
							{
								params: {
									path: { accommodationId: accommodation.id, employeeId },
								},
							}
						)
					)
				),
			]);

			// Handle email conflict error (409)
			if (!res.response.ok && res.response.status === 409) {
				form.setFieldError(
					'sesCode',
					t(
						($) => $.admin.accommodations.properties.sesCode.errors.sesCodeInUse
					)
				);
				return false;
			}

			throwErrors(res);
			return true;
		},
		onSuccess: async (success) => {
			if (success) {
				await queryClient.invalidateQueries({ queryKey: ['accommodations'] });
				await revalidator.revalidate();

				await navigate('/admin/accommodations');
			}
		},
	});

	return (
		<>
			<div hidden={revalidator.state === 'idle'}>Revalidating...</div>
			<Modal
				opened
				onClose={() => void navigate('/admin/accommodations')}
				title={t(($) => $.admin.accommodations.edit.title)}
			>
				<form
					onSubmit={form.onSubmit((data) => {
						mutate(data);
					})}
				>
					<Stack gap="xs">
						<Group grow>
							<TextInput
								key={form.key('name')}
								name="name"
								label={t(($) => $.admin.accommodations.properties.name.label)}
								{...form.getInputProps('name')}
							/>
							<TextInput
								key={form.key('sesCode')}
								name="sesCode"
								label={t(
									($) => $.admin.accommodations.properties.sesCode.label
								)}
								{...form.getInputProps('sesCode')}
							/>
						</Group>
						<Switch
							key={form.key('internetConnection')}
							name="internetConnection"
							label={t(
								($) =>
									$.admin.accommodations.properties.internetConnection.label
							)}
							{...form.getInputProps('internetConnection', {
								type: 'checkbox',
							})}
						/>
						<MultiSelect
							key={form.key('employees')}
							label={t(
								($) => $.admin.accommodations.properties.employees.label
							)}
							data={availableEmployees.map((employee) => ({
								value: employee.id,
								label: `${employee.name} ${employee.surname}`,
							}))}
							nothingFoundMessage={t(
								($) => $.admin.employees.properties.accommodations.none
							)}
							{...form.getInputProps('employees')}
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
		</>
	);
}
