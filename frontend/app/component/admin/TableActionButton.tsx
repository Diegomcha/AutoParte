import { ActionIcon, polymorphic, Tooltip } from "@mantine/core";

import type { ActionIconProps } from "@mantine/core";

interface TableActionButtonProps extends ActionIconProps {
	tooltip: string;
}

const TableActionButton = polymorphic<"button", TableActionButtonProps>(
	({ tooltip, children, ...props }: TableActionButtonProps) => (
		<Tooltip label={tooltip}>
			<ActionIcon {...props}>{children}</ActionIcon>
		</Tooltip>
	)
);

export default TableActionButton;
