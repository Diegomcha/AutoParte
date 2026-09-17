import { Center, Paper, Stack, Text, Title } from "@mantine/core";

import { CheckCircleIcon } from "@phosphor-icons/react";
import { useTranslation } from "react-i18next";

export default function CheckInFinished() {
	const { t } = useTranslation("routes", {
		keyPrefix: "checkIn.checkInCompleted"
	});

	return (
		<Center mih="100vh" bg="dark">
			<Paper
				withBorder
				shadow="sm"
				p="xl"
				h={{ base: "100vh", sm: "auto" }}
				w={{ base: "100vw", sm: "auto" }}
			>
				<Stack component="main" align="center" justify="center" gap="lg">
					<Center c="blue">
						<CheckCircleIcon weight="bold" size="90%" />
					</Center>
					<Title order={2} size="h1" ta="center">
						{t(($) => $.title)}
					</Title>
					<Text size="lg" c="dimmed" textWrap="pretty">
						{t(($) => $.description)}
					</Text>
				</Stack>
			</Paper>
		</Center>
	);
}
