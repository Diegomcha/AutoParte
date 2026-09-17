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
	Title
} from "@mantine/core";
import { useForm } from "@mantine/form";

import {
	ArrowUUpLeftIcon,
	FloppyDiskIcon,
	SpinnerIcon
} from "@phosphor-icons/react";
import { useMutation, useSuspenseQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import { queryClient, queryFactory } from "~/services/Api";

export async function clientLoader() {
	await queryClient.query(queryFactory.configuration.get());
}

export default function ConfigPage() {
	const { t } = useTranslation();

	const { data: config } = useSuspenseQuery(queryFactory.configuration.get());

	const form = useForm({
		mode: "uncontrolled",
		initialValues: config,
		validate: {
			//TODO:
		}
	});

	const { mutate, isPending } = useMutation(
		queryFactory.configuration.update()
	);

	const {
		mutate: validateSesCreds,
		isPending: isValidatingSesCreds,
		isError: isValidationError,
		data: isSesCredsValidUpdated
	} = useMutation(queryFactory.configuration.validateSesCreds());

	const isSesCredsValid =
		!isValidationError &&
		(isSesCredsValidUpdated ?? config.sesCredentialsValid);

	let validationStatusText = t(($) =>
		isSesCredsValid
			? $.admin.config.sesValidation.valid
			: $.admin.config.sesValidation.invalid
	);
	if (isValidationError)
		validationStatusText = t(($) => $.admin.config.sesValidation.error);
	if (isValidatingSesCreds)
		validationStatusText = t(($) => $.admin.config.sesValidation.validating);

	return (
		<form
			onSubmit={form.onSubmit((data) => {
				mutate(data, {
					onSuccess: () => {
						form.resetDirty();

						validateSesCreds();
					}
				});
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
						loading={isPending}
						hidden={!form.isDirty()}
					>
						{t(($) => $.buttons.reset)}
					</Button>
					<Button
						type="submit"
						color="green"
						leftSection={<FloppyDiskIcon weight="bold" size={16} />}
						loading={isPending}
						disabled={!form.isDirty()}
					>
						{t(($) => $.buttons.save)}
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
								key={form.key("sesUsername")}
								name="sesUsername"
								label={t(
									($) => $.admin.config.properties.sesCredentials.username
								)}
								{...form.getInputProps("sesUsername")}
							/>
							<TextInput
								key={form.key("sesLandlordCode")}
								name="sesLandlordCode"
								label={t(
									($) =>
										$.admin.config.properties.sesCredentials.sesLandlordCode
								)}
								{...form.getInputProps("sesLandlordCode")}
							/>
						</Group>
						<PasswordInput
							key={form.key("sesPassword")}
							name="sesPassword"
							label={t(
								($) => $.admin.config.properties.sesCredentials.password
							)}
							{...form.getInputProps("sesPassword")}
						/>
						<Divider my="sm" />
						<Center>
							<Chip
								color="green"
								variant="light"
								icon={
									isValidatingSesCreds ? (
										<SpinnerIcon className="animate-spin" />
									) : undefined
								}
								checked={isValidatingSesCreds || isSesCredsValid}
								disabled={form.isDirty() || isValidatingSesCreds}
							>
								{validationStatusText}
							</Chip>
						</Center>
					</Fieldset>
					<Fieldset legend={t(($) => $.admin.config.properties.toggles.legend)}>
						<Stack>
							<Switch
								key={form.key("digitalSignatureEnabled")}
								name="digitalSignatureEnabled"
								label={t(
									($) =>
										$.admin.config.properties.toggles.digitalSignatureEnabled
								)}
								{...form.getInputProps("digitalSignatureEnabled", {
									type: "checkbox"
								})}
							/>
							<Switch
								key={form.key("manualReviewEnabled")}
								name="manualReviewEnabled"
								label={t(
									($) => $.admin.config.properties.toggles.manualReviewEnabled
								)}
								{...form.getInputProps("manualReviewEnabled", {
									type: "checkbox"
								})}
							/>
						</Stack>
					</Fieldset>
				</Group>
			</Center>
		</form>
	);
}
