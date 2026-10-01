import {
	Button,
	Chip,
	Divider,
	Fieldset,
	Group,
	NumberInput,
	PasswordInput,
	SimpleGrid,
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
	const { t } = useTranslation("routes", {
		keyPrefix: "admin.config"
	});
	const { t: tConfig } = useTranslation("entities", {
		keyPrefix: "config"
	});
	const { t: tCommon } = useTranslation();

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
		isSesCredsValid ? $.sesValidation.valid : $.sesValidation.invalid
	);
	if (isValidationError) validationStatusText = t(($) => $.sesValidation.error);
	if (isValidatingSesCreds)
		validationStatusText = t(($) => $.sesValidation.validating);

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
				<Title order={2}>{t(($) => $.title)}</Title>
				<Group>
					<Button
						type="reset"
						color="gray"
						leftSection={<ArrowUUpLeftIcon weight="bold" size={16} />}
						loading={isPending}
						hidden={!form.isDirty()}
					>
						{tCommon(($) => $.buttons.reset)}
					</Button>
					<Button
						type="submit"
						color="green"
						leftSection={<FloppyDiskIcon weight="bold" size={16} />}
						loading={isPending}
						disabled={!form.isDirty()}
					>
						{tCommon(($) => $.buttons.save)}
					</Button>
				</Group>
			</Group>
			<Divider my="sm" />
			<SimpleGrid cols={3}>
				<Fieldset legend={tConfig(($) => $.ses.title)}>
					<Stack>
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
						<Group grow>
							<TextInput
								key={form.key("sesUsername")}
								name="sesUsername"
								label={tConfig(($) => $.ses.username.label)}
								{...form.getInputProps("sesUsername")}
							/>
							<TextInput
								key={form.key("sesLandlordCode")}
								name="sesLandlordCode"
								label={tConfig(($) => $.ses.landlordCode.label)}
								{...form.getInputProps("sesLandlordCode")}
							/>
						</Group>
						<PasswordInput
							key={form.key("sesPassword")}
							name="sesPassword"
							label={tConfig(($) => $.ses.password.label)}
							{...form.getInputProps("sesPassword")}
						/>
					</Stack>
				</Fieldset>
				<Fieldset legend={tConfig(($) => $.functionality.title)}>
					<Stack>
						<Switch
							key={form.key("digitalSignatureEnabled")}
							name="digitalSignatureEnabled"
							label={tConfig(
								($) => $.functionality.digitalSignatureEnabled.label
							)}
							description={tConfig(
								($) => $.functionality.digitalSignatureEnabled.description
							)}
							{...form.getInputProps("digitalSignatureEnabled", {
								type: "checkbox"
							})}
						/>
						<Switch
							key={form.key("manualReviewEnabled")}
							name="manualReviewEnabled"
							label={tConfig(($) => $.functionality.manualReviewEnabled.label)}
							description={tConfig(
								($) => $.functionality.manualReviewEnabled.description
							)}
							{...form.getInputProps("manualReviewEnabled", {
								type: "checkbox"
							})}
						/>
					</Stack>
				</Fieldset>
				<Fieldset legend={tConfig(($) => $.retention.title)}>
					<Stack>
						<NumberInput
							key={form.key("logsRetentionDays")}
							label={tConfig(($) => $.retention.logsRetentionDays.label)}
							description={tConfig(
								($) => $.retention.logsRetentionDays.description
							)}
							{...form.getInputProps("logsRetentionDays")}
						/>
						<NumberInput
							key={form.key("bookingsRetentionDays")}
							label={tConfig(($) => $.retention.bookingsRetentionDays.label)}
							description={tConfig(
								($) => $.retention.bookingsRetentionDays.description
							)}
							{...form.getInputProps("bookingsRetentionDays")}
						/>
					</Stack>
				</Fieldset>
			</SimpleGrid>
		</form>
	);
}
