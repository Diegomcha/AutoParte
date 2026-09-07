import {
	Button,
	Drawer,
	Fieldset,
	Select,
	SimpleGrid,
	Stack,
	TextInput,
} from '@mantine/core';
import { isNotEmpty, useForm } from '@mantine/form';
import { FloppyDiskIcon } from '@phosphor-icons/react';
import { useMutation, useQuery } from '@tanstack/react-query';
import CountrySelect from '~/component/CountrySelect';
import useStaticModalTransition from '~/hooks/useStaticModalTransition';
import { queryClient, queryFactory } from '~/services/Api';
import Validators from '~/services/Validators';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import { useNewAddressHandler } from '.';
import type { Route } from './+types/newAddress';
import type { AddressDtoRequest } from '~/@types/api';
import type { CountryCode } from '~/services/CountryService';

export async function clientLoader({
	params: { accommodationId, bookingId },
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const [countries, spanishProvinces] = await Promise.all([
		queryClient.query(queryFactory.catalogue.countries.list()),
		queryClient.query(queryFactory.catalogue.countries.spanishProvinces.list()),
	]);

	return {
		countries,
		spanishProvinces,
	};
}

export default function CreatePersonAddress({
	loaderData: { countries, spanishProvinces },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const { opened, close } = useStaticModalTransition(() => void navigate('..'));
	const handleNewAddress = useNewAddressHandler();

	const form = useForm<AddressDtoRequest & { province?: string | null }>({
		initialValues: {
			addressLine1: '',
			addressLine2: '',
			municipality: '',
			postalCode: '',
			country: '',
			province: null,
		},
		validate: {
			addressLine1: isNotEmpty(
				t(($) => $.people.newAddress.properties.addressLine1.errors.undefined)
			),
			country: isNotEmpty(
				t(($) => $.people.newAddress.properties.country.errors.undefined)
			),
			province: (value) => {
				if (form.values.country === 'ESP' && !value)
					return t(
						($) => $.people.newAddress.properties.province.errors.undefined
					);
			},
			municipality: isNotEmpty(
				t(($) => $.people.newAddress.properties.municipality.errors.undefined)
			),
			postalCode: isNotEmpty(
				t(($) => $.people.newAddress.properties.postalCode.errors.undefined)
			),
		},
		transformValues: (values) =>
			({
				addressLine1: values.addressLine1,
				// eslint-disable-next-line @typescript-eslint/prefer-nullish-coalescing -- I want to send undefined if the field is empty.
				addressLine2: values.addressLine2 || undefined,
				country: values.country,
				postalCode: values.postalCode,
				municipality:
					values.country === 'ESP'
						? // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
							values.province! + values.municipality
						: values.municipality,
			}) satisfies AddressDtoRequest,
		onValuesChange: (values, prevValues) => {
			if (values.country !== prevValues.country) {
				form.resetField('province');
				form.resetField('municipality');
				form.resetField('postalCode');
			}

			// TODO: This does not work as expected
			if (values.province !== prevValues.province) {
				form.resetField('municipality');
				form.resetField('postalCode');
			}

			// TODO: This does not work as expected
			if (values.municipality !== prevValues.municipality)
				form.resetField('postalCode');
		},
	});

	const {
		data: spanishMunicipalities,
		isLoading: isSpanishMunicipalitiesLoading,
	} = useQuery({
		...queryFactory.catalogue.countries.spanishProvinces.municipalities.list(
			form.values.province ?? 'unexistant-province-code'
		),
		enabled: !!form.values.province,
	});

	const { data: spanishPostalCodes, isLoading: isSpanishPostalCodesLoading } =
		useQuery({
			...queryFactory.catalogue.countries.spanishProvinces.municipalities.postalCodes.list(
				form.values.province ?? 'unexistant-province-code',
				form.values.municipality || 'unexistant-municipality-code'
			),
			enabled: !!form.values.province && !!form.values.municipality,
		});

	const { mutate, isPending } = useMutation(queryFactory.addresses.create());

	return (
		<Drawer
			opened={opened}
			onClose={close}
			title={t(($) => $.people.newAddress.title)}
			size="auto"
		>
			<form
				onSubmit={form.onSubmit((address) => {
					mutate(address, {
						onSuccess: (created) =>
							void queryClient
								.query(queryFactory.addresses.detail(created.id))
								.then((address) => {
									handleNewAddress(address);
									close();
								}),
					});
				})}
				onReset={form.onReset}
			>
				<Stack>
					<Fieldset legend={t(($) => $.people.newAddress.legend)}>
						<SimpleGrid cols={2}>
							<TextInput
								label={t(
									($) => $.people.newAddress.properties.addressLine1.label
								)}
								withAsterisk
								key={form.key('addressLine1')}
								{...form.getInputProps('addressLine1')}
							/>
							<TextInput
								label={t(
									($) => $.people.newAddress.properties.addressLine2.label
								)}
								key={form.key('addressLine2')}
								{...form.getInputProps('addressLine2')}
							/>
							<CountrySelect
								countries={countries as CountryCode[]}
								label={t(($) => $.people.newAddress.properties.country.label)}
								withAsterisk
								key={form.key('country')}
								{...form.getInputProps('country')}
							/>
							<Select
								data={Object.entries(spanishProvinces)
									.map(([provinceCode, provinceName]) => ({
										value: provinceCode,
										label: provinceName,
									}))
									.sort((a, b) => a.label.localeCompare(b.label))}
								label={t(($) => $.people.newAddress.properties.province.label)}
								withAsterisk={form.values.country === 'ESP'}
								disabled={form.values.country !== 'ESP'}
								searchable
								checkIconPosition="right"
								key={form.key('province')}
								{...form.getInputProps('province')}
							/>
							{form.values.country === 'ESP' ? (
								<>
									<Select
										data={
											spanishMunicipalities &&
											Object.entries(spanishMunicipalities)
												.map(([municipalityCode, municipalityName]) => ({
													value: municipalityCode,
													label: municipalityName,
												}))
												.sort((a, b) => a.label.localeCompare(b.label))
										}
										label={t(
											($) => $.people.newAddress.properties.municipality.label
										)}
										withAsterisk
										disabled={!form.values.province}
										loading={isSpanishMunicipalitiesLoading}
										searchable
										checkIconPosition="right"
										key={form.key('municipality')}
										{...form.getInputProps('municipality')}
									/>
									<Select
										data={spanishPostalCodes?.sort((a, b) =>
											a.localeCompare(b)
										)}
										label={t(
											($) => $.people.newAddress.properties.postalCode.label
										)}
										withAsterisk
										disabled={!form.values.municipality}
										loading={isSpanishPostalCodesLoading}
										searchable
										checkIconPosition="right"
										key={form.key('postalCode')}
										{...form.getInputProps('postalCode')}
									/>
								</>
							) : (
								<>
									<TextInput
										label={t(
											($) => $.people.newAddress.properties.municipality.label
										)}
										withAsterisk
										disabled={!form.values.country}
										key={form.key('municipality')}
										{...form.getInputProps('municipality')}
									/>
									<TextInput
										label={t(
											($) => $.people.newAddress.properties.postalCode.label
										)}
										withAsterisk
										disabled={!form.values.country}
										key={form.key('postalCode')}
										{...form.getInputProps('postalCode')}
									/>
								</>
							)}
						</SimpleGrid>
					</Fieldset>
					<Button
						type="submit"
						color="green"
						leftSection={<FloppyDiskIcon weight="bold" size={16} />}
						loading={isPending}
						disabled={!form.isDirty()}
					>
						{t(($) => $.common.buttons.add)}
					</Button>
				</Stack>
			</form>
		</Drawer>
	);
}
