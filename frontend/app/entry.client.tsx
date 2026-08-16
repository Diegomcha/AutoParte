import * as Sentry from '@sentry/react-router';
import { startTransition, StrictMode } from 'react';
import { hydrateRoot } from 'react-dom/client';
import { HydratedRouter } from 'react-router/dom';

Sentry.init({
	dsn: 'https://3951cc5a5940576fd5df54b8c1075c15@o4511343046426624.ingest.de.sentry.io/4511484336472144',
	dataCollection: {
		// To disable sending user data and HTTP bodies, uncomment the lines below. For more info visit:
		// https://docs.sentry.io/platforms/javascript/guides/react-router/configuration/options/#dataCollection
		userInfo: false,
		httpBodies: [],
	},
	integrations: [
		// Registers and configures the Tracing integration,
		// which automatically instruments your application to monitor its
		// performance, including custom React Router routing instrumentation
		Sentry.reactRouterTracingIntegration(),
		// Registers the Replay integration,
		// which automatically captures Session Replays
		Sentry.replayIntegration(),
	],
	environment: import.meta.env.MODE,
	// Enable logs to be sent to Sentry
	enableLogs: true,
	// Set tracesSampleRate to 1.0 to capture 100%
	// of transactions for tracing.
	// TODO: We recommend adjusting this value in production
	// Learn more at
	// https://docs.sentry.io/platforms/javascript/guides/react-router/configuration/options/#traces-sample-rate
	tracesSampleRate: 1.0,
	// TODO: Set `tracePropagationTargets` to declare which URL(s) should have trace propagation enabled
	tracePropagationTargets: [/^\//, /^https:\/\/yourserver\.io\/api/],
	// Capture Replay for 10% of all sessions,
	// plus 100% of sessions with an error
	// Learn more at
	// https://docs.sentry.io/platforms/javascript/guides/react-router/session-replay/configuration/#general-integration-configuration
	replaysSessionSampleRate: 0.1,
	replaysOnErrorSampleRate: 1.0,
});

startTransition(() => {
	hydrateRoot(
		document,
		<StrictMode>
			<HydratedRouter />
		</StrictMode>
	);
});
