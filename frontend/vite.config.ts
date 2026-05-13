import { reactRouter } from '@react-router/dev/vite';
import { defineConfig } from 'vite';
import devTools from 'vite-plugin-devtools-json';

export default defineConfig({
	plugins: [devTools(), reactRouter()],
	resolve: {
		tsconfigPaths: true,
	},
	server: {
		proxy: {
			'/api': {
				target: 'http://localhost:8080',
				changeOrigin: true,
			},
		},
	},
});
