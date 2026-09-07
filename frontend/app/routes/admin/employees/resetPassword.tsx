import { useNavigate } from "react-router";

import { Button, Group, Modal, Text, useModalsStack } from "@mantine/core";

import { ClockClockwiseIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import EmployeeCredsModal from "~/component/EmployeeCredsModal";
import useStaticModalStackTransition from "~/hooks/useStaticModalStackTransition";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { Route } from "./+types/resetPassword";

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	Validators.validateUuids(id);

	await queryClient.query(queryFactory.employees.detail(id));
}

export default function ResetPasswordEmployee({
	params: { id }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const stack = useModalsStack(["confirmation", "credentials"]);
	const { close } = useStaticModalStackTransition(
		stack,
		"confirmation",
		() => void navigate("/admin/employees")
	);

	const {
		mutate,
		isPending,
		data: creds
	} = useMutation(queryFactory.employees.resetPassword(id));

	return (
		<Modal.Stack>
			<Modal
				{...stack.register("confirmation")}
				onClose={close}
				title={t(($) => $.admin.employees.resetPassword.title)}
			>
				<Text>{t(($) => $.admin.employees.resetPassword.description)}</Text>
				<Group justify="right" mt="md">
					<Button
						color="red"
						loading={isPending}
						leftSection={<ClockClockwiseIcon />}
						onClick={() => {
							mutate(undefined, {
								onSuccess: () => {
									stack.open("credentials");
								}
							});
						}}
					>
						{t(($) => $.admin.employees.resetPassword.button)}
					</Button>
				</Group>
			</Modal>
			{creds && (
				<EmployeeCredsModal
					{...stack.register("credentials")}
					creds={creds}
					onClose={close}
					title={t(($) => $.admin.employees.resetPassword.done.title)}
					description={t(
						($) => $.admin.employees.resetPassword.done.description
					)}
				/>
			)}
		</Modal.Stack>
	);
}
