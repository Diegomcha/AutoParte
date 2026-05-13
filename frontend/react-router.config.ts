import type { Config } from '@react-router/dev/config';

export default {
	// Build as an SPA so Spring Boot can serve static assets.
	ssr: false,
	prerender: true,
} satisfies Config;
