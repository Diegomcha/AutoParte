import { useNavigate } from "react-router";

import { Button, Group, Modal, Stack, TextInput } from "@mantine/core";
import { isNotEmpty, useForm } from "@mantine/form";

import { PlusIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import BooleanInputWithUndefined from "~/component/input/BooleanInputWithUndefined";
import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryFactory } from "~/services/Api";

export default function NewAccommodation() {
	const navigate = useNavigate();
	const { t } = useTranslation("routes", {
		keyPrefix: "admin.accommodations.new"
	});
	const { t: tAccommodation } = useTranslation("entities", {
		keyPrefix: "accommodation"
	});
	const { t: tCommon } = useTranslation();

	const { opened, close } = useStaticModalTransition(
		() => void navigate("/admin/accommodations")
	);

	const form = useForm({
		mode: "uncontrolled",
		initialValues: {
			name: "",
			sesCode: "",
			internetConnection: "undefined"
		},
		validate: {
			name: isNotEmpty(tAccommodation(($) => $.name.errors.noName)),
			sesCode: isNotEmpty(tAccommodation(($) => $.sesCode.errors.noSesCode))
		},
		transformValues: (values) => ({
			...values,
			internetConnection:
				values.internetConnection === "undefined"
					? undefined
					: values.internetConnection === "true"
		})
	});

	const { mutate: create, isPending: isCreating } = useMutation(
		queryFactory.accommodations.create()
	);

	return (
		<Modal opened={opened} onClose={close} title={t(($) => $.title)}>
			<form
				onSubmit={form.onSubmit((data) => {
					create(data, {
						onSuccess: ([ok, errorCode]) => {
							if (ok) close();
							else if (errorCode === "NAME_IN_USE") {
								form.setFieldError(
									"name",
									tAccommodation(($) => $.name.errors.nameInUse)
								);
							} else {
								form.setFieldError(
									"sesCode",
									tAccommodation(($) => $.sesCode.errors.sesCodeInUse)
								);
							}
						}
					});
				})}
			>
				<Stack>
					<Group grow>
						<TextInput
							key={form.key("name")}
							name="name"
							label={tAccommodation(($) => $.name.label)}
							withAsterisk
							{...form.getInputProps("name")}
						/>
						<TextInput
							key={form.key("sesCode")}
							name="sesCode"
							label={tAccommodation(($) => $.sesCode.label)}
							withAsterisk
							{...form.getInputProps("sesCode")}
						/>
					</Group>
					<BooleanInputWithUndefined
						key={form.key("internetConnection")}
						name="internetConnection"
						label={tAccommodation(($) => $.internetConnection.label)}
						withAsterisk
						{...form.getInputProps("internetConnection")}
					/>
				</Stack>
				<Group justify="right" mt="md">
					<Button type="submit" loading={isCreating} leftSection={<PlusIcon />}>
						{tCommon(($) => $.buttons.create)}
					</Button>
				</Group>
			</form>
		</Modal>
	);
}
