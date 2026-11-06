import JSONViewer from "~/components/JSONViewer";

import { useBookingExportContext } from "./layout";

export default function BookingExportJSON() {
	const { booking } = useBookingExportContext();

	return <JSONViewer value={booking} />;
}
