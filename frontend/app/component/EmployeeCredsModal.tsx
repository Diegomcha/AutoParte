import {
	Alert,
	Fieldset,
	Modal,
	PasswordInput,
	Stack,
	Text,
	TextInput,
} from '@mantine/core';
import { AsteriskIcon, AtIcon, WarningIcon } from '@phosphor-icons/react';
import { useTranslation } from 'react-i18next';
import type { EmployeeDtoCredentialsResponse } from '~/@types/api';

export default function EmployeeCredsModal({
	creds,
	description,
	...props
}: React.ComponentProps<typeof Modal> & {
	creds: EmployeeDtoCredentialsResponse;
	description: string;
}) {
	const { t } = useTranslation();

	return (
		<Modal {...props}>
			<Stack>
				<Alert color="yellow" icon={<WarningIcon weight="bold" />}>
					{t(($) => $.admin.employees.credentials.warning)}
				</Alert>
				<Text>{description}</Text>
				<Fieldset legend={t(($) => $.admin.employees.credentials.title)}>
					<TextInput
						readOnly
						leftSectionPointerEvents="none"
						leftSection={<AtIcon />}
						label={t(($) => $.admin.employees.credentials.fields.username)}
						value={creds.email}
						onClick={(event) => {
							event.currentTarget.select();
						}}
					/>
					<PasswordInput
						readOnly
						leftSectionPointerEvents="none"
						leftSection={<AsteriskIcon />}
						label={t(($) => $.admin.employees.credentials.fields.password)}
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
