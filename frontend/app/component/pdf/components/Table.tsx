import { Text, View } from "@react-pdf/renderer";

import { tableStyles } from "./Table.styles";

export function TableHeader({ children }: Readonly<{ children: string }>) {
	return <Text style={tableStyles.header}>{children}</Text>;
}

export function TableCell({
	children = "Value"
}: Readonly<{ children?: string }>) {
	return <Text style={tableStyles.cell}>{children}</Text>;
}

export function Table({ children }: Readonly<{ children: React.ReactNode }>) {
	return <View style={tableStyles.table}>{children}</View>;
}

export function TableRow({
	children
}: Readonly<{ children: React.ReactNode }>) {
	return <View style={tableStyles.row}>{children}</View>;
}
