import { useNavigate } from "react-router";

import { Button, Center, Paper, Stack, Title } from "@mantine/core";
import { useForm } from "@mantine/form";

import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import NewPasswordInput, {
	isPasswordStrongEnough
} from "~/component/NewPasswordInput";
import { queryFactory } from "~/services/Api";
import AuthService from "~/services/AuthService";
import Validators from "~/services/Validators";

import type { UpdatePasswordDto } from "~/@types/api";
import type { Route } from "./+types/createPassword";

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
	if (await AuthService.isAuthenticated())
		throw Validators.throwValidationErrorResponse(
			"User is already authenticated, cannot create password.\nUse the update password route instead."
		);

	const url = new URL(request.url);
	const username = url.searchParams.get("username");
	const currentPassword = url.searchParams.get("currentPassword");
	const redirect = url.searchParams.get("redirect") ?? "/";

	if (!username || !currentPassword)
		throw Validators.throwValidationErrorResponse(
			"Missing username or currentPassword query parameters."
		);

	return { username, currentPassword, redirect };
}

export default function CreatePassword({
	loaderData: { username, currentPassword, redirect }
}: Route.ComponentProps) {
	const navigate = useNavigate();

	const { t } = useTranslation("routes", { keyPrefix: "auth.createPassword" });

	const { mutate: createPassword, isPending: isCreatingPassword } = useMutation(
		queryFactory.auth.updatePassword()
	);
	const { mutate: login, isPending: isLoggingIn } = useMutation(
		queryFactory.auth.login()
	);

	const isPending = isCreatingPassword || isLoggingIn;

	const form = useForm<UpdatePasswordDto>({
		mode: "uncontrolled",
		initialValues: {
			username,
			currentPassword,
			newPassword: ""
		},
		validate: {
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
						createPassword(updatePasswordData, {
							onSuccess: () => {
								login(
									{
										username: updatePasswordData.username,
										password: updatePasswordData.newPassword
									},
									{
										onSuccess: () => {
											void navigate(redirect);
										}
									}
								);
							}
						});
					})}
				>
					<Stack>
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
