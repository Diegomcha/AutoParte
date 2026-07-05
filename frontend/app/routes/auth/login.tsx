import {
	Button,
	Center,
	Checkbox,
	Paper,
	PasswordInput,
	Stack,
	TextInput,
	Title,
} from '@mantine/core';
import { isNotEmpty, useForm } from '@mantine/form';
import { useTranslation } from 'react-i18next';
import { useSubmit } from 'react-router';
import AuthService from '../../services/AuthService';
import type { Route } from './+types/login';
import type { LoginRequest } from '~/@types/api';

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
	if (await AuthService.isAuthenticated())
		return AuthService.getSuccessRedirection(request);
}

export async function clientAction({ request }: Route.ClientActionArgs) {
	const values = (await request.json()) as Required<LoginRequest>;
	return await AuthService.performLogin(values);
}

export default function LoginPage({
	actionData: logInSuccess,
}: Route.ComponentProps) {
	const { t } = useTranslation();
	const submit = useSubmit();

	const form = useForm({
		initialValues: {
			username: '',
			password: '',
			rememberMe: false,
		},
		validate: {
			username: isNotEmpty(t(($) => $.auth.login.form.errors.noUsername)),
			password: isNotEmpty(t(($) => $.auth.login.form.errors.noPassword)),
		},
	});

	// Handle login failure by setting a form error on the password field
	if (logInSuccess === false) {
		form.setFieldError(
			'password',
			t(($) => $.auth.login.form.errors.invalidCredentials)
		);
	}

	return (
		<Center bg="dark" className="h-screen">
			<Paper withBorder p="xl" className="w-full sm:max-w-md">
				<Title ta="center" mb="lg">
					{t(($) => $.auth.login.title)}
				</Title>
				<form
					onSubmit={form.onSubmit((creds) =>
						submit(creds, { encType: 'application/json', method: 'POST' })
					)}
				>
					<Stack>
						<TextInput
							key={form.key('username')}
							name="username"
							label={t(($) => $.auth.login.form.username)}
							size="md"
							radius="md"
							{...form.getInputProps('username')}
						/>
						<PasswordInput
							key={form.key('password')}
							name="password"
							label={t(($) => $.auth.login.form.password)}
							type="password"
							size="md"
							radius="md"
							{...form.getInputProps('password')}
						/>
						<Checkbox
							key={form.key('rememberMe')}
							name="remember-me"
							label={t(($) => $.auth.login.form.rememberMe)}
							size="md"
							radius="md"
							{...form.getInputProps('rememberMe')}
						/>
						<Button
							type="submit"
							size="md"
							radius="md"
							loading={form.submitting}
						>
							{t(($) => $.auth.login.form.submit)}
						</Button>
					</Stack>
				</form>
			</Paper>
		</Center>
	);
}
