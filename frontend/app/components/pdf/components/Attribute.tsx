import { Text, View } from "@react-pdf/renderer";

import { attributeStyles } from "./Attribute.styles";

export default function Attribute({
	title,
	value,
	style = attributeStyles.attribute
}: Readonly<{
	title: string;
	value: string;
	style?: typeof attributeStyles.attribute;
}>) {
	return (
		<View style={style}>
			<Text style={attributeStyles.title}>{title}</Text>
			<Text style={attributeStyles.value}>{value}</Text>
		</View>
	);
}
