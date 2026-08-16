import { InputBase } from '@mantine/core';
import { useUncontrolled } from '@mantine/hooks';
import { t } from 'i18next';
import { parsePhoneNumberFromString } from 'libphonenumber-js';
import { forwardRef } from 'react';
import BasePhoneInput from 'react-phone-number-input/input';
import type { ReactNode } from 'react';
import type { Value } from 'react-phone-number-input';

export function isValidPhoneNumber(error?: ReactNode) {
	return (value: Value | undefined) => {
		if (value && !parsePhoneNumberFromString(value)?.isValid())
			return error ?? t(($) => $.phoneInput.errors.invalidPhoneNumber);
	};
}

export default forwardRef(function PhoneInput(
	{
		onChange,
		value,
		defaultValue,
		...props
	}: InputBase.Props & {
		value?: Value;
		onChange?: (value?: Value) => void;
		defaultValue?: Value;
		readOnly?: boolean;
	},
	ref
) {
	const [_value, handleChange] = useUncontrolled({
		value,
		defaultValue,
		finalValue: undefined,
		onChange,
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
});
