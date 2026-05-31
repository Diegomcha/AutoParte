import * as Sentry from '@sentry/react-router';
import { startTransition, StrictMode } from 'react';
import { hydrateRoot } from 'react-dom/client';
import { HydratedRouter } from 'react-router/dom';

const tracing = Sentry.reactRouterTracingIntegration({
	useInstrumentationAPI: true,
});

Sentry.init({
	dsn: 'https://3951cc5a5940576fd5df54b8c1075c15@o4511343046426624.ingest.de.sentry.io/4511484336472144',
	sendDefaultPii: true,
	integrations: [tracing, Sentry.replayIntegration()],
	enableLogs: true,
	tracesSampleRate: 1,
	tracePropagationTargets: [/^\//],
	replaysSessionSampleRate: 0.1,
	replaysOnErrorSampleRate: 1,
});

startTransition(() => {
	hydrateRoot(
		document,
		<StrictMode>
			<HydratedRouter
				unstable_instrumentations={[tracing.clientInstrumentation]}
			/>
		</StrictMode>
	);
});
