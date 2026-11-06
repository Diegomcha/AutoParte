import { StyleSheet } from "@react-pdf/renderer";

export const tableStyles = StyleSheet.create({
	table: { flexDirection: "column", gap: 10, padding: 10, width: "100%" },
	row: { flexDirection: "row", gap: 10, width: "100%" },
	header: {
		width: "23.5%",
		fontSize: 13,
		color: "#4f4f4f",
		textAlign: "center"
	},
	cell: { width: "23.5%", fontSize: 12, textAlign: "center" }
});
