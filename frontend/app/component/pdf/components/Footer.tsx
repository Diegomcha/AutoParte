import { Text, View } from "@react-pdf/renderer";
import { getFixedT } from "i18next";

import { footerStyles } from "./Footer.styles";

const t = getFixedT(null, "components", "pdf.booking.footer");

export default function Footer({
	currentPage,
	totalPages,
	id
}: Readonly<{
	currentPage: number;
	totalPages: number;
	id: string;
}>) {
	return (
		<View style={footerStyles.footer}>
			<Text>{id}</Text>
			<Text style={footerStyles.page}>
				{t(($) => $.pagination, { currentPage, totalPages })}
			</Text>
		</View>
	);
}
