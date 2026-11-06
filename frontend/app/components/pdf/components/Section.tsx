import { Text, View } from "@react-pdf/renderer";

import { sectionStyles } from "./Section.styles";

export default function Section({
	title,
	children
}: Readonly<{
	title: string;
	children: React.ReactNode;
}>) {
	return (
		<View style={sectionStyles.section}>
			<View style={sectionStyles.title}>
				<Text style={sectionStyles.heading}>{title}</Text>
				<View style={sectionStyles.divider} />
			</View>
			{children}
		</View>
	);
}
