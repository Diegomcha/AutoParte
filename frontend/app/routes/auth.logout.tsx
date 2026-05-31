import { Button, Title } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import { Form } from 'react-router';
import AuthService from '../services/AuthService';
import type { Route } from './+types/auth.logout';

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
	if (!(await AuthService.isAuthenticated()))
		return AuthService.getLoginRedirection(request);
}

export async function clientAction({ request }: Route.ClientActionArgs) {
	return (await AuthService.performLogout())
		? AuthService.getSuccessRedirection(request)
		: AuthService.getLoginRedirection(request);
}

export default function LoginPage() {
	const { t } = useTranslation();

	return (
		<>
			<Title>{t(($) => $.auth.logout.title)}</Title>
			<Form method="post">
				<Button type="submit">{t(($) => $.auth.logout.button)}</Button>
			</Form>
		</>
	);
}
