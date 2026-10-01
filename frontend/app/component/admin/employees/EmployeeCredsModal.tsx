import {
	Alert,
	Fieldset,
	Modal,
	PasswordInput,
	Stack,
	Text,
	TextInput
} from "@mantine/core";

import { AsteriskIcon, AtIcon, WarningIcon } from "@phosphor-icons/react";
import { useTranslation } from "react-i18next";

import type { EmployeeDtoCredentialsResponse } from "~/@types/api";

export default function EmployeeCredsModal({
	creds,
	description,
	...props
}: React.ComponentProps<typeof Modal> & {
	creds: EmployeeDtoCredentialsResponse;
	description: string;
}) {
	const { t } = useTranslation("components", {
		keyPrefix: "employeeCredsModal"
	});

	return (
		<Modal {...props}>
			<Stack>
				<Alert color="yellow" icon={<WarningIcon weight="bold" />}>
					{t(($) => $.warning)}
				</Alert>
				<Text>{description}</Text>
				<Fieldset legend={t(($) => $.title)}>
					<TextInput
						readOnly
						leftSectionPointerEvents="none"
						leftSection={<AtIcon />}
						label={t(($) => $.fields.username)}
						value={creds.email}
						onClick={(event) => {
							event.currentTarget.select();
						}}
					/>
					<PasswordInput
						readOnly
						leftSectionPointerEvents="none"
						leftSection={<AsteriskIcon />}
						label={t(($) => $.fields.password)}
						value={creds.password}
						onClick={(event) => {
							event.currentTarget.select();
						}}
					/>
				</Fieldset>
			</Stack>
		</Modal>
	);
}
