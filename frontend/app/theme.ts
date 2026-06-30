import { ActionIcon, Button, createTheme } from '@mantine/core';

export const theme = createTheme({
	components: {
		Button: Button.extend({
			defaultProps: {
				variant: 'light',
			},
		}),
		ActionIcon: ActionIcon.extend({
			defaultProps: {
				variant: 'subtle',
				size: 'sm',
			},
		}),
	},
});
