import { useNavigate } from "react-router";

import {
	Button,
	Center,
	Paper,
	PasswordInput,
	Stack,
	Title
} from "@mantine/core";
import { isNotEmpty, useForm } from "@mantine/form";

import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import NewPasswordInput, {
	isPasswordStrongEnough
} from "~/component/NewPasswordInput";
import { queryFactory } from "~/services/Api";
import AuthService from "~/services/AuthService";

import type { UpdatePasswordDto } from "~/@types/api";
import type { Route } from "./+types/updatePassword";

export async function clientLoader() {
	const user = await AuthService.getLoggedInUser();

	// If not authenticated user, redirect to login page
	if (!user) return AuthService.getLoginRedirection();

	return {
		username: user.username
	};
}

export default function UpdatePassword({
	loaderData: { username }
}: Route.ComponentProps) {
	const navigate = useNavigate();

	const { t } = useTranslation("routes", { keyPrefix: "auth.updatePassword" });

	const { mutate: updatePassword, isPending } = useMutation(
		queryFactory.auth.updatePassword()
	);

	const form = useForm<UpdatePasswordDto>({
		mode: "uncontrolled",
		initialValues: {
			username,
			currentPassword: "",
			newPassword: ""
		},
		validate: {
			currentPassword: isNotEmpty(
				t(($) => $.form.currentPassword.errors.noCurrentPassword)
			),
			newPassword: isPasswordStrongEnough()
		}
	});

	return (
		<Center bg="dark" h="100vh">
			<Paper withBorder p="xl" w="100%" maw="28rem">
				<Title ta="center" mb="lg">
					{t(($) => $.title)}
				</Title>
				<form
					onSubmit={form.onSubmit((updatePasswordData) => {
						updatePassword(updatePasswordData, {
							onSuccess: (ok) => {
								if (!ok)
									form.setFieldError(
										"currentPassword",
										t(($) => $.form.currentPassword.errors.invalidCredentials)
									);
								else void navigate("/");
							}
						});
					})}
				>
					<Stack>
						<PasswordInput
							key={form.key("currentPassword")}
							label={t(($) => $.form.currentPassword.label)}
							autoComplete="current-password"
							size="md"
							radius="md"
							{...form.getInputProps("currentPassword")}
						/>
						<NewPasswordInput
							key={form.key("newPassword")}
							label={t(($) => $.form.newPassword.label)}
							autoComplete="new-password"
							size="md"
							radius="md"
							{...form.getInputProps("newPassword")}
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
