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
import { NavigationProgress, nprogress } from '@mantine/nprogress';
import * as Sentry from '@sentry/react-router';
import { QueryClientProvider } from '@tanstack/react-query';
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
import { theme } from './theme';
import type { Route } from './+types/root';
//
import './i18n';
import './dayjs';
import './app.css';
import AuthService from './services/AuthService';
import { ValidationErrorResponse } from './services/Validators';

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
	// Log error to Sentry if error
	if (error instanceof Error) {
		console.error(error);
		Sentry.captureException(error);
	}

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
	if (ApiErrorResponse.isApiErrorResponse(error)) {
		if (error.status === 404) {
			status = '404';
			showRetry = false;
		} else if (error.status === 401) {
			void AuthService.refreshLoggedInUser().then(() => {
				location.reload();
			});
			return <></>;
		} else if (error.status.toString().startsWith('5')) status = '503';
	}

	// Handle validation error responses
	if (ValidationErrorResponse.isValidationErrorResponse(error)) {
		status = '400';
		showRetry = false;
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
								Refresh the page
							</Button>
						</Group>
					</>
				)}
				{stack && <Code block>{stack}</Code>}
			</Stack>
		</Center>
	);
}
