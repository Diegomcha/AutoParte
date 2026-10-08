import { startTransition, StrictMode } from "react";
import { HydratedRouter } from "react-router/dom";

import * as Sentry from "@sentry/react-router";
import { hydrateRoot } from "react-dom/client";

Sentry.init({
	dsn: "https://3951cc5a5940576fd5df54b8c1075c15@o4511343046426624.ingest.de.sentry.io/4511484336472144",
	dataCollection: {
		// To disable sending user data and HTTP bodies, uncomment the lines below. For more info visit:
		// https://docs.sentry.io/platforms/javascript/guides/react-router/configuration/options/#dataCollection
		userInfo: false,
		cookies: false,
		httpHeaders: {
			request: { deny: ["forwarded", "-ip", "remote-", "via", "-user"] },
			response: { deny: ["forwarded", "-ip", "remote-", "via", "-user"] }
		},
		httpBodies: [],
		urlQueryParams: { deny: ["forwarded", "-ip", "remote-", "via", "-user"] },
		genAI: { inputs: false, outputs: false },
		databaseQueryData: false,
		graphQL: { document: false, variables: false }
	},
	integrations: [
		// Registers and configures the Tracing integration,
		// which automatically instruments your application to monitor its
		// performance, including custom React Router routing instrumentation
		Sentry.reactRouterTracingIntegration(),
		// Registers the Replay integration,
		// which automatically captures Session Replays
		Sentry.replayIntegration()
	],
	environment: import.meta.env.MODE,
	// Set tracesSampleRate to 1.0 to capture 100%
	// of transactions for tracing.
	// Learn more at
	// https://docs.sentry.io/platforms/javascript/guides/react-router/configuration/options/#traces-sample-rate
	tracesSampleRate: import.meta.env.PROD ? 0.1 : 1.0,
	tracePropagationTargets: [
		// Matches relative paths (e.g., fetch('/api/...')) and absolute URLs containing '/api'
		"/api",
		// Matches all relative endpoints on your app if you have endpoints outside /api
		/^\//
	],
	// Capture Replay for 10% of all sessions,
	// plus 100% of sessions with an error
	// Learn more at
	// https://docs.sentry.io/platforms/javascript/guides/react-router/session-replay/configuration/#general-integration-configuration
	replaysSessionSampleRate: 0.1,
	replaysOnErrorSampleRate: 1.0
});

startTransition(() => {
	hydrateRoot(
		document,
		<StrictMode>
			<HydratedRouter />
		</StrictMode>
	);
});
