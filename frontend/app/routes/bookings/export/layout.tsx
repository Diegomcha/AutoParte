import { Outlet, useNavigate, useOutletContext } from "react-router";

import { Modal } from "@mantine/core";

import { useTranslation } from "react-i18next";

import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import CountryService from "~/services/CountryService";
import PhoneService from "~/services/PhoneService";

import type { CountryCode } from "~/services/CountryService";
import type { Route } from "./+types/layout";

export type ExportedBookingData = Awaited<
	ReturnType<typeof clientLoader>
>["booking"];

export interface BookingExportContextType {
	booking: ExportedBookingData;
}

/**
 * Hook to access the booking export context.
 * @returns The booking export context containing the booking data.
 */
export function useBookingExportContext() {
	return useOutletContext<BookingExportContextType>();
}

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	const [booking, communications, people, addresses, provincesMap] =
		await Promise.all([
			queryClient.query(
				queryFactory.accommodations.bookings.detail(accommodationId, bookingId)
			),
			queryClient.query(
				queryFactory.accommodations.bookings.communications.list(
					accommodationId,
					bookingId
				)
			),
			queryClient.query(
				queryFactory.accommodations.bookings.people.list(
					accommodationId,
					bookingId
				)
			),
			queryClient.query(
				queryFactory.accommodations.bookings.addresses.list(
					accommodationId,
					bookingId
				)
			),
			queryClient.query(
				queryFactory.catalogue.countries.spanishProvinces.list()
			)
		]);

	// Fetch municipalities for the unique province codes
	const municipalitiesMap = Object.fromEntries(
		await Promise.all(
			Array.from(
				new Set(
					addresses
						.filter((address) => address.country === "ESP")
						.map((address) => address.municipality.slice(0, 2))
				)
			).map((provinceCode) =>
				queryClient.query(
					queryFactory.catalogue.countries.spanishProvinces.municipalities.listWProvinceCode(
						provinceCode
					)
				)
			)
		)
	);

	// Fetch signature for each person
	const signatures = await Promise.all(
		people.map((person) =>
			queryClient.query(
				queryFactory.accommodations.bookings.people.getSignature(
					accommodationId,
					bookingId,
					person.id
				)
			)
		)
	);

	// * Build complete booking data

	const labelledAddresses = Object.fromEntries(
		addresses.map((address) => [
			address.id,
			{
				...address,
				municipality:
					address.country === "ESP"
						? `${
								municipalitiesMap[address.municipality.slice(0, 2)]?.[
									address.municipality.slice(2)
								] ?? ""
							} (${provincesMap[address.municipality.slice(0, 2)] ?? ""})`
						: address.municipality,
				country: CountryService.getName(address.country as CountryCode)
			}
		])
	);

	const peopleWithSignatures = people.map((person, index) => ({
		...person,
		personalInfo: {
			...person.personalInfo,
			nationality: person.personalInfo.nationality
				? CountryService.getName(person.personalInfo.nationality as CountryCode)
				: undefined
		},
		contactInfo: {
			...person.contactInfo,
			phoneNumber1: person.contactInfo.phoneNumber1
				? PhoneService.format(person.contactInfo.phoneNumber1)
				: undefined,
			phoneNumber2: person.contactInfo.phoneNumber2
				? PhoneService.format(person.contactInfo.phoneNumber2)
				: undefined
		},
		address: person.address ? labelledAddresses[person.address] : undefined,
		signature: signatures[index]
	}));

	return {
		booking: {
			...booking,
			communications,
			people: peopleWithSignatures
		}
	};
}

export default function BookingExportLayout({
	loaderData: { booking }
}: Route.ComponentProps) {
	const navigate = useNavigate();

	const { t: tCommon } = useTranslation();

	const { opened, close } = useStaticModalTransition(() => {
		void navigate("..");
	});

	const activeExport = location.pathname.split("/").at(-1) as "pdf" | "json";

	return (
		<Modal
			title={tCommon(($) => $.export[activeExport].title)}
			opened={opened}
			onClose={close}
			size="90%"
			styles={{
				body: {
					padding: 0
				}
			}}
		>
			<Outlet context={{ booking } satisfies BookingExportContextType} />
		</Modal>
	);
}
