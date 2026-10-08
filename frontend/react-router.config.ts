import { sentryOnBuildEnd } from "@sentry/react-router/vite";

import type { Config } from "@react-router/dev/config";

export default {
	ssr: false,
	buildEnd: sentryOnBuildEnd
} satisfies Config;
