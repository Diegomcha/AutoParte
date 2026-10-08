import { Document } from "@react-pdf/renderer";
import { getFixedT, t as tCommon } from "i18next";

import { lang } from "~/i18n";

import BookingSummaryPage from "./pages/BookingSummaryPage";
import CommunicationsPage from "./pages/CommunicationsPage";
import PersonPage from "./pages/PersonPage";

import type { SignatureDtoResponse } from "~/@types/api";
import type { ExportedBookingData } from "~/routes/bookings/export/layout";

export interface BookingPDFProps {
	booking: ExportedBookingData;
	getCalendarImage: () => Promise<string>;
	getSignatureImage: (
		signature: SignatureDtoResponse["paths"]
	) => Promise<string>;
}

const t = getFixedT(null, "components", "pdf.booking");

export default function BookingPDF({
	booking,
	getCalendarImage,
	getSignatureImage
}: Readonly<BookingPDFProps>) {
	const totalPages = 2 + booking.people.length;

	return (
		<Document
			title={t(($) => $.meta.title, { id: booking.id })}
			author={tCommon(($) => $.meta.name)}
			creator={tCommon(($) => $.meta.name)}
			language={lang}
		>
			<BookingSummaryPage
				booking={booking}
				getCalendarImage={getCalendarImage}
				totalPages={totalPages}
			/>
			<CommunicationsPage booking={booking} totalPages={totalPages} />
			{booking.people.map((person, index) => (
				<PersonPage
					key={person.id}
					number={index + 1}
					person={person}
					booking={booking}
					getSignatureImage={getSignatureImage}
					totalPages={totalPages}
				/>
			))}
		</Document>
	);
}
