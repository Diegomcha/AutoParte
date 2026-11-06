import { JsonViewer } from "@mantine/code-highlight";

import { useTranslation } from "react-i18next";

import type { JsonViewerProps } from "@mantine/code-highlight";

export default function JSONViewer(props: Readonly<JsonViewerProps>) {
	const { t } = useTranslation("components", { keyPrefix: "jsonViewer" });

	return (
		<JsonViewer
			withSize
			withLineNumbers
			withChevrons
			withControls
			withCopy
			withCopyButton
			copyLabel={t(($) => $.copyLabel)}
			copiedLabel={t(($) => $.copiedLabel)}
			expandAllLabel={t(($) => $.expandAllLabel)}
			collapseAllLabel={t(($) => $.collapseAllLabel)}
			{...props}
		/>
	);
}
