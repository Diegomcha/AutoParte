import { ActionIcon, Tooltip } from '@mantine/core';

export default function TableActionButton({
	children,
	tooltip,
	...props
}: React.PropsWithChildren &
	ActionIcon.Props & {
		tooltip: string;
	}) {
	return (
		<Tooltip label={tooltip}>
			<ActionIcon {...props}>{children}</ActionIcon>
		</Tooltip>
	);
}
