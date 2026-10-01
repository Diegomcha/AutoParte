import { useNavigate } from "react-router";

import {
	Button,
	Group,
	Modal,
	MultiSelect,
	Space,
	Stack,
	TextInput
} from "@mantine/core";
import { isNotEmpty, useForm } from "@mantine/form";

import { FloppyDiskIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import BooleanInputWithUndefined from "~/component/input/BooleanInputWithUndefined";
import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { Route } from "./+types/edit";

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	Validators.validateUuids(id);

	return {
		accommodation: await queryClient.query(
			queryFactory.accommodations.detail(id)
		),
		availableEmployees: await queryClient.query(queryFactory.employees.list())
	};
}

export default function EditAccommodation({
	loaderData: { accommodation, availableEmployees }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation("routes", {
		keyPrefix: "admin.accommodations.edit"
	});
	const { t: tCommon } = useTranslation();
	const { t: tAccommodation } = useTranslation("entities", {
		keyPrefix: "accommodation"
	});

	const { opened, close } = useStaticModalTransition(
		() => void navigate("/admin/accommodations")
	);

	const form = useForm({
		mode: "uncontrolled",
		initialValues: {
			name: accommodation.name,
			sesCode: accommodation.sesCode,
			employees: accommodation.employees.map((employee) => employee.id),
			internetConnection: String(accommodation.internetConnection ?? undefined)
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

	const { mutate: edit, isPending: isEditing } = useMutation(
		queryFactory.accommodations.update(accommodation.id)
	);
	const { mutate: linkEmployees, isPending: isLinkingEmployees } = useMutation(
		queryFactory.accommodations.employees.linkMultiple(accommodation.id)
	);
	const { mutate: unlinkEmployees, isPending: isUnlinkingEmployees } =
		useMutation(
			queryFactory.accommodations.employees.unlinkMultiple(accommodation.id)
		);
	const isPending = isEditing || isLinkingEmployees || isUnlinkingEmployees;

	return (
		<Modal opened={opened} onClose={close} title={t(($) => $.title)}>
			<form
				onSubmit={form.onSubmit((data) => {
					edit(data, {
						onSuccess: ([ok, errorCode]) => {
							if (!ok) {
								if (errorCode === "NAME_IN_USE") {
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
								return;
							}

							// Determine which employees were added and which were removed & perform the necessary link/unlink operations
							const { addedEmployees, removedEmployees } =
								extractEmployeesChange(
									accommodation.employees.map((e) => e.id),
									data.employees
								);

							linkEmployees(addedEmployees, {
								onSuccess: () => {
									unlinkEmployees(removedEmployees, {
										onSuccess: close
									});
								}
							});
						}
					});
				})}
			>
				<Stack gap="xs">
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
					<Space />
					<MultiSelect
						key={form.key("employees")}
						label={tAccommodation(($) => $.employees.label)}
						data={availableEmployees.map((employee) => ({
							value: employee.id,
							label: `${employee.name} ${employee.surname}`
						}))}
						nothingFoundMessage={t(($) => $.form.noAvailableEmployees)}
						{...form.getInputProps("employees")}
					/>
				</Stack>
				<Group justify="right" mt="md">
					<Button
						type="submit"
						loading={isPending}
						leftSection={<FloppyDiskIcon />}
					>
						{tCommon(($) => $.buttons.save)}
					</Button>
				</Group>
			</form>
		</Modal>
	);
}

/**
 * Compares the old and new employees assigned to an accommodation and returns the added and removed employees.
 * @param oldEmployees Old employee ids assigned to the accommodation
 * @param newEmployees New employee ids assigned to the accommodation
 * @returns An object containing the added and removed employees
 */
function extractEmployeesChange(
	oldEmployees: string[],
	newEmployees: string[]
) {
	return {
		addedEmployees: newEmployees.filter(
			(newEmployee) => !oldEmployees.includes(newEmployee)
		),
		removedEmployees: oldEmployees.filter(
			(oldEmployee) => !newEmployees.includes(oldEmployee)
		)
	};
}
