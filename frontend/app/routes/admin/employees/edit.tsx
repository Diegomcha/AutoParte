import { useNavigate } from "react-router";

import {
	Button,
	Chip,
	Group,
	Modal,
	MultiSelect,
	Stack,
	TextInput
} from "@mantine/core";
import { isEmail, isNotEmpty, useForm } from "@mantine/form";

import { CheckCircleIcon, FloppyDiskIcon } from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { Route } from "./+types/edit";

export async function clientLoader({ params: { id } }: Route.ClientLoaderArgs) {
	Validators.validateUuids(id);

	return {
		employee: await queryClient.query(queryFactory.employees.detail(id)),
		availableAccommodations: await queryClient.query(
			queryFactory.accommodations.list()
		)
	};
}

export default function EditEmployee({
	loaderData: { employee, availableAccommodations }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const { opened, close } = useStaticModalTransition(
		() => void navigate("/admin/employees")
	);

	const form = useForm({
		initialValues: {
			enabled: employee.enabled,
			name: employee.name,
			surname: employee.surname,
			email: employee.email,
			accommodations: employee.accommodations.map(
				(accommodation) => accommodation.id
			)
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

	const { mutate: edit, isPending: isEditing } = useMutation(
		queryFactory.employees.update(employee.id)
	);
	const { mutate: linkAccommodations, isPending: isLinkingAccommodations } =
		useMutation(
			queryFactory.employees.accommodations.linkMultiple(employee.id)
		);
	const { mutate: unlinkAccommodations, isPending: isUnlinkingAccommodations } =
		useMutation(
			queryFactory.employees.accommodations.unlinkMultiple(employee.id)
		);
	const isPending =
		isEditing || isLinkingAccommodations || isUnlinkingAccommodations;

	return (
		<Modal
			opened={opened}
			onClose={close}
			title={t(($) => $.admin.employees.edit.title)}
		>
			<form
				onSubmit={form.onSubmit((data) => {
					edit(data, {
						onSuccess: (success) => {
							if (!success) {
								form.setFieldError(
									"email",
									t(($) => $.admin.employees.properties.email.errors.emailInUse)
								);
								return;
							}

							// Determine which accommodations were added and which were removed & perform the necessary link/unlink operations
							const { addedAccommodations, removedAccommodations } =
								extractAccommodationsChange(
									employee.accommodations.map((a) => a.id),
									data.accommodations
								);

							linkAccommodations(addedAccommodations, {
								onSuccess: () => {
									unlinkAccommodations(removedAccommodations, {
										onSuccess: close
									});
								}
							});
						}
					});
				})}
			>
				<Stack gap="xs">
					<Chip
						key={form.key("enabled")}
						icon={<CheckCircleIcon />}
						color="green"
						variant="light"
						{...form.getInputProps("enabled", { type: "checkbox" })}
					>
						{form.getValues().enabled
							? t(($) => $.admin.employees.properties.enabled.states.enabled)
							: t(($) => $.admin.employees.properties.enabled.states.disabled)}
					</Chip>
					<div>
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
					</div>
					<MultiSelect
						key={form.key("accommodations")}
						label={t(($) => $.admin.employees.properties.accommodations.label)}
						data={availableAccommodations.map((accommodation) => ({
							value: accommodation.id,
							label: accommodation.name
						}))}
						nothingFoundMessage={t(
							($) => $.admin.employees.properties.accommodations.none
						)}
						{...form.getInputProps("accommodations")}
					/>
				</Stack>
				<Group justify="right" mt="md">
					<Button
						type="submit"
						loading={isPending}
						leftSection={<FloppyDiskIcon />}
					>
						{t(($) => $.buttons.save)}
					</Button>
				</Group>
			</form>
		</Modal>
	);
}

/**
 * Compares the old and new accommodations assigned to an employee and returns the added and removed accommodations.
 * @param oldAccommodations Old accommodation ids assigned to the employee
 * @param newAccommodations New accommodation ids assigned to the employee
 * @returns An object containing the added and removed accommodations
 */
function extractAccommodationsChange(
	oldAccommodations: string[],
	newAccommodations: string[]
) {
	return {
		addedAccommodations: newAccommodations.filter(
			(newAccommodation) => !oldAccommodations.includes(newAccommodation)
		),
		removedAccommodations: oldAccommodations.filter(
			(oldAccommodation) => !newAccommodations.includes(oldAccommodation)
		)
	};
}
