import { Button, Group, Modal, Stack, Switch, TextInput } from '@mantine/core';
import { isNotEmpty, useForm } from '@mantine/form';
import { UserCirclePlusIcon } from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import { useTranslation } from 'react-i18next';
import { useNavigate, useRevalidator } from 'react-router';
import type { AccommodationDtoRequest, ProblemDetail } from '~/@types/api';

export default function NewAccommodation() {
	const navigate = useNavigate();
	const revalidator = useRevalidator();
	const { t } = useTranslation();

	const form = useForm({
		initialValues: {
			name: '',
			sesCode: '',
			internetConnection: false,
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
		mutationFn: async (values: AccommodationDtoRequest) => {
			const response = await api.POST('/api/accommodations', {
				body: values,
			});

			// Handle sesCode conflict error (409)
			if (!response.response.ok && response.response.status === 409) {
				const problem = response.error as ProblemDetail;

				// TODO: Mejorar!
				if (problem.detail?.includes('name'))
					form.setFieldError(
						'name',
						t(($) => $.admin.accommodations.properties.name.errors.nameInUse)
					);
				else
					form.setFieldError(
						'sesCode',
						t(
							($) =>
								$.admin.accommodations.properties.sesCode.errors.sesCodeInUse
						)
					);
				return false;
			}

			return throwErrors(response);
		},
		onSuccess: async (success) => {
			if (success) {
				await queryClient.invalidateQueries({ queryKey: ['accommodations'] });
				await revalidator.revalidate();

				await navigate(`/admin/accommodations/${success.id}`);
			}
		},
	});

	return (
		<Modal
			opened
			onClose={() => void navigate('/admin/accommodations')}
			title={t(($) => $.admin.accommodations.new.title)}
		>
			<form
				onSubmit={form.onSubmit((data) => {
					mutate(data);
				})}
			>
				<Stack>
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
							label={t(($) => $.admin.accommodations.properties.sesCode.label)}
							{...form.getInputProps('sesCode')}
						/>
					</Group>
					<Switch
						key={form.key('internetConnection')}
						name="internetConnection"
						label={t(
							($) => $.admin.accommodations.properties.internetConnection.label
						)}
						{...form.getInputProps('internetConnection', { type: 'checkbox' })}
					/>
				</Stack>
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
	);
}
