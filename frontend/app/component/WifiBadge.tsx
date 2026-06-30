import {
	QuestionMarkIcon,
	WifiHighIcon,
	WifiSlashIcon,
} from '@phosphor-icons/react';
import BooleanBadge from './BooleanBadge';

export default function WifiBadge({
	value,
}: Readonly<{
	value?: boolean;
}>) {
	return (
		<BooleanBadge
			value={value}
			icons={{
				true: <WifiHighIcon />,
				false: <WifiSlashIcon />,
				undefined: <QuestionMarkIcon />,
			}}
		/>
	);
}
