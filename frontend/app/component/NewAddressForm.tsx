import {
	Button,
	Drawer,
	Fieldset,
	Select,
	SimpleGrid,
	Stack,
	TextInput
} from "@mantine/core";
import { isNotEmpty, useForm } from "@mantine/form";

import { FloppyDiskIcon } from "@phosphor-icons/react";
import {
	useMutation,
	useQueries,
	useSuspenseQueries
} from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import CountrySelect from "~/component/CountrySelect";
import { queryFactory } from "~/services/Api";

import type { AddressDtoRequest } from "~/@types/api";
import type { CountryCode } from "~/services/CountryService";

export interface NewAddressFormProps extends React.ComponentProps<
	typeof Drawer
> {
	handleNewAddress?: (addressId: string) => void;
}

export default function NewAddressForm({
	handleNewAddress,
	...props
}: Readonly<NewAddressFormProps>) {
	const { t } = useTranslation();

	const { countries, spanishProvinces } = useSuspenseQueries({
		queries: [
			queryFactory.catalogue.countries.list(),
			queryFactory.catalogue.countries.spanishProvinces.list()
		],
		combine: (result) => {
			return {
				countries: result[0].data,
				spanishProvinces: result[1].data
			};
		}
	});

	const form = useForm<AddressDtoRequest & { province?: string | null }>({
		initialValues: {
			addressLine1: "",
			addressLine2: "",
			municipality: "",
			postalCode: "",
			country: "",
			province: null
		},
		validate: {
			addressLine1: isNotEmpty(
				t(($) => $.people.newAddress.properties.addressLine1.errors.undefined)
			),
			country: isNotEmpty(
				t(($) => $.people.newAddress.properties.country.errors.undefined)
			),
			province: (value) => {
				if (form.values.country === "ESP" && !value)
					return t(
						($) => $.people.newAddress.properties.province.errors.undefined
					);
			},
			municipality: isNotEmpty(
				t(($) => $.people.newAddress.properties.municipality.errors.undefined)
			),
			postalCode: isNotEmpty(
				t(($) => $.people.newAddress.properties.postalCode.errors.undefined)
			)
		},
		transformValues: (values) =>
			({
				addressLine1: values.addressLine1,
				// eslint-disable-next-line @typescript-eslint/prefer-nullish-coalescing -- I want to send undefined if the field is empty.
				addressLine2: values.addressLine2 || undefined,
				country: values.country,
				postalCode: values.postalCode,
				municipality:
					values.country === "ESP"
						? // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
							values.province! + values.municipality
						: values.municipality
			}) satisfies AddressDtoRequest,
		onValuesChange: (values, prevValues) => {
			if (values.country !== prevValues.country) {
				form.resetField("province");
				form.resetField("municipality");
				form.resetField("postalCode");
			}

			// TODO: This does not work as expected
			if (values.province !== prevValues.province) {
				form.resetField("municipality");
				form.resetField("postalCode");
			}

			// TODO: This does not work as expected
			if (values.municipality !== prevValues.municipality)
				form.resetField("postalCode");
		}
	});

	const [
		{ data: spanishMunicipalities, isLoading: isSpanishMunicipalitiesLoading },
		{ data: spanishPostalCodes, isLoading: isSpanishPostalCodesLoading }
	] = useQueries({
		queries: [
			{
				...queryFactory.catalogue.countries.spanishProvinces.municipalities.list(
					form.values.province ?? "unexistant-province-code"
				),
				enabled: !!form.values.province
			},
			{
				...queryFactory.catalogue.countries.spanishProvinces.municipalities.postalCodes.list(
					form.values.province ?? "unexistant-province-code",
					form.values.municipality || "unexistant-municipality-code"
				),
				enabled: !!form.values.province && !!form.values.municipality
			}
		]
	});

	const { mutate, isPending } = useMutation(queryFactory.addresses.create());

	return (
		<Drawer title={t(($) => $.people.newAddress.title)} size="auto" {...props}>
			<form
				onSubmit={(e) => {
					// Prevent the form submission from bubbling up to the parent form (if any) and triggering its submission.
					e.stopPropagation();

					form.onSubmit((address) => {
						mutate(address, {
							onSuccess: (created) => {
								handleNewAddress?.(created.id);
								form.reset();
							}
						});
					})(e);
				}}
				onReset={form.onReset}
			>
				<Stack>
					<Fieldset legend={t(($) => $.people.newAddress.legend)}>
						<SimpleGrid cols={2}>
							<TextInput
								type="text"
								autoComplete="address-line1"
								label={t(
									($) => $.people.newAddress.properties.addressLine1.label
								)}
								withAsterisk
								key={form.key("addressLine1")}
								{...form.getInputProps("addressLine1")}
							/>
							<TextInput
								type="text"
								autoComplete="address-line2"
								label={t(
									($) => $.people.newAddress.properties.addressLine2.label
								)}
								key={form.key("addressLine2")}
								{...form.getInputProps("addressLine2")}
							/>
							<CountrySelect
								countries={countries as CountryCode[]}
								label={t(($) => $.people.newAddress.properties.country.label)}
								withAsterisk
								key={form.key("country")}
								{...form.getInputProps("country")}
							/>
							<Select
								autoComplete="address-level1"
								data={Object.entries(spanishProvinces)
									.map(([provinceCode, provinceName]) => ({
										value: provinceCode,
										label: provinceName
									}))
									.sort((a, b) => a.label.localeCompare(b.label))}
								label={t(($) => $.people.newAddress.properties.province.label)}
								withAsterisk={form.values.country === "ESP"}
								disabled={form.values.country !== "ESP"}
								searchable
								checkIconPosition="right"
								key={form.key("province")}
								{...form.getInputProps("province")}
							/>
							{form.values.country === "ESP" ? (
								<>
									<Select
										autoComplete="address-level2"
										data={
											spanishMunicipalities &&
											Object.entries(spanishMunicipalities)
												.map(([municipalityCode, municipalityName]) => ({
													value: municipalityCode,
													label: municipalityName
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
										key={form.key("municipality")}
										{...form.getInputProps("municipality")}
									/>
									<Select
										autoComplete="postal-code"
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
										key={form.key("postalCode")}
										{...form.getInputProps("postalCode")}
									/>
								</>
							) : (
								<>
									<TextInput
										type="text"
										autoComplete="address-level2"
										label={t(
											($) => $.people.newAddress.properties.municipality.label
										)}
										withAsterisk
										disabled={!form.values.country}
										key={form.key("municipality")}
										{...form.getInputProps("municipality")}
									/>
									<TextInput
										type="text"
										autoComplete="postal-code"
										label={t(
											($) => $.people.newAddress.properties.postalCode.label
										)}
										withAsterisk
										disabled={!form.values.country}
										key={form.key("postalCode")}
										{...form.getInputProps("postalCode")}
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
						{t(($) => $.buttons.add)}
					</Button>
				</Stack>
			</form>
		</Drawer>
	);
}
