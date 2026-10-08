import { Image, Page, Text, View } from "@react-pdf/renderer";
import { getFixedT, t as tCommon } from "i18next";

import TimeService from "~/services/TimeService";

import Attribute from "../components/Attribute";
import Footer from "../components/Footer";
import Section from "../components/Section";
import { pageStyles as styles } from "./styles";

import type { ExportedBookingData } from "~/routes/bookings/export/layout";

const t = getFixedT(null, "components", "pdf.booking");
const tBooking = getFixedT(null, "entities", "booking");
const tEntity = getFixedT(null, "entities", "common");

export interface BookingSummaryPageProps {
	booking: ExportedBookingData;
	totalPages: number;
	getCalendarImage: () => Promise<string>;
}
export default function BookingSummaryPage({
	booking,
	totalPages,
	getCalendarImage
}: Readonly<BookingSummaryPageProps>) {
	return (
		<Page size="A4" style={styles.page}>
			<View style={styles.pageContent}>
				<View style={styles.header}>
					<Text style={styles.title}>{t(($) => $.header.title)}</Text>
					<Text style={styles.muted}>
						{t(($) => $.header.subtitle, {
							date: TimeService().format(t(($) => $.header.subtitleDateFormat))
						})}
					</Text>
				</View>

				<Section title={t(($) => $.sections.accommodationAndDates.title)}>
					<View style={styles.gridCols}>
						<View style={styles.gridCol}>
							<Image src={getCalendarImage} style={styles.calendarImage} />
						</View>
						<View style={styles.gridCol}>
							<Attribute
								title={tBooking(($) => $.details.accommodation.label)}
								value={booking.accommodationName}
							/>
							<Attribute
								title={tBooking(($) => $.details.startTime.label)}
								value={TimeService(booking.startTime).format(
									tBooking(($) => $.details.startTime.format)
								)}
							/>
							<Attribute
								title={tBooking(($) => $.details.endTime.label)}
								value={TimeService(booking.endTime).format(
									tBooking(($) => $.details.endTime.format)
								)}
							/>
							<Attribute
								title={tBooking(($) => $.details.duration.label)}
								value={tBooking(($) => $.details.duration.value, {
									count: TimeService(booking.endTime).diff(
										booking.startTime,
										"days"
									)
								})}
							/>
						</View>
					</View>
				</Section>

				<Section title={t(($) => $.sections.otherDetails.title)}>
					<View style={styles.gridCols}>
						<View style={styles.gridCol}>
							<Attribute
								title={tBooking(($) => $.details.status.label)}
								value={tBooking(
									($) => $.details.status.states[booking.status].label
								)}
							/>
							<Attribute
								title={tEntity(($) => $.createdAt.label)}
								value={TimeService(booking.createdAt).format(
									tEntity(($) => $.createdAt.format)
								)}
							/>
							<Attribute
								title={tEntity(($) => $.updatedAt.label)}
								value={TimeService(booking.updatedAt).format(
									tEntity(($) => $.updatedAt.format)
								)}
							/>
						</View>
						<View style={styles.gridCol}>
							<Attribute
								title={tBooking(($) => $.details.numberOfPeople.label)}
								value={tBooking(($) => $.details.numberOfPeople.value, {
									count: booking.numberOfPeople
								})}
							/>
							<Attribute
								title={tBooking(($) => $.details.numberOfRooms.label)}
								value={tBooking(($) => $.details.numberOfRooms.value, {
									count: booking.numberOfRooms ?? 0
								})}
							/>
							<Attribute
								title={tBooking(($) => $.details.internetConnection.label)}
								value={tBooking(
									($) =>
										$.details.internetConnection.states[
											String(booking.internetConnection) as
												"true" | "false" | "null"
										]
								)}
							/>
						</View>
					</View>
				</Section>

				<Section title={t(($) => $.sections.payment.title)}>
					<View style={styles.gridCols}>
						<View style={styles.gridCol}>
							<Attribute
								title={tBooking(($) => $.payment.type.label)}
								value={
									booking.payment?.type
										? tBooking(
												// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- We know that booking.payment is not null here
												($) => $.payment.type.options[booking.payment!.type]
											)
										: tCommon(($) => $.undefined)
								}
							/>
							<Attribute
								title={tBooking(($) => $.payment.mean.label)}
								value={booking.payment?.mean ?? tCommon(($) => $.undefined)}
							/>
							<Attribute
								title={tBooking(($) => $.payment.expiryDate.label)}
								value={
									booking.payment?.expiryDate
										? TimeService(booking.payment.expiryDate).format(
												tBooking(($) => $.payment.expiryDate.format)
											)
										: tCommon(($) => $.undefined)
								}
							/>
						</View>
						<View style={styles.gridCol}>
							<Attribute
								title={tBooking(($) => $.payment.date.label)}
								value={
									booking.payment?.date
										? TimeService(booking.payment.date).format(
												tBooking(($) => $.payment.date.format)
											)
										: tCommon(($) => $.undefined)
								}
							/>
							<Attribute
								title={tBooking(($) => $.payment.holder.label)}
								value={booking.payment?.holder ?? tCommon(($) => $.undefined)}
							/>
						</View>
					</View>
				</Section>

				<Footer id={booking.id} currentPage={1} totalPages={totalPages} />
			</View>
		</Page>
	);
}
