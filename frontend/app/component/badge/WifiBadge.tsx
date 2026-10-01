import {
	QuestionMarkIcon,
	WifiHighIcon,
	WifiSlashIcon
} from "@phosphor-icons/react";

import BooleanBadge from "./BooleanBadge";

export default function WifiBadge({
	value
}: Readonly<{
	value?: boolean;
}>) {
	return (
		<BooleanBadge
			value={value}
			displayOpts={{
				true: {
					icon: <WifiHighIcon />
				},
				false: {
					icon: <WifiSlashIcon />
				},
				null: {
					icon: <QuestionMarkIcon />
				}
			}}
		/>
	);
}
