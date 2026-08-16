import { Button, Group, Modal, Stack, TextInput } from '@mantine/core';
import { isNotEmpty, useForm } from '@mantine/form';
import { PlusIcon } from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import BooleanInputWithUndefined from '~/component/BooleanInputWithUndefined';
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
			internetConnection: 'undefined',
		},
		validate: {
			name: isNotEmpty(
				t(($) => $.admin.accommodations.properties.name.errors.noName)
			),
			sesCode: isNotEmpty(
				t(($) => $.admin.accommodations.properties.sesCode.errors.noSesCode)
			),
		},
		transformValues: (values) => ({
			...values,
			internetConnection:
				values.internetConnection === 'undefined'
					? undefined
					: values.internetConnection === 'true',
		}),
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
							withAsterisk
							{...form.getInputProps('name')}
						/>
						<TextInput
							key={form.key('sesCode')}
							name="sesCode"
							label={t(($) => $.admin.accommodations.properties.sesCode.label)}
							withAsterisk
							{...form.getInputProps('sesCode')}
						/>
					</Group>
					<BooleanInputWithUndefined
						key={form.key('internetConnection')}
						name="internetConnection"
						label={t(
							($) => $.admin.accommodations.properties.internetConnection.label
						)}
						withAsterisk
						{...form.getInputProps('internetConnection')}
					/>
				</Stack>
				<Group justify="right" mt="md">
					<Button type="submit" loading={isPending} leftSection={<PlusIcon />}>
						{t(($) => $.common.buttons.create)}
					</Button>
				</Group>
			</form>
		</Modal>
	);
}
