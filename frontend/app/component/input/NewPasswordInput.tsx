import { Group, PasswordInput, Progress, Stack, Tooltip } from "@mantine/core";
import { useUncontrolled } from "@mantine/hooks";

import { t } from "i18next";
import { useTranslation } from "react-i18next";

import PasswordService from "~/services/PasswordService";

import type { ComponentProps, ReactNode } from "react";

interface NewPasswordInputProps extends Omit<
	ComponentProps<typeof PasswordInput>,
	"value" | "defaultValue" | "onChange"
> {
	value?: string;
	defaultValue?: string;
	onChange?: (value: string) => void;
}

const MIN_PASSWORD_LENGTH = 8;

export function isPasswordStrongEnough(error?: ReactNode) {
	return (password?: string) => {
		if (!password)
			return (
				error ??
				t(($) => $.newPasswordInput.validatorErrors.empty, {
					ns: "components"
				})
			);

		if (password.length < MIN_PASSWORD_LENGTH)
			return (
				error ??
				t(($) => $.newPasswordInput.validatorErrors.tooShort, {
					ns: "components",
					minLength: MIN_PASSWORD_LENGTH
				})
			);

		const { score } = PasswordService.check(password);
		if (score < 2)
			return (
				error ??
				t(($) => $.newPasswordInput.validatorErrors.tooUnsecure, {
					ns: "components"
				})
			);
	};
}

export default function NewPasswordInput({
	value,
	defaultValue,
	onChange,
	...props
}: Readonly<NewPasswordInputProps>) {
	const { t } = useTranslation("components", {
		keyPrefix: "newPasswordInput"
	});

	const [_value, handleChange] = useUncontrolled({
		value,
		defaultValue,
		onChange
	});

	const { score, feedback } = PasswordService.check(_value);
	const { color, label } = t(($) => $.scoring[score], { returnObjects: true });

	return (
		<Stack gap="xs">
			<PasswordInput
				{...props}
				value={_value}
				onChange={(event) => {
					handleChange(event.currentTarget.value);
				}}
				description={feedback.warning ?? props.description}
			/>
			<Tooltip multiline label={label}>
				<Group grow gap={5}>
					<Progress size="sm" color={color} value={score >= 1 ? 100 : 0} />
					<Progress size="sm" color={color} value={score >= 2 ? 100 : 0} />
					<Progress size="sm" color={color} value={score >= 3 ? 100 : 0} />
					<Progress size="sm" color={color} value={score >= 4 ? 100 : 0} />
				</Group>
			</Tooltip>
		</Stack>
	);
}
