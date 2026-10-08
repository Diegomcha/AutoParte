import { Image, Page, Text, View } from "@react-pdf/renderer";
import { getFixedT, t as tCommon } from "i18next";

import TimeService from "~/services/TimeService";

import Attribute from "../components/Attribute";
import Footer from "../components/Footer";
import { pageStyles as styles } from "./styles";

import type { SignatureDtoResponse } from "~/@types/api";
import type { ExportedBookingData } from "~/routes/bookings/export/layout";
import type { BookingSummaryPageProps } from "./BookingSummaryPage";

function SubSection({
	title,
	children
}: Readonly<{ title: string; children: React.ReactNode }>) {
	return (
		<View style={styles.subSection}>
			<Text style={styles.subSectionHeading}>{title}</Text>
			{children}
		</View>
	);
}

const t = getFixedT(null, "components", "pdf.booking.person");
const tPerson = getFixedT(null, "entities", "person");

export interface PersonPageProps extends Omit<
	BookingSummaryPageProps,
	"getCalendarImage"
> {
	number: number;
	person: ExportedBookingData["people"][number];
	getSignatureImage: (
		signature: SignatureDtoResponse["paths"]
	) => Promise<string>;
}

export default function PersonPage({
	number,
	booking,
	person,
	getSignatureImage,
	totalPages
}: Readonly<PersonPageProps>) {
	return (
		<Page size="A4" style={styles.page}>
			<View style={styles.pageContent}>
				<Text style={styles.secondTitle}>
					{t(($) => $.title, {
						number,
						name: person.personalInfo.name,
						firstSurname: person.personalInfo.firstSurname,
						secondSurname: person.personalInfo.secondSurname
					})}
				</Text>

				<SubSection title={tPerson(($) => $.personalInfo.title)}>
					<View style={styles.gridCols}>
						<View style={styles.gridCol}>
							<Attribute
								title={tPerson(($) => $.personalInfo.name.label)}
								value={person.personalInfo.name}
							/>
							<Attribute
								title={tPerson(($) => $.personalInfo.nationality.label)}
								value={
									person.personalInfo.nationality ?? tCommon(($) => $.undefined)
								}
							/>
							<Attribute
								title={tPerson(($) => $.personalInfo.gender.label)}
								value={
									person.personalInfo.gender
										? tPerson(
												($) =>
													$.personalInfo.gender.options[
														// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- We know that gender is not null here because of the conditional check above
														person.personalInfo.gender!
													]
											)
										: tCommon(($) => $.undefined)
								}
							/>
						</View>
						<View style={styles.gridCol}>
							<Attribute
								title={tPerson(($) => $.personalInfo.firstSurname.label)}
								value={person.personalInfo.firstSurname}
							/>
							<Attribute
								title={tPerson(($) => $.personalInfo.secondSurname.label)}
								value={
									person.personalInfo.birthDate
										? TimeService(person.personalInfo.birthDate).format(
												tPerson(($) => $.personalInfo.birthDate.format)
											)
										: tCommon(($) => $.undefined)
								}
							/>
							<Attribute
								title={tPerson(($) => $.personalInfo.relationship.label)}
								value={
									person.relationship
										? tPerson(
												($) =>
													$.personalInfo.relationship.options[
														// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- We know that relationship is not null here because of the conditional check above
														person.relationship!
													]
											)
										: tCommon(($) => $.undefined)
								}
							/>
						</View>
						<View style={styles.gridCol}>
							<Attribute
								title={tPerson(($) => $.personalInfo.secondSurname.label)}
								value={
									person.personalInfo.secondSurname ??
									tCommon(($) => $.undefined)
								}
							/>
							<Attribute
								title={tPerson(($) => $.personalInfo.address.label)}
								value={
									person.address
										? tPerson(($) => $.personalInfo.address.value, {
												...person.address
											})
										: tCommon(($) => $.undefined)
								}
							/>
						</View>
					</View>
				</SubSection>

				<SubSection title={tPerson(($) => $.contactInfo.title)}>
					<View style={styles.gridCols}>
						<View style={styles.gridCol}>
							<Attribute
								title={tPerson(($) => $.contactInfo.email.label)}
								value={person.contactInfo.email ?? tCommon(($) => $.undefined)}
							/>
						</View>
						<View style={styles.gridCol}>
							<Attribute
								title={tPerson(($) => $.contactInfo.phoneNumber1.label)}
								value={
									person.contactInfo.phoneNumber1 ?? tCommon(($) => $.undefined)
								}
							/>
						</View>
						<View style={styles.gridCol}>
							<Attribute
								title={tPerson(($) => $.contactInfo.phoneNumber2.label)}
								value={
									person.contactInfo.phoneNumber2 ?? tCommon(($) => $.undefined)
								}
							/>
						</View>
					</View>
				</SubSection>

				<SubSection title={tPerson(($) => $.document.title)}>
					<View style={styles.gridCols}>
						<View style={styles.gridCol}>
							<Attribute
								title={tPerson(($) => $.document.type.label)}
								value={
									person.document?.type
										? tPerson(
												($) =>
													$.document.type.options[
														// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- We know that document is not null here because of the conditional check above
														person.document!.type
													]
											)
										: tCommon(($) => $.undefined)
								}
							/>
						</View>
						<View style={styles.gridCol}>
							<Attribute
								title={tPerson(($) => $.document.number.label)}
								value={person.document?.number ?? tCommon(($) => $.undefined)}
							/>
						</View>
						<View style={styles.gridCol}>
							<Attribute
								title={tPerson(($) => $.document.supportNumber.label)}
								value={
									person.document?.supportNumber ?? tCommon(($) => $.undefined)
								}
							/>
						</View>
					</View>
				</SubSection>

				<SubSection title={t(($) => $.sections.signature.title)}>
					{person.mustSign ? (
						<View style={styles.signature}>
							{person.signature?.paths ? (
								<Image
									style={styles.signatureImage}
									// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- We know that signature is not null here because of the conditional check above
									src={() => getSignatureImage(person.signature!.paths)}
								/>
							) : (
								<View style={styles.signatureImage} />
							)}
							<Text style={styles.muted}>
								{t(($) => $.sections.signature.context, {
									count: person.signature ? 1 : 0,
									signedAt: TimeService(person.signature?.signedAt).format(
										tPerson(($) => $.signature.format)
									),
									ip: person.signature?.ipAddress
								})}
							</Text>
						</View>
					) : (
						<Text style={styles.muted}>
							{t(($) => $.sections.signature.notRequired)}
						</Text>
					)}
				</SubSection>

				<Footer
					id={booking.id}
					currentPage={2 + number}
					totalPages={totalPages}
				/>
			</View>
		</Page>
	);
}
