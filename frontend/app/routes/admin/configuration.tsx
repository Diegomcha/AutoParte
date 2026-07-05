import {
	Button,
	Center,
	Chip,
	Divider,
	Fieldset,
	Group,
	PasswordInput,
	Stack,
	Switch,
	TextInput,
	Title,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import {
	ArrowUUpLeftIcon,
	FloppyDiskIcon,
	SpinnerIcon,
} from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import { useTranslation } from 'react-i18next';
import type { Route } from './+types/configuration';
import type { ConfigDtoRequest } from '~/@types/api';

export async function clientLoader() {
	return await queryClient.fetchQuery({
		queryKey: ['configuration'],
		queryFn: async () => throwErrors(await api.GET('/api/config')),
	});
}

export default function ConfigPage({
	loaderData: config,
}: Route.ComponentProps) {
	const { t } = useTranslation();

	const form = useForm({
		initialValues: config,
		validate: {
			//TODO:
		},
	});

	const {
		mutate: validate,
		isPending: isValidating,
		isError: isValidationError,
	} = useMutation({
		mutationFn: async () => {
			const res = await api.POST('/api/config/validate-ses');

			// Handle unauthorized error (401)
			if (res.response.status === 401) return false;

			throwErrors(res);

			return true;
		},
		onSuccess: (success) => {
			form.setFieldValue('sesCredentialsValid', success);
		},
	});

	const { mutate, isPending } = useMutation({
		throwOnError: true,
		mutationFn: async (data: ConfigDtoRequest) =>
			throwErrors(
				await api.PUT('/api/config', {
					body: data,
				})
			),
		onSuccess: async () => {
			await queryClient.invalidateQueries({ queryKey: ['configuration'] });
			validate();
		},
	});

	return (
		<form
			onSubmit={form.onSubmit((data) => {
				mutate(data);
			})}
			onReset={form.onReset}
		>
			<Group justify="space-between">
				<Title order={2}>{t(($) => $.admin.config.title)}</Title>
				<Group>
					<Button
						type="reset"
						color="gray"
						leftSection={<ArrowUUpLeftIcon weight="bold" size={16} />}
						disabled={isPending}
					>
						{t(($) => $.common.buttons.cancel)}
					</Button>
					<Button
						type="submit"
						loading={isPending}
						color="green"
						leftSection={<FloppyDiskIcon weight="bold" size={16} />}
					>
						{t(($) => $.common.buttons.save)}
					</Button>
				</Group>
			</Group>
			<Divider my="sm" />
			<Center>
				<Group align="top">
					<Fieldset
						legend={t(($) => $.admin.config.properties.sesCredentials.legend)}
					>
						<Group grow>
							<TextInput
								key={form.key('sesUsername')}
								name="sesUsername"
								label={t(
									($) => $.admin.config.properties.sesCredentials.username
								)}
								{...form.getInputProps('sesUsername')}
							/>
							<TextInput
								key={form.key('sesLandlordCode')}
								name="sesLandlordCode"
								label={t(
									($) =>
										$.admin.config.properties.sesCredentials.sesLandlordCode
								)}
								{...form.getInputProps('sesLandlordCode')}
							/>
						</Group>
						<PasswordInput
							key={form.key('sesPassword')}
							name="sesPassword"
							label={t(
								($) => $.admin.config.properties.sesCredentials.password
							)}
							{...form.getInputProps('sesPassword')}
						/>
						<Divider my="sm" />
						<Center>
							<Chip
								key={form.key('sesCredentialsValid')}
								name="sesCredentialsValid"
								color="green"
								variant="light"
								icon={
									isValidating ? (
										<SpinnerIcon className="animate-spin" />
									) : undefined
								}
								checked={form.getValues().sesCredentialsValid}
							>
								{isValidating
									? t(($) => $.admin.config.sesValidation.validating)
									: isValidationError
										? t(($) => $.admin.config.sesValidation.error)
										: form.getValues().sesCredentialsValid
											? t(($) => $.admin.config.sesValidation.valid)
											: t(($) => $.admin.config.sesValidation.invalid)}
							</Chip>
						</Center>
					</Fieldset>
					<Fieldset legend={t(($) => $.admin.config.properties.toggles.legend)}>
						<Stack>
							<Switch
								key={form.key('digitalSignatureEnabled')}
								name="digitalSignatureEnabled"
								label={t(
									($) =>
										$.admin.config.properties.toggles.digitalSignatureEnabled
								)}
								{...form.getInputProps('digitalSignatureEnabled', {
									type: 'checkbox',
								})}
							/>
							<Switch
								key={form.key('manualReviewEnabled')}
								name="manualReviewEnabled"
								label={t(
									($) => $.admin.config.properties.toggles.manualReviewEnabled
								)}
								{...form.getInputProps('manualReviewEnabled', {
									type: 'checkbox',
								})}
							/>
						</Stack>
					</Fieldset>
				</Group>
			</Center>
		</form>
	);
}
