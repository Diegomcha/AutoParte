import { useState } from "react";

import {
	ActionIcon,
	Button,
	Fieldset,
	Group,
	Select,
	SimpleGrid,
	Space,
	Stack,
	Text,
	TextInput
} from "@mantine/core";
import { DateInput } from "@mantine/dates";
import { formRootRule, isEmail, isNotEmpty, useForm } from "@mantine/form";

import {
	ArrowUUpLeftIcon,
	FloppyDiskIcon,
	ScanIcon
} from "@phosphor-icons/react";
import { useMutation, useSuspenseQueries } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import { queryFactory } from "~/services/Api";
import CountryService from "~/services/CountryService";
import TimeService from "~/services/TimeService";

import AddressSelect from "./AddressSelect";
import ComplexRequiredAsterisk from "./ComplexRequiredLabel";
import CountrySelect from "./CountrySelect";
import DocumentScanner from "./DocumentScanner";
import PhoneInput, { isValidPhoneNumber } from "./PhoneInput";

import type { PersonDtoRequest, PersonDtoResponse } from "~/@types/api";
import type { CountryCode } from "~/services/CountryService";

interface PersonFormProps {
	accommodationId: string;
	bookingId: string;
	person?: PersonDtoResponse;
	readOnly?: boolean;
	onCreatedPerson?: (id: string) => void;
	onUpdatedPerson?: (id: string) => void;
	onSubmit?: () => void;
	checkInMode?: true;
	defaultMode?: "scanning" | "manual";
}

export default function PersonForm({
	accommodationId,
	bookingId,
	person,
	readOnly,
	onCreatedPerson,
	onUpdatedPerson,
	onSubmit,
	checkInMode,
	defaultMode = "manual"
}: Readonly<PersonFormProps>) {
	const { t: tCommon } = useTranslation();
	const { t: tPerson } = useTranslation("entities", {
		keyPrefix: "person"
	});
	// const { t } = useTranslation("components", {
	// 	keyPrefix: "personForm"
	// });

	const { countries, genders, relationships, documentTypes } =
		useSuspenseQueries({
			queries: [
				queryFactory.catalogue.countries.list(),
				queryFactory.catalogue.genders(),
				queryFactory.catalogue.relationships(),
				queryFactory.catalogue.documentTypes()
			],
			combine: (result) => {
				return {
					countries: result[0].data,
					genders: result[1].data,
					relationships: result[2].data,
					documentTypes: result[3].data
				};
			}
		});

	const form = useForm({
		mode: "uncontrolled",
		initialValues: {
			personalInfo: {
				name: person?.personalInfo.name ?? "",
				firstSurname: person?.personalInfo.firstSurname ?? "",
				secondSurname: person?.personalInfo.secondSurname ?? "",
				nationality: person?.personalInfo.nationality ?? null,
				birthDate: person?.personalInfo.birthDate ?? "",
				gender: person?.personalInfo.gender ?? null
			},
			contactInfo: {
				phoneNumber1: person?.contactInfo.phoneNumber1 ?? "",
				phoneNumber2: person?.contactInfo.phoneNumber2 ?? "",
				email: person?.contactInfo.email ?? ""
			},
			document: {
				type: person?.document?.type ?? null,
				number: person?.document?.number ?? "",
				supportNumber: person?.document?.supportNumber ?? ""
			},
			address: person?.address ?? null,
			relationship: person?.relationship ?? null
		},
		validate: {
			personalInfo: {
				name: isNotEmpty(tPerson(($) => $.personalInfo.name.errors.undefined)),
				firstSurname: isNotEmpty(
					tPerson(($) => $.personalInfo.firstSurname.errors.undefined)
				),
				secondSurname: (value, values) => {
					if (checkInMode && values.document.type === "NIF" && !value.trim())
						return tPerson(
							($) => $.personalInfo.secondSurname.errors.undefined
						);
				},
				birthDate: (value) => {
					if (checkInMode && !value)
						return tPerson(($) => $.personalInfo.birthDate.errors.undefined);

					if (value && TimeService(value).isAfter(TimeService()))
						return tPerson(($) => $.personalInfo.birthDate.errors.inFuture);
				}
			},
			contactInfo: {
				[formRootRule]: (values) => {
					if (!(values.email || values.phoneNumber1 || values.phoneNumber2))
						return true;
				},
				email: (value) => {
					if (value && isEmail()(value))
						return tPerson(($) => $.contactInfo.email.errors.invalid);
				},
				phoneNumber1: isValidPhoneNumber(),
				phoneNumber2: isValidPhoneNumber()
			},
			document: {
				type: (value, values) => {
					if (checkInMode && isAdult(values.personalInfo.birthDate) && !value)
						return tPerson(($) => $.document.type.errors.undefined);
				},
				number: (value, values) => {
					if (values.document.type) {
						if (!value)
							return tPerson(($) => $.document.number.errors.undefined);

						if (
							["NIF", "NIE"].includes(values.document.type) &&
							!isValidNif(value)
						)
							return tPerson(($) => $.document.number.errors.invalidDni);
					}
				},
				supportNumber: (value, values) => {
					if (requiresSupportNumber(values.document.type)) {
						if (isNotEmpty()(value))
							return tPerson(($) => $.document.supportNumber.errors.undefined);
					}
				}
			},
			address: (value) => {
				if (checkInMode && !value)
					return tPerson(($) => $.personalInfo.address.errors.undefined);
			},
			relationship: (value, values) => {
				if (
					checkInMode &&
					isAdult(values.personalInfo.birthDate) === false &&
					!value
				)
					return tPerson(($) => $.personalInfo.relationship.errors.undefined);
			}
		},
		transformValues: (values) =>
			({
				personalInfo: {
					name: values.personalInfo.name.trim(),
					firstSurname: values.personalInfo.firstSurname.trim(),
					secondSurname: values.personalInfo.secondSurname.trim() || undefined,
					nationality: values.personalInfo.nationality ?? undefined,
					birthDate: values.personalInfo.birthDate
						? TimeService(values.personalInfo.birthDate).toISOString()
						: undefined,
					gender: values.personalInfo.gender ?? undefined
				},
				contactInfo: {
					email: values.contactInfo.email.trim() || undefined,
					phoneNumber1: values.contactInfo.phoneNumber1.trim() || undefined,
					phoneNumber2: values.contactInfo.phoneNumber2.trim() || undefined
				},
				document: values.document.type
					? {
							type: values.document.type,
							number: values.document.number.trim(),
							supportNumber: values.document.supportNumber.trim() || undefined
						}
					: undefined,
				address: values.address ?? undefined,
				relationship: values.relationship ?? undefined
			}) satisfies PersonDtoRequest,
		onValuesChange: (values, prevValues) => {
			if (values.document.type !== prevValues.document.type) {
				form.clearFieldError("document.number");
				form.clearFieldError("document.supportNumber");
			}
		}
	});

	const watchedFormValues = {
		personalInfo: {
			birthDate: form.useWatchValue("personalInfo.birthDate"),
			nationality: form.useWatchValue("personalInfo.nationality")
		},
		document: {
			type: form.useWatchValue("document.type")
		}
	};

	const { mutate: create, isPending: isCreating } = useMutation(
		queryFactory.accommodations.bookings.people.create(
			accommodationId,
			bookingId
		)
	);
	const { mutate: update, isPending: isUpdating } = useMutation(
		queryFactory.accommodations.bookings.people.update(
			accommodationId,
			bookingId,
			person?.id ?? "non-existent-id"
		)
	);

	const isSaving = isUpdating || isCreating;

	// Scanning mode
	const [mode, setMode] = useState<"scanning" | "manual">(defaultMode);
	if (mode === "scanning")
		return (
			<DocumentScanner
				w="100%"
				onBack={() => {
					setMode("manual");
				}}
				onScanSuccess={(scan) => {
					form.setValues({
						...scan,
						personalInfo: {
							...scan.personalInfo,
							secondSurname: scan.personalInfo.secondSurname ?? ""
						},
						document: {
							...scan.document,
							supportNumber: scan.document.supportNumber ?? ""
						}
					});
					void form.validate();
					setMode("manual");
				}}
			/>
		);

	return (
		<form
			onSubmit={form.onSubmit((values) => {
				// Handle updating
				if (person != null)
					update(values, {
						onSuccess: () => {
							onUpdatedPerson?.(person.id);
							form.resetDirty();
						}
					});
				// Handle creating
				else if (form.isDirty())
					create(values, {
						onSuccess: (created) => {
							onCreatedPerson?.(created.id);
							form.resetDirty();
						}
					});

				onSubmit?.();
			})}
			onReset={form.onReset}
		>
			<Stack>
				<Fieldset legend={tPerson(($) => $.personalInfo.title)}>
					<SimpleGrid cols={{ base: 1, sm: 2, md: 3 }}>
						<TextInput
							type="text"
							autoComplete="given-name"
							key={form.key("personalInfo.name")}
							label={tPerson(($) => $.personalInfo.name.label)}
							withAsterisk
							readOnly={readOnly}
							{...form.getInputProps("personalInfo.name")}
						/>
						<TextInput
							type="text"
							autoComplete="family-name"
							key={form.key("personalInfo.firstSurname")}
							label={tPerson(($) => $.personalInfo.firstSurname.label)}
							withAsterisk
							readOnly={readOnly}
							{...form.getInputProps("personalInfo.firstSurname")}
						/>
						<TextInput
							type="text"
							key={form.key("personalInfo.secondSurname")}
							label={
								<>
									{tPerson(($) => $.personalInfo.secondSurname.label)}
									{!checkInMode &&
										watchedFormValues.document.type === "NIF" && (
											<ComplexRequiredAsterisk action="checkIn" />
										)}
								</>
							}
							withAsterisk={
								checkInMode && watchedFormValues.document.type === "NIF"
							}
							readOnly={readOnly}
							{...form.getInputProps("personalInfo.secondSurname")}
						/>
						<CountrySelect
							key={form.key("personalInfo.nationality")}
							label={tPerson(($) => $.personalInfo.nationality.label)}
							countries={countries as CountryCode[]}
							clearable
							readOnly={readOnly}
							{...form.getInputProps("personalInfo.nationality")}
						/>
						<DateInput
							autoComplete="bday"
							key={form.key("personalInfo.birthDate")}
							label={
								<>
									{tPerson(($) => $.personalInfo.birthDate.label)}
									{!checkInMode && <ComplexRequiredAsterisk action="checkIn" />}
								</>
							}
							withAsterisk={checkInMode}
							valueFormat={tPerson(($) => $.personalInfo.birthDate.format)}
							clearable
							readOnly={readOnly}
							{...form.getInputProps("personalInfo.birthDate")}
						/>
						<Select
							autoComplete="sex"
							key={form.key("personalInfo.gender")}
							label={tPerson(($) => $.personalInfo.gender.label)}
							data={genders.map((g) => ({
								value: g,
								label: tPerson(
									($) => $.personalInfo.gender.options[g as "MALE"]
								)
							}))}
							checkIconPosition="right"
							clearable
							readOnly={readOnly}
							{...form.getInputProps("personalInfo.gender")}
						/>
						<AddressSelect
							key={form.key("address")}
							label={
								<>
									{tPerson(($) => $.personalInfo.address.label)}
									{!checkInMode && <ComplexRequiredAsterisk action="checkIn" />}
								</>
							}
							withAsterisk={checkInMode}
							accommodationId={accommodationId}
							bookingId={bookingId}
							clearable
							readOnly={readOnly}
							{...form.getInputProps("address")}
						/>
						<Select
							key={form.key("relationship")}
							label={
								<>
									{tPerson(($) => $.personalInfo.relationship.label)}
									{!checkInMode &&
										isAdult(watchedFormValues.personalInfo.birthDate) ===
											false && <ComplexRequiredAsterisk action="checkIn" />}
								</>
							}
							withAsterisk={
								checkInMode &&
								isAdult(watchedFormValues.personalInfo.birthDate) === false
							}
							data={relationships.map((r) => ({
								value: r,
								label: tPerson(
									($) => $.personalInfo.relationship.options[r as "GRANDPARENT"]
								)
							}))}
							checkIconPosition="right"
							searchable
							clearable
							readOnly={readOnly}
							{...form.getInputProps("relationship")}
						/>
					</SimpleGrid>
				</Fieldset>
				<Fieldset legend={tPerson(($) => $.contactInfo.title)}>
					<SimpleGrid cols={{ base: 1, sm: 2, md: 3 }}>
						<TextInput
							type="email"
							autoComplete="email"
							key={form.key("contactInfo.email")}
							label={tPerson(($) => $.contactInfo.email.label)}
							readOnly={readOnly}
							{...form.getInputProps("contactInfo.email")}
						/>
						<PhoneInput
							key={form.key("contactInfo.phoneNumber1")}
							label={tPerson(($) => $.contactInfo.phoneNumber1.label)}
							readOnly={readOnly}
							defaultCountry={CountryService.getPhoneCountryCode(
								watchedFormValues.personalInfo.nationality as CountryCode
							)}
							{...form.getInputProps("contactInfo.phoneNumber1")}
						/>
						<PhoneInput
							key={form.key("contactInfo.phoneNumber2")}
							label={tPerson(($) => $.contactInfo.phoneNumber2.label)}
							readOnly={readOnly}
							defaultCountry={CountryService.getPhoneCountryCode(
								watchedFormValues.personalInfo.nationality as CountryCode
							)}
							{...form.getInputProps("contactInfo.phoneNumber2")}
						/>
					</SimpleGrid>
					<Space h="xs" />
					<Text size="xs" c={form.errors.contactInfo ? "red" : "gray"}>
						{tPerson(($) => $.contactInfo.constraint)}
					</Text>
				</Fieldset>
				<Fieldset legend={tPerson(($) => $.document.title)}>
					<SimpleGrid cols={{ base: 1, sm: 2, md: 3 }}>
						<Select
							key={form.key("document.type")}
							withAsterisk={
								checkInMode && isAdult(watchedFormValues.personalInfo.birthDate)
							}
							label={
								<>
									{tPerson(($) => $.document.type.label)}
									{!checkInMode &&
										isAdult(watchedFormValues.personalInfo.birthDate) && (
											<ComplexRequiredAsterisk action="checkIn" />
										)}
								</>
							}
							data={documentTypes.map((dt) => ({
								value: dt,
								label: tPerson(($) => $.document.type.options[dt as "NIF"])
							}))}
							checkIconPosition="right"
							clearable
							readOnly={readOnly}
							{...form.getInputProps("document.type")}
						/>
						<TextInput
							type="text"
							key={form.key("document.number")}
							label={tPerson(($) => $.document.number.label)}
							disabled={!watchedFormValues.document.type}
							readOnly={readOnly}
							withAsterisk={!!watchedFormValues.document.type}
							{...form.getInputProps("document.number")}
						/>
						<TextInput
							type="text"
							key={form.key("document.supportNumber")}
							label={tPerson(($) => $.document.supportNumber.label)}
							disabled={!requiresSupportNumber(watchedFormValues.document.type)}
							readOnly={readOnly}
							withAsterisk={requiresSupportNumber(
								watchedFormValues.document.type
							)}
							{...form.getInputProps("document.supportNumber")}
						/>
					</SimpleGrid>
				</Fieldset>
				<Group>
					<Button
						onClick={() => {
							setMode("scanning");
						}}
						leftSection={<ScanIcon weight="bold" size={16} />}
						hidden={readOnly}
						visibleFrom="xs"
					>
						{tCommon(($) => $.buttons.scan)}
					</Button>
					<ActionIcon
						variant="light"
						size="input-sm"
						onClick={() => {
							setMode("scanning");
						}}
						hidden={readOnly}
						hiddenFrom="xs"
					>
						<ScanIcon weight="bold" size={16} />
					</ActionIcon>
					<div style={{ flex: 1 }} />
					<Group gap="xs">
						<Button
							type="reset"
							color="gray"
							leftSection={<ArrowUUpLeftIcon weight="bold" size={16} />}
							loading={isSaving}
							hidden={!form.isDirty() || readOnly}
						>
							{tCommon(($) => $.buttons.reset)}
						</Button>
						<Button
							type="submit"
							color="green"
							leftSection={<FloppyDiskIcon weight="bold" size={16} />}
							loading={isSaving}
							disabled={!(checkInMode ?? form.isDirty())}
							hidden={readOnly}
						>
							{tCommon(($) =>
								person == null ? $.buttons.add : $.buttons.save
							)}
						</Button>
					</Group>
				</Group>
			</Stack>
		</form>
	);
}

/**
 * Returns whether the person is an adult (18 years or older) based on their birth date.
 * May return undefined if the birth date is not provided.
 * @param birthDate String representing the person's birth date.
 * @returns True if the person is an adult, false if they are a minor, or undefined if the birth date is not provided.
 */
function isAdult(birthDate?: string) {
	return birthDate
		? TimeService().diff(TimeService(birthDate), "year") >= 18
		: undefined;
}

/**
 * Returns whether a support number is required for the given document type.
 * @param documentType The type of the document.
 * @returns True if a support number is required, false otherwise.
 */
function requiresSupportNumber(documentType?: string | null) {
	return ["NIF", "NIE"].includes(documentType ?? "");
}

/**
 * Checks if the given NIF (Número de Identificación Fiscal) is valid.
 * @param nif The NIF to validate.
 * @returns True if the NIF is valid, false otherwise.
 */
function isValidNif(nif: string) {
	// Check the format of the NIF / NIE
	if (!/^(\d{8})([A-Z])$/.test(nif) && !/^[XYZ]\d{7}[A-Z]$/.test(nif)) {
		return false;
	}

	// Get number part
	const number = Number.parseInt(
		nif.slice(0, -1).replace("X", "0").replace("Y", "1").replace("Z", "2")
	);

	// Compare the control letter with the expected one
	return "TRWAGMYFPDXBNJZSQVHLCKE"[number % 23] === nif.charAt(8);
}
