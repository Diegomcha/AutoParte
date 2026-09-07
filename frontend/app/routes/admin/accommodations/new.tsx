import { Button, Group, Modal, Stack, TextInput } from '@mantine/core';
import { isNotEmpty, useForm } from '@mantine/form';
import { PlusIcon } from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import BooleanInputWithUndefined from '~/component/BooleanInputWithUndefined';
import useStaticModalTransition from '~/hooks/useStaticModalTransition';
import { queryFactory } from '~/services/Api';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';

export default function NewAccommodation() {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const { opened, close } = useStaticModalTransition(
		() => void navigate('/admin/accommodations')
	);

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

	const { mutate: create, isPending: isCreating } = useMutation(
		queryFactory.accommodations.create()
	);

	return (
		<Modal
			opened={opened}
			onClose={close}
			title={t(($) => $.admin.accommodations.new.title)}
		>
			<form
				onSubmit={form.onSubmit((data) => {
					create(data, {
						onSuccess: ([ok, errorCode]) => {
							if (ok) close();
							else if (errorCode === 'NAME_IN_USE') {
								form.setFieldError(
									'name',
									t(
										($) =>
											$.admin.accommodations.properties.name.errors.nameInUse
									)
								);
							} else {
								form.setFieldError(
									'sesCode',
									t(
										($) =>
											$.admin.accommodations.properties.sesCode.errors
												.sesCodeInUse
									)
								);
							}
						},
					});
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
					<Button type="submit" loading={isCreating} leftSection={<PlusIcon />}>
						{t(($) => $.common.buttons.create)}
					</Button>
				</Group>
			</form>
		</Modal>
	);
}
