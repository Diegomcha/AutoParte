import { StyleSheet } from "@react-pdf/renderer";

export const pageStyles = StyleSheet.create({
	page: { padding: 48, backgroundColor: "#ffffff" },
	pageContent: { width: "100%", height: "100%", gap: 20 },
	header: { gap: 4 },
	title: { fontSize: 32, fontWeight: 600 },
	text: { fontSize: 12 },
	muted: { color: "#4f4f4f", fontSize: 10, fontWeight: 400 },
	gridCols: { flexDirection: "row", gap: 12, width: "100%" },
	gridCol: { flexGrow: 1, flexBasis: 0, flexDirection: "column", gap: 12 },
	secondTitle: { fontSize: 18, fontWeight: 600 },
	subSection: { gap: 12, width: "100%" },
	subSectionHeading: {
		borderLeftWidth: 4,
		borderLeftColor: "#2d9cdb",
		paddingLeft: 8,
		fontSize: 14,
		fontWeight: 300
	},
	calendarImage: {
		width: "170px"
	},
	address: { gap: 4 },
	signature: { gap: 10 },
	signatureImage: {
		borderBottomWidth: 1,
		borderBottomColor: "#000000",
		width: 200,
		height: 100
	}
});
