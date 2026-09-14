import { forwardRef } from "react";

import { InputBase } from "@mantine/core";
import { useUncontrolled } from "@mantine/hooks";

import { t } from "i18next";
import { parsePhoneNumberFromString } from "libphonenumber-js";
import BasePhoneInput from "react-phone-number-input/input";

import type { ReactNode } from "react";
import type { Value } from "react-phone-number-input";
import type { Props } from "react-phone-number-input/input";

export function isValidPhoneNumber(error?: ReactNode) {
	return (value: Value | undefined) => {
		if (value && !parsePhoneNumberFromString(value)?.isValid())
			return error ?? t(($) => $.phoneInput.errors.invalidPhoneNumber);
	};
}

type PhoneInputProps = Omit<
	Props<React.ComponentProps<"input">>,
	"onChange" | "inputComponent"
> &
	InputBase.Props & {
		onChange?: (value?: Value) => void;
	};

export default forwardRef<HTMLInputElement, PhoneInputProps>(
	function PhoneInput(
		{ onChange, value, defaultValue, ...props }: PhoneInputProps,
		ref
	) {
		const [_value, handleChange] = useUncontrolled({
			value,
			defaultValue,
			finalValue: undefined,
			onChange
		});
		return (
			<BasePhoneInput
				ref={ref}
				value={_value}
				onChange={handleChange}
				inputComponent={InputBase}
				{...props}
			/>
		);
	}
);
