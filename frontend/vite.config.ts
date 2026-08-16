import { reactRouter } from '@react-router/dev/vite';
import { sentryReactRouter } from '@sentry/react-router';
import tailwindcss from '@tailwindcss/vite';
import { defineConfig } from 'vite';
import devTools from 'vite-plugin-devtools-json';

export default defineConfig((config) => ({
	plugins: [
		devTools(),
		tailwindcss(),
		reactRouter(),
		sentryReactRouter(
			{
				org: 'diegomcha',
				project: 'autoparte-front',
				telemetry: false,
			},
			config
		),
	],
	resolve: {
		tsconfigPaths: true,
	},
	server: {
		proxy: {
			'/api': {
				target: 'http://localhost:8080',
				changeOrigin: true,
				proxyTimeout: 5000,
			},
		},
	},
}));
