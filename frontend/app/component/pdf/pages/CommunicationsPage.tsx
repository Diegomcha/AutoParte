import { Page, Text, View } from "@react-pdf/renderer";
import { getFixedT, t as tCommon } from "i18next";

import TimeService from "~/services/TimeService";

import Footer from "../components/Footer";
import Section from "../components/Section";
import { Table, TableCell, TableHeader, TableRow } from "../components/Table";
import { pageStyles as styles } from "./styles";

import type { BookingSummaryPageProps } from "./BookingSummaryPage";

const t = getFixedT(null, "components", "pdf.booking");
const tCommunications = getFixedT(null, "entities", "booking.communications");

export default function CommunicationsPage({
	booking,
	totalPages
}: Readonly<Omit<BookingSummaryPageProps, "getCalendarImage">>) {
	return (
		<Page size="A4" style={styles.page}>
			<View style={styles.pageContent}>
				<Section title={t(($) => $.sections.communications.title)}>
					{booking.communications.length === 0 ? (
						<Text style={styles.text}>
							{t(($) => $.sections.communications.empty)}
						</Text>
					) : (
						<Table>
							<TableRow>
								<TableHeader>
									{tCommunications(($) => $.type.label)}
								</TableHeader>
								<TableHeader>
									{tCommunications(($) => $.status.label)}
								</TableHeader>
								<TableHeader>
									{tCommunications(($) => $.sentTimestamp.label)}
								</TableHeader>
								<TableHeader>
									{tCommunications(($) => $.error.label)}
								</TableHeader>
							</TableRow>
							{booking.communications.map((communication, index) => (
								<TableRow key={`${communication.type}-${String(index)}`}>
									<TableCell>
										{tCommunications(
											($) => $.type.options[communication.type].label
										)}
									</TableCell>
									<TableCell>
										{tCommunications(
											($) => $.status.states[communication.status].label
										)}
									</TableCell>
									<TableCell>
										{communication.sentTimestamp
											? TimeService(communication.sentTimestamp).format(
													tCommunications(($) => $.sentTimestamp.format)
												)
											: tCommon(($) => $.undefined)}
									</TableCell>
									<TableCell>
										{communication.error ?? tCommon(($) => $.undefined)}
									</TableCell>
								</TableRow>
							))}
						</Table>
					)}
				</Section>

				<Section title={t(($) => $.sections.people.title)}>
					<Text style={styles.text}>{t(($) => $.sections.people.text)}</Text>
				</Section>

				<Footer id={booking.id} currentPage={2} totalPages={totalPages} />
			</View>
		</Page>
	);
}
