import { ActionIcon, Button, createTheme, DEFAULT_THEME } from '@mantine/core';

export const theme = createTheme({
	fontFamily: "'Twemoji Country Flags'," + DEFAULT_THEME.fontFamily,
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
