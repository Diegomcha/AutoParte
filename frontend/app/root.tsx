import {
	Button,
	Center,
	Code,
	ColorSchemeScript,
	Group,
	mantineHtmlProps,
	MantineProvider,
	Stack,
	Text,
	Title,
} from '@mantine/core';
import { Notifications } from '@mantine/notifications';
import { NavigationProgress, nprogress } from '@mantine/nprogress';
import * as Sentry from '@sentry/react-router';
import { QueryClientProvider } from '@tanstack/react-query';
import { polyfillCountryFlagEmojis } from 'country-flag-emoji-polyfill';
import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import {
	isRouteErrorResponse,
	Links,
	Meta,
	Outlet,
	Scripts,
	ScrollRestoration,
	useFetchers,
	useNavigation,
} from 'react-router';
import { ApiErrorResponse, queryClient } from './api';
import AuthService from './services/AuthService';
import { ValidationErrorResponse } from './services/Validators';
import { theme } from './theme';
import type { Route } from './+types/root';
//
import './i18n';
import './app.css';

polyfillCountryFlagEmojis();

export function Layout({ children }: Readonly<{ children: React.ReactNode }>) {
	return (
		<html lang="en" {...mantineHtmlProps}>
			<head>
				<meta charSet="utf-8" />
				<meta name="viewport" content="width=device-width, initial-scale=1" />
				<ColorSchemeScript />
				<Meta />
				<Links />
			</head>
			<body>
				<MantineProvider theme={theme}>
					<QueryClientProvider client={queryClient}>
						<NavigationProgress />
						{children}
					</QueryClientProvider>
					<Notifications />
				</MantineProvider>
				<ScrollRestoration />
				<Scripts />
			</body>
		</html>
	);
}

export function HydrateFallback() {
	useEffect(() => {
		nprogress.start();
	}, []);
}

export default function App() {
	const navigation = useNavigation();
	const fetchers = useFetchers();

	useEffect(() => {
		const fetchersIdle = fetchers.every((f) => f.state === 'idle');
		if (navigation.state === 'idle' && fetchersIdle) {
			nprogress.complete();
		} else {
			nprogress.start();
		}
	}, [navigation.state, fetchers]);

	return <Outlet />;
}

export function ErrorBoundary({ error }: Route.ErrorBoundaryProps) {
	// Display error message to user
	const { t } = useTranslation();

	let status = '500';
	let stack;
	let showRetry = true;

	// Handle route error responses
	if (isRouteErrorResponse(error)) {
		status = error.status.toString();
		if (status.startsWith('4')) showRetry = false;
	}
	// Handle API error responses
	else if (ApiErrorResponse.isApiErrorResponse(error)) {
		if ([404, 403].includes(error.status)) {
			status = error.status.toString();
			showRetry = false;
		} else if (error.status === 401) {
			void AuthService.refreshLoggedInUser().then(() => {
				location.reload();
			});
			return <></>;
		} else if (error.status.toString().startsWith('5')) status = '503';
	}
	// Handle validation error responses
	else if (ValidationErrorResponse.isValidationErrorResponse(error)) {
		status = '400';
		showRetry = false;
	}

	// Log error to Sentry
	if (error instanceof Error && status.startsWith('5')) {
		console.error(error);
		Sentry.captureException(error);
	}

	// Show stack trace in development mode
	if (import.meta.env.DEV && error instanceof Error) stack = error.stack;

	const message = t(($) => $.error.status[status as 'default'], {
		defaultValue: t(($) => $.error.status.default),
	});

	return (
		<Center component="main" className="h-screen">
			<Stack>
				<p className="text-9xl font-black text-center">{status}</p>
				<Title ta="center">{message}</Title>
				{showRetry && (
					<>
						<Text size="lg" ta="center">
							{t(($) => $.error.disclaimer)}
						</Text>
						<Group justify="center">
							<Button
								size="md"
								onClick={() => {
									location.reload();
								}}
							>
								{t(($) => $.error.refreshButton)}
							</Button>
						</Group>
					</>
				)}
				{stack && <Code block>{stack}</Code>}
			</Stack>
		</Center>
	);
}
