import {
	Button,
	Checkbox,
	PasswordInput,
	TextInput,
	Title,
} from '@mantine/core';
import { isNotEmpty, useForm } from '@mantine/form';
import { useTranslation } from 'react-i18next';
import AuthService from '../services/AuthService';
import type { Route } from './+types/auth.login';
import type { LoginRequest } from '~/@types/api';

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
	if (await AuthService.isAuthenticated())
		return AuthService.getSuccessRedirection(request);
}

export default function LoginPage() {
	const { t } = useTranslation();

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

	const login = async (values: Required<LoginRequest>) => {
		const success = await AuthService.performLogin(values);
		if (!success) {
			form.setFieldError(
				'password',
				t(($) => $.auth.login.form.errors.invalidCredentials)
			);
			return;
		}
		location.reload();
	};

	return (
		<>
			<Title>{t(($) => $.auth.login.title)}</Title>
			<form onSubmit={form.onSubmit(login)}>
				<TextInput
					key={form.key('username')}
					name="username"
					label={t(($) => $.auth.login.form.username)}
					{...form.getInputProps('username')}
					// type="email"
				/>
				<PasswordInput
					key={form.key('password')}
					name="password"
					label={t(($) => $.auth.login.form.password)}
					type="password"
					{...form.getInputProps('password')}
				/>
				<Checkbox
					key={form.key('rememberMe')}
					name="remember-me"
					label={t(($) => $.auth.login.form.rememberMe)}
					{...form.getInputProps('rememberMe')}
				/>
				<Button type="submit">{t(($) => $.auth.login.form.submit)}</Button>
			</form>
		</>
	);
}
