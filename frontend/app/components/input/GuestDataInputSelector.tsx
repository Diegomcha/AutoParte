import { Button, Stack, Text } from "@mantine/core";

import { IdentificationCardIcon, TextboxIcon } from "@phosphor-icons/react";
import { useTranslation } from "react-i18next";

export default function GuestDataInputSelector({
	onSelection
}: Readonly<{
	onSelection: (selected: "scanning" | "manual") => void;
}>) {
	const { t } = useTranslation("components", {
		keyPrefix: "guestDataInputSelector"
	});
	return (
		<Stack>
			<Text size="xl" fw="bold">
				{t(($) => $.message)}
			</Text>
			<Stack gap="sm">
				<Button
					color={t(($) => $.options.scanning.color)}
					leftSection={<IdentificationCardIcon size={32} />}
					size="xl"
					justify="left"
					styles={{
						label: {
							textWrap: "pretty"
						}
					}}
					onClick={() => {
						onSelection("scanning");
					}}
				>
					{t(($) => $.options.scanning.label)}
				</Button>
				<Button
					color={t(($) => $.options.manual.color)}
					leftSection={<TextboxIcon size={32} />}
					size="xl"
					justify="left"
					styles={{
						label: {
							textWrap: "pretty"
						}
					}}
					onClick={() => {
						onSelection("manual");
					}}
				>
					{t(($) => $.options.manual.label)}
				</Button>
			</Stack>
		</Stack>
	);
}
