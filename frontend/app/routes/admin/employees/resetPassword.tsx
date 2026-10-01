import { useNavigate } from "react-router";

import { Button, Group, Modal, Text, useModalsStack } from "@mantine/core";

import { ClockClockwiseIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import EmployeeCredsModal from "~/component/admin/employees/EmployeeCredsModal";
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
	const { t } = useTranslation("routes", {
		keyPrefix: "admin.employees.resetPassword"
	});

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
				title={t(($) => $.title)}
			>
				<Text>{t(($) => $.description)}</Text>
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
						{t(($) => $.button)}
					</Button>
				</Group>
			</Modal>
			{creds && (
				<EmployeeCredsModal
					{...stack.register("credentials")}
					creds={creds}
					onClose={close}
					title={t(($) => $.done.title)}
					description={t(($) => $.done.description)}
				/>
			)}
		</Modal.Stack>
	);
}
