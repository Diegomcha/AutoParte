import { useState } from "react";

import { CheckIcon, Group, Select, Text } from "@mantine/core";
import { useUncontrolled } from "@mantine/hooks";

import { PlusIcon } from "@phosphor-icons/react";
import { useSuspenseQueries } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import { lang } from "~/i18n";
import { _api, _unwrapResponse, queryFactory } from "~/services/Api";
import CountryService from "~/services/CountryService";

import NewAddressForm from "./NewAddressForm";

import type { CountryCode } from "~/services/CountryService";

export default function AddressSelect({
	accommodationId,
	bookingId,
	value,
	defaultValue,
	onChange,
	...props
}: Select.Props & {
	accommodationId: string;
	bookingId: string;
	value?: string | null;
	defaultValue?: string | null;
	onChange?: (value: string | null) => void;
}) {
	const { t } = useTranslation("components", {
		keyPrefix: "addressSelect"
	});

	const { provincesMap, bookingAddresses } = useSuspenseQueries({
		queries: [
			queryFactory.catalogue.countries.spanishProvinces.list(),
			queryFactory.accommodations.bookings.addresses.list(
				accommodationId,
				bookingId
			)
		],
		combine: (result) => ({
			provincesMap: result[0].data,
			bookingAddresses: result[1].data
		})
	});

	const [newAddressesCache, setNewAddressesCache] = useState<string[]>([]);
	const newAddresses = useSuspenseQueries({
		queries: newAddressesCache.map((addressId) =>
			queryFactory.addresses.detail(addressId)
		),
		combine: (result) => result.map((res) => res.data)
	})
		// Remove duplicates that are already in bookingAddresses
		.filter((address) =>
			bookingAddresses.every((bookAddr) => bookAddr.id !== address.id)
		);

	const addresses = bookingAddresses.concat(newAddresses);

	// Fetch municipalities for the unique province codes
	const municipalityQueries = useSuspenseQueries({
		queries: Array.from(
			new Set(
				addresses
					.filter((address) => address.country === "ESP")
					.map((address) => address.municipality.slice(0, 2))
			)
			// eslint-disable-next-line @tanstack/query/prefer-query-options -- Special case for fetching municipalities based on province codes
		).map((provinceCode) => ({
			queryKey: [
				...queryFactory.catalogue.countries.spanishProvinces.municipalities.list(
					provinceCode
				).queryKey,
				{ component: "AddressSelect" }
			],
			queryFn: async () =>
				[
					provinceCode,
					_unwrapResponse(
						await _api.GET(
							"/api/catalogue/countries/ESP/provinces/{provinceCode}/municipalities",
							{
								params: {
									path: { provinceCode }
								}
							}
						)
					)
				] as const
		}))
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
					address.country === "ESP"
						? `${
								municipalitiesMap[address.municipality.slice(0, 2)]?.[
									address.municipality.slice(2)
								] ?? ""
							} · ${provincesMap[address.municipality.slice(0, 2)] ?? ""}`
						: address.municipality,
				countryLabel: CountryService.getName(address.country as CountryCode)
			}
		])
	);

	const selectData = Object.values(addressMap)
		.map((address) => ({
			value: address.id,
			label:
				address.addressLine1 +
				(address.addressLine2 ? ` / ${address.addressLine2}` : "")
		}))
		.sort((a, b) => a.label.localeCompare(b.label, lang));

	const [_value, handleChange] = useUncontrolled({
		value,
		defaultValue,
		finalValue: undefined,
		onChange
	});

	const [openNewAddressForm, setOpenNewAddressForm] = useState(false);

	return (
		<>
			<Select
				{...props}
				value={_value}
				onChange={(value) => {
					if (value === "$new") setOpenNewAddressForm(true);
					else handleChange(value);
				}}
				data={[
					"$new",
					{
						group: t(($) => $.groups.current),
						items: selectData.filter((addr) => addr.value === _value)
					},
					{
						group: t(($) => $.groups.booking, {
							count: bookingAddresses.length
						}),
						items: selectData.filter(
							(addr) =>
								addr.value !== _value &&
								bookingAddresses.some((bookAddr) => bookAddr.id === addr.value)
						)
					},
					{
						group: t(($) => $.groups.other, { count: newAddresses.length }),
						items: selectData.filter(
							(addr) =>
								addr.value !== _value &&
								newAddresses.some((newAddr) => newAddr.id === addr.value)
						)
					}
				]}
				renderOption={({ checked, option }) => {
					// Display a special option for creating a new address
					if (option.value === "$new") {
						return (
							<Group gap="xs" h={"100%"} w={"100%"} wrap="nowrap">
								<PlusIcon width="1em" />
								<Text size="sm">{t(($) => $.newButton)}</Text>
							</Group>
						);
					}

					// Display the address details for existing addresses
					// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- We know that the address exists in the map.
					const address = addressMap[option.value]!;

					return (
						<Group gap="xs" wrap="nowrap">
							<div>
								<Text fw={"bold"} size="sm">
									{address.addressLine1}
									{address.addressLine2 && ` / ${address.addressLine2}`}
								</Text>
								<Text size="sm">
									{address.postalCode} · {address.municipalityLabel} ·{" "}
									{CountryService.getFlag(address.country as CountryCode)}{" "}
									{address.countryLabel}
								</Text>
							</div>
							{checked && <CheckIcon width="1em" />}
						</Group>
					);
				}}
				comboboxProps={{
					position: "bottom-start",
					width: "auto"
				}}
			/>
			<NewAddressForm
				opened={openNewAddressForm}
				onClose={() => {
					setOpenNewAddressForm(false);
				}}
				handleNewAddress={(addressId) => {
					setOpenNewAddressForm(false);
					setNewAddressesCache((prev) => [...prev, addressId]);
					handleChange(addressId);
				}}
			/>
		</>
	);
}
