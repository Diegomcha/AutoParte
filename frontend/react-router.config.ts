import { sentryOnBuildEnd } from '@sentry/react-router';
import type { Config } from '@react-router/dev/config';

export default {
	ssr: false,
	prerender: true,
	buildEnd: async ({ viteConfig, reactRouterConfig, buildManifest }) => {
		await sentryOnBuildEnd({
			viteConfig,
			reactRouterConfig,
			buildManifest,
		});
	},
} satisfies Config;
