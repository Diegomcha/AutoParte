import { useNavigate } from "react-router";

import { Button, Group, Modal, TextInput, useModalsStack } from "@mantine/core";
import { isEmail, isNotEmpty, useForm } from "@mantine/form";

import { UserCirclePlusIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import EmployeeCredsModal from "~/component/admin/employees/EmployeeCredsModal";
import useStaticModalStackTransition from "~/hooks/useStaticModalStackTransition";
import { queryFactory } from "~/services/Api";

export default function NewEmployee() {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const stack = useModalsStack(["new", "created"]);
	const { close } = useStaticModalStackTransition(
		stack,
		"new",
		() => void navigate("/admin/employees")
	);

	const form = useForm({
		mode: "uncontrolled",
		initialValues: {
			name: "",
			surname: "",
			email: ""
		},
		validate: {
			name: isNotEmpty(
				t(($) => $.admin.employees.properties.name.errors.noName)
			),
			surname: isNotEmpty(
				t(($) => $.admin.employees.properties.surname.errors.noSurname)
			),
			email: isEmail(
				t(($) => $.admin.employees.properties.email.errors.invalidEmail)
			)
		}
	});

	const {
		mutate: create,
		data: created,
		isPending: isCreating
	} = useMutation(queryFactory.employees.create());

	return (
		<Modal.Stack>
			<Modal
				{...stack.register("new")}
				onClose={close}
				title={t(($) => $.admin.employees.new.title)}
			>
				<form
					onSubmit={form.onSubmit((data) => {
						create(data, {
							onSuccess: (created) => {
								if (created) stack.open("created");
								else
									form.setFieldError(
										"email",
										t(
											($) =>
												$.admin.employees.properties.email.errors.emailInUse
										)
									);
							}
						});
					})}
				>
					<Group grow>
						<TextInput
							key={form.key("name")}
							name="name"
							label={t(($) => $.admin.employees.properties.name.label)}
							{...form.getInputProps("name")}
						/>
						<TextInput
							key={form.key("surname")}
							name="surname"
							label={t(($) => $.admin.employees.properties.surname.label)}
							{...form.getInputProps("surname")}
						/>
					</Group>
					<TextInput
						key={form.key("email")}
						name="email"
						label={t(($) => $.admin.employees.properties.email.label)}
						{...form.getInputProps("email")}
					/>
					<Group justify="right" mt="md">
						<Button
							type="submit"
							loading={isCreating}
							leftSection={<UserCirclePlusIcon />}
						>
							{t(($) => $.buttons.create)}
						</Button>
					</Group>
				</form>
			</Modal>
			{created && (
				<EmployeeCredsModal
					{...stack.register("created")}
					creds={created}
					onClose={close}
					title={t(($) => $.admin.employees.new.created.title)}
					description={t(($) => $.admin.employees.new.created.description)}
				/>
			)}
		</Modal.Stack>
	);
}
