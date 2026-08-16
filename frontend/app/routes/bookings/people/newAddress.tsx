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
import api, { queryClient, throwErrors } from '~/api';
import CountrySelect from '~/component/CountrySelect';
import Validators from '~/services/Validators';
import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import type { Route } from './+types/newAddress';
import type { AddressDtoRequest } from '~/@types/api';
import type { CountryCode } from '~/services/CountryService';

export async function clientLoader({
	params: { accommodationId, bookingId },
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	return {
		countries: await queryClient.fetchQuery({
			queryKey: ['catalogue', 'countries'],
			queryFn: async () =>
				throwErrors(await api.GET('/api/catalogue/countries')),
		}),
		spanishProvinces: await queryClient.fetchQuery({
			queryKey: ['catalogue', 'countries', 'ESP', 'provinces'],
			queryFn: async () =>
				throwErrors(await api.GET('/api/catalogue/countries/ESP/provinces')),
		}),
	};
}

export default function CreatePersonAddress({
	loaderData: { countries, spanishProvinces },
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

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
	});

	const {
		data: spanishMunicipalities,
		isLoading: isSpanishMunicipalitiesLoading,
	} = useQuery({
		throwOnError: true,
		queryKey: [
			'catalogue',
			'countries',
			'ESP',
			'provinces',
			form.values.province,
			'municipalities',
		],
		queryFn: async () =>
			form.values.province
				? throwErrors(
						await api.GET(
							'/api/catalogue/countries/ESP/provinces/{provinceCode}/municipalities',
							{
								params: { path: { provinceCode: form.values.province } },
							}
						)
					)
				: {},
	});

	const { data: spanishPostalCodes, isLoading: isSpanishPostalCodesLoading } =
		useQuery({
			throwOnError: true,
			queryKey: [
				'catalogue',
				'countries',
				'ESP',
				'provinces',
				form.values.province,
				'municipalities',
				form.values.municipality,
				'postal-codes',
			],
			queryFn: async () =>
				form.values.province && form.values.municipality
					? throwErrors(
							await api.GET(
								'/api/catalogue/countries/ESP/provinces/{provinceCode}/municipalities/{municipalityCode}/postal-codes',
								{
									params: {
										path: {
											provinceCode: form.values.province,
											municipalityCode: form.values.municipality,
										},
									},
								}
							)
						)
					: [],
		});

	const { mutate, isPending } = useMutation({
		throwOnError: true,
		mutationFn: async (address: AddressDtoRequest) =>
			throwErrors(
				await api.POST('/api/addresses', {
					body: address,
				})
			),
		onSuccess: async (created) => {
			const address = await queryClient.fetchQuery({
				queryKey: ['addresses', created.id],
				queryFn: async () =>
					throwErrors(
						await api.GET('/api/addresses/{id}', {
							params: { path: { id: created.id } },
						})
					),
			});
			await navigate('..', { state: { address } });
		},
	});

	useEffect(() => {
		form.resetField('province');
		form.resetField('municipality');
		form.resetField('postalCode');
	}, [form.values.country]);

	return (
		<Drawer
			opened
			onClose={() => {
				void navigate('..');
			}}
			title={t(($) => $.people.newAddress.title)}
			size="auto"
		>
			<form
				onSubmit={form.onSubmit((address) => {
					mutate(address);
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
								{...form.getInputProps('addressLine1')}
							/>
							<TextInput
								label={t(
									($) => $.people.newAddress.properties.addressLine2.label
								)}
								{...form.getInputProps('addressLine2')}
							/>
							<CountrySelect
								countries={countries as CountryCode[]}
								label={t(($) => $.people.newAddress.properties.country.label)}
								withAsterisk
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
										{...form.getInputProps('municipality')}
									/>
									<TextInput
										label={t(
											($) => $.people.newAddress.properties.postalCode.label
										)}
										withAsterisk
										disabled={!form.values.country}
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
		// <Modal opened onClose={goBack} title={t(($) => $.people.delete.title)}>
		// 	{t(($) => $.people.delete.description)}

		// 	<Group justify="right" mt="md" gap="xs">
		// 		<Button onClick={goBack} color="gray">
		// 			{t(($) => $.common.buttons.cancel)}
		// 		</Button>
		// 		<Button
		// 			color="red"
		// 			loading={isPending}
		// 			onClick={() => {
		// 				mutate();
		// 			}}
		// 		>
		// 			{t(($) => $.common.buttons.delete)}
		// 		</Button>
		// 	</Group>
		// </Modal>
	);
}
