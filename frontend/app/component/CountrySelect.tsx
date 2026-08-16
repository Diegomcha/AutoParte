import { CheckIcon, Group, Select, Text } from '@mantine/core';
import CountryService from '~/services/CountryService';
import type { CountryCode } from '~/services/CountryService';

export default function CountrySelect({
	countries,
	...props
}: Select.Props & { countries: CountryCode[] }) {
	const opts = Object.fromEntries(
		countries.map(
			(country) =>
				[
					country,
					{
						value: country,
						label: CountryService.getName(country),
					},
				] as const
		)
	);

	return (
		<Select
			{...props}
			data={Object.values(opts).sort((a, b) => a.label.localeCompare(b.label))}
			renderOption={({ checked, option }) => (
				<Group gap="xs" wrap="nowrap">
					{checked ? (
						<CheckIcon width="1em" />
					) : (
						CountryService.getFlag(option.value as CountryCode)
					)}
					<Text size="sm">
						{
							// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- We know that the option exists in the opts map.
							opts[option.value as CountryCode]!.label
						}
					</Text>
				</Group>
			)}
			comboboxProps={{
				position: 'bottom-start',
				width: 'auto',
			}}
			checkIconPosition="right"
			searchable
			limit={50}
		/>
	);
}
