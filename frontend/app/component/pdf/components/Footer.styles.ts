import { StyleSheet } from "@react-pdf/renderer";

export const footerStyles = StyleSheet.create({
	footer: {
		width: "100%",
		flexGrow: 1,
		flexDirection: "row",
		paddingTop: 24,
		fontSize: 10,
		justifyContent: "space-between",
		color: "#4f4f4f",
		alignItems: "flex-end"
	},
	page: { textAlign: "right" }
});
