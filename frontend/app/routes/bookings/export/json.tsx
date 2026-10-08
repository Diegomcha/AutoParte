import JSONViewer from "~/component/JSONViewer";

import { useBookingExportContext } from "./layout";

export default function BookingExportJSON() {
	const { booking } = useBookingExportContext();

	return <JSONViewer value={booking} />;
}
