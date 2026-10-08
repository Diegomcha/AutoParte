import { useRef } from "react";

import { PDFViewer } from "@react-pdf/renderer";

import BookingPDF from "~/component/pdf/BookingPDF";
import { CalendarRenderer } from "~/component/pdf/renderers/CalendarRenderer";
import { SignatureRenderer } from "~/component/pdf/renderers/SignatureRenderer";

import { useBookingExportContext } from "./layout";

import type { CalendarRendererRef } from "~/component/pdf/renderers/CalendarRenderer";
import type { SignatureExporterRef } from "~/component/pdf/renderers/SignatureRenderer";

export default function BookingExportPDF() {
	const signatureRenderer = useRef<SignatureExporterRef>(null);
	const calendarRenderer = useRef<CalendarRendererRef>(null);

	const { booking } = useBookingExportContext();

	return (
		<>
			<PDFViewer height="100%" width="100%" style={{ minHeight: "80vh" }}>
				<BookingPDF
					booking={booking}
					getCalendarImage={() => {
						if (!calendarRenderer.current)
							throw new Error("Calendar renderer is not available");

						return calendarRenderer.current.getImage();
					}}
					getSignatureImage={(signature) => {
						if (!signatureRenderer.current)
							throw new Error("Signature renderer is not available");

						return signatureRenderer.current.getImage(signature);
					}}
				/>
			</PDFViewer>
			<SignatureRenderer ref={signatureRenderer} />
			<CalendarRenderer booking={booking} ref={calendarRenderer} />
		</>
	);
}
