import { useNavigate } from "react-router";

import {
	Button,
	Center,
	Checkbox,
	Paper,
	PasswordInput,
	Stack,
	TextInput,
	Title
} from "@mantine/core";
import { isNotEmpty, useForm } from "@mantine/form";

import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import { queryFactory } from "~/services/Api";
import AuthService from "~/services/AuthService";

import type { Route } from "./+types/login";

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
	// Redirect to the appropriate page if the user is already authenticated
	if (await AuthService.isAuthenticated(true))
		return AuthService.getSuccessRedirection(request);

	return {
		successRedirectPath: AuthService.getSuccessRedirectionPath(request.url)
	};
}

export default function LoginPage({
	loaderData: { successRedirectPath }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation("routes", { keyPrefix: "auth.login" });

	const { mutate: login, isPending } = useMutation(queryFactory.auth.login());

	const form = useForm({
		mode: "uncontrolled",
		initialValues: {
			username: "",
			password: "",
			rememberMe: false
		},
		validate: {
			username: isNotEmpty(t(($) => $.form.username.errors.noUsername)),
			password: isNotEmpty(t(($) => $.form.password.errors.noPassword))
		}
	});

	return (
		<Center bg="dark" h="100vh">
			<Paper withBorder p="xl" w="100%" maw="28rem">
				<Title ta="center" mb="lg">
					{t(($) => $.title)}
				</Title>
				<form
					onSubmit={form.onSubmit((creds) => {
						login(creds, {
							onSuccess: (res) => {
								if (res === true) void navigate(successRedirectPath);
								else if (res === "USER_CREDENTIALS_EXPIRED")
									void navigate(
										AuthService.getCreatePasswordRedirectionPath(
											creds,
											successRedirectPath
										)
									);
								else
									form.setFieldError(
										"password",
										t(($) => $.backendErrors[res])
									);
							}
						});
					})}
				>
					<Stack>
						<TextInput
							key={form.key("username")}
							label={t(($) => $.form.username.label)}
							autoComplete="username"
							size="md"
							radius="md"
							{...form.getInputProps("username")}
						/>
						<PasswordInput
							key={form.key("password")}
							label={t(($) => $.form.password.label)}
							autoComplete="current-password"
							size="md"
							radius="md"
							{...form.getInputProps("password")}
						/>
						<Checkbox
							key={form.key("rememberMe")}
							label={t(($) => $.form.rememberMe.label)}
							size="md"
							radius="md"
							{...form.getInputProps("rememberMe")}
						/>
						<Button type="submit" size="md" radius="md" loading={isPending}>
							{t(($) => $.form.submit)}
						</Button>
					</Stack>
				</form>
			</Paper>
		</Center>
	);
}
