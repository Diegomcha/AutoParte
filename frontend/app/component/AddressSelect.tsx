import { CheckIcon, Group, Select, Text } from '@mantine/core';
import { useUncontrolled } from '@mantine/hooks';
import { PlusIcon } from '@phosphor-icons/react';
import { useSuspenseQueries, useSuspenseQuery } from '@tanstack/react-query';
import api, { throwErrors } from '~/api';
import { lang } from '~/i18n';
import CountryService from '~/services/CountryService';
import { useTranslation } from 'react-i18next';
import type { AddressDtoResponse } from '~/@types/api';
import type { CountryCode } from '~/services/CountryService';

export default function AddressSelect({
	addresses,
	value,
	defaultValue,
	onChange,
	onNew,
	...props
}: Select.Props & {
	addresses: AddressDtoResponse[];
	value?: string | null;
	defaultValue?: string | null;
	onChange?: (value: string | null) => void;
	onNew?: () => void;
}) {
	const { t } = useTranslation();

	const { data: provincesMap } = useSuspenseQuery({
		queryKey: ['catalogue', 'countries', 'ESP', 'provinces'],
		queryFn: async () =>
			throwErrors(await api.GET('/api/catalogue/countries/ESP/provinces')),
	});

	// Fetch municipalities for the unique province codes
	const municipalityQueries = useSuspenseQueries({
		queries: Array.from(
			new Set(
				addresses
					.filter((address) => address.country === 'ESP')
					.map((address) => address.municipality.slice(0, 2))
			)
		).map((provinceCode) => ({
			queryKey: [
				'catalogue',
				'countries',
				'ESP',
				'provinces',
				provinceCode,
				'municipalities',
				'tuple', // Add a unique identifier to avoid query key collisions with other views
			],
			queryFn: async () => {
				const response = throwErrors(
					await api.GET(
						'/api/catalogue/countries/ESP/provinces/{provinceCode}/municipalities',
						{
							params: {
								path: { provinceCode },
							},
						}
					)
				);
				return [provinceCode, response] as const;
			},
		})),
	});

	// Create a map of province codes to their municipalities for quick lookup
	const municipalitiesMap = Object.fromEntries(
		municipalityQueries.map((query) => query.data)
	);

	// Create the address map
	const addressMap = Object.fromEntries(
		addresses.map((address) => [
			address.id,
			{
				...address,
				municipalityLabel:
					address.country === 'ESP'
						? `${
								municipalitiesMap[address.municipality.slice(0, 2)]?.[
									address.municipality.slice(2)
								] ?? ''
							} · ${provincesMap[address.municipality.slice(0, 2)] ?? ''}`
						: address.municipality,
				countryLabel: CountryService.getName(address.country as CountryCode),
			},
		])
	);

	const selectData = Object.values(addressMap)
		.map((address) => ({
			value: address.id,
			label:
				address.addressLine1 +
				(address.addressLine2 ? ` / ${address.addressLine2}` : ''),
		}))
		.sort((a, b) => a.label.localeCompare(b.label, lang));

	const [_value, handleChange] = useUncontrolled({
		value,
		defaultValue,
		finalValue: undefined,
		onChange,
	});

	return (
		<Select
			{...props}
			value={_value}
			onChange={(values) => {
				if (values === '$new') onNew?.();
				else handleChange(values);
			}}
			data={[
				'$new',
				{
					group: t(($) => $.addressSelect.current),
					items: selectData.filter((addr) => addr.value === _value),
				},
				{
					group: t(($) => $.addressSelect.other),
					items: selectData.filter((addr) => addr.value !== _value),
				},
			]}
			renderOption={({ checked, option }) => {
				// Display a special option for creating a new address
				if (option.value === '$new') {
					return (
						<Group gap="xs" h={'100%'} w={'100%'} wrap="nowrap">
							<PlusIcon width="1em" />
							<Text size="sm">{t(($) => $.addressSelect.new)}</Text>
						</Group>
					);
				}

				// Display the address details for existing addresses
				// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- We know that the address exists in the map.
				const address = addressMap[option.value]!;

				return (
					<Group gap="xs" wrap="nowrap">
						<div>
							<Text fw={'bold'} size="sm">
								{address.addressLine1}
								{address.addressLine2 && ` / ${address.addressLine2}`}
							</Text>
							<Text size="sm">
								{address.postalCode} · {address.municipalityLabel} ·{' '}
								{CountryService.getFlag(address.country as CountryCode)}{' '}
								{address.countryLabel}
							</Text>
						</div>
						{checked && <CheckIcon width="1em" />}
					</Group>
				);
			}}
			comboboxProps={{
				position: 'bottom-start',
				width: 'auto',
			}}
		/>
	);
}
