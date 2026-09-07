import {
	Button,
	Fieldset,
	Group,
	Select,
	SimpleGrid,
	Space,
	Stack,
	Text,
	TextInput,
} from '@mantine/core';
import { DateInput } from '@mantine/dates';
import { formRootRule, isEmail, isNotEmpty, useForm } from '@mantine/form';
import {
	ArrowUUpLeftIcon,
	FloppyDiskIcon,
	ScanIcon,
} from '@phosphor-icons/react';
import { useMutation, useSuspenseQueries } from '@tanstack/react-query';
import { queryFactory } from '~/services/Api';
import TimeService from '~/services/TimeService';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import AddressSelect from './AddressSelect';
import ComplexRequiredAsterisk from './ComplexRequiredLabel';
import CountrySelect from './CountrySelect';
import PhoneInput, { isValidPhoneNumber } from './PhoneInput';
import type { PersonDtoRequest, PersonDtoResponse } from '~/@types/api';
import type { CountryCode } from '~/services/CountryService';

interface PersonFormProps {
	accommodationId: string;
	bookingId: string;
	person?: PersonDtoResponse;
	handleCreatedPerson?: (id: string) => void;
	readOnly?: boolean;
}

export default function PersonForm({
	accommodationId,
	bookingId,
	person,
	handleCreatedPerson,
	readOnly,
}: Readonly<PersonFormProps>) {
	const { t } = useTranslation();

	const { countries, genders, relationships, documentTypes } =
		useSuspenseQueries({
			queries: [
				queryFactory.catalogue.countries.list(),
				queryFactory.catalogue.genders(),
				queryFactory.catalogue.relationships(),
				queryFactory.catalogue.documentTypes(),
			],
			combine: (result) => {
				return {
					countries: result[0].data,
					genders: result[1].data,
					relationships: result[2].data,
					documentTypes: result[3].data,
				};
			},
		});

	const form = useForm({
		initialValues: {
			personalInfo: {
				name: person?.personalInfo.name ?? '',
				firstSurname: person?.personalInfo.firstSurname ?? '',
				secondSurname: person?.personalInfo.secondSurname ?? '',
				nationality: person?.personalInfo.nationality ?? null,
				birthDate: person?.personalInfo.birthDate ?? '',
				gender: person?.personalInfo.gender ?? null,
			},
			contactInfo: {
				phoneNumber1: person?.contactInfo.phoneNumber1 ?? '',
				phoneNumber2: person?.contactInfo.phoneNumber2 ?? '',
				email: person?.contactInfo.email ?? '',
			},
			document: {
				type: person?.document?.type ?? null,
				number: person?.document?.number ?? '',
				supportNumber: person?.document?.supportNumber ?? '',
			},
			address: person?.address ?? null,
			relationship: person?.relationship ?? null,
		},
		validate: {
			personalInfo: {
				name: isNotEmpty(
					t(($) => $.people.properties.personalInfo.name.errors.undefined)
				),
				firstSurname: isNotEmpty(
					t(
						($) =>
							$.people.properties.personalInfo.firstSurname.errors.undefined
					)
				),
				birthDate: (value) => {
					if (value && TimeService(value).isAfter(TimeService()))
						return t(
							($) => $.people.properties.personalInfo.birthDate.errors.inFuture
						);
				},
			},
			contactInfo: {
				[formRootRule]: (values) => {
					if (!(values.email || values.phoneNumber1 || values.phoneNumber2))
						return true;
				},
				email: (value) => {
					if (value && isEmail()(value))
						return t(
							($) => $.people.properties.contactInfo.email.errors.invalid
						);
				},
				phoneNumber1: isValidPhoneNumber(),
				phoneNumber2: isValidPhoneNumber(),
			},
			document: {
				number: (value) => {
					if (form.values.document.type) {
						if (isNotEmpty()(value))
							return t(
								($) => $.people.properties.document.number.errors.undefined
							);

						if (
							['NIF', 'NIE'].includes(form.values.document.type) &&
							!isValidNif(value)
						)
							return t(
								($) => $.people.properties.document.number.errors.invalidDni
							);
					}
				},
				supportNumber: (value) => {
					if (requiresSupportNumber(form.values.document.type)) {
						if (isNotEmpty()(value))
							return t(
								($) =>
									$.people.properties.document.supportNumber.errors.undefined
							);
					}
				},
			},
		},
		transformValues: (values) =>
			({
				personalInfo: {
					name: values.personalInfo.name,
					firstSurname: values.personalInfo.firstSurname,
					secondSurname: values.personalInfo.secondSurname || undefined,
					nationality: values.personalInfo.nationality ?? undefined,
					birthDate: values.personalInfo.birthDate
						? TimeService(values.personalInfo.birthDate).toISOString()
						: undefined,
					gender: values.personalInfo.gender ?? undefined,
				},
				contactInfo: {
					email: values.contactInfo.email || undefined,
					phoneNumber1: values.contactInfo.phoneNumber1 || undefined,
					phoneNumber2: values.contactInfo.phoneNumber2 || undefined,
				},
				document: values.document.type
					? {
							type: values.document.type,
							number: values.document.number,
							supportNumber: values.document.supportNumber || undefined,
						}
					: undefined,
				address: values.address ?? undefined,
				relationship: values.relationship ?? undefined,
			}) satisfies PersonDtoRequest,
		onValuesChange: (values, prevValues) => {
			if (values.document.type !== prevValues.document.type) {
				form.clearFieldError('document.number');
				form.clearFieldError('document.supportNumber');
			}
		},
	});

	const isAdult = form.values.personalInfo.birthDate
		? TimeService().diff(
				TimeService(form.values.personalInfo.birthDate),
				'year'
			) >= 18
		: undefined;

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
			person?.id ?? 'non-existent-id'
		)
	);

	const isSaving = isUpdating || isCreating;

	return (
		<form
			onSubmit={form.onSubmit((values) => {
				// Handle updating
				if (person != null)
					update(values, {
						onSuccess: () => {
							form.resetDirty();
						},
					});
				// Handle creating
				else
					create(values, {
						onSuccess: (created) => {
							handleCreatedPerson?.(created.id);
							form.resetDirty();
						},
					});
			})}
			onReset={form.onReset}
		>
			<Stack>
				<Fieldset legend={t(($) => $.people.properties.personalInfo.title)}>
					<SimpleGrid cols={3}>
						<TextInput
							key={form.key('personalInfo.name')}
							label={t(($) => $.people.properties.personalInfo.name.label)}
							withAsterisk
							readOnly={readOnly}
							{...form.getInputProps('personalInfo.name')}
						/>
						<TextInput
							key={form.key('personalInfo.firstSurname')}
							label={t(
								($) => $.people.properties.personalInfo.firstSurname.label
							)}
							withAsterisk
							readOnly={readOnly}
							{...form.getInputProps('personalInfo.firstSurname')}
						/>
						<TextInput
							key={form.key('personalInfo.secondSurname')}
							label={
								<>
									{t(
										($) => $.people.properties.personalInfo.secondSurname.label
									)}
									{form.values.document.type === 'NIF' && (
										<ComplexRequiredAsterisk action="checkIn" />
									)}
								</>
							}
							readOnly={readOnly}
							{...form.getInputProps('personalInfo.secondSurname')}
						/>
						<CountrySelect
							key={form.key('personalInfo.nationality')}
							label={t(
								($) => $.people.properties.personalInfo.nationality.label
							)}
							countries={countries as CountryCode[]}
							clearable
							readOnly={readOnly}
							{...form.getInputProps('personalInfo.nationality')}
						/>
						<DateInput
							key={form.key('personalInfo.birthDate')}
							label={
								<>
									{t(($) => $.people.properties.personalInfo.birthDate.label)}
									{<ComplexRequiredAsterisk action="checkIn" />}
								</>
							}
							valueFormat={t(
								($) => $.people.properties.personalInfo.birthDate.format
							)}
							clearable
							readOnly={readOnly}
							{...form.getInputProps('personalInfo.birthDate')}
						/>
						<Select
							key={form.key('personalInfo.gender')}
							label={t(($) => $.people.properties.personalInfo.gender.label)}
							data={genders.map((g) => ({
								value: g,
								label: t(
									($) =>
										$.people.properties.personalInfo.gender.options[g as 'MALE']
								),
							}))}
							checkIconPosition="right"
							clearable
							readOnly={readOnly}
							{...form.getInputProps('personalInfo.gender')}
						/>
						<AddressSelect
							key={form.key('address')}
							label={
								<>
									{t(($) => $.people.properties.personalInfo.address.label)}
									{<ComplexRequiredAsterisk action="checkIn" />}
								</>
							}
							accommodationId={accommodationId}
							bookingId={bookingId}
							clearable
							readOnly={readOnly}
							{...form.getInputProps('address')}
						/>
						<Select
							key={form.key('relationship')}
							label={
								<>
									{t(
										($) => $.people.properties.personalInfo.relationship.label
									)}
									{isAdult === false && (
										<ComplexRequiredAsterisk action="checkIn" />
									)}
								</>
							}
							data={relationships.map((r) => ({
								value: r,
								label: t(
									($) =>
										$.people.properties.personalInfo.relationship.options[
											r as 'GRANDPARENT'
										]
								),
							}))}
							checkIconPosition="right"
							searchable
							clearable
							readOnly={readOnly}
							{...form.getInputProps('relationship')}
						/>
					</SimpleGrid>
				</Fieldset>
				<Fieldset legend={t(($) => $.people.properties.contactInfo.title)}>
					<SimpleGrid cols={3}>
						<TextInput
							key={form.key('contactInfo.email')}
							label={t(($) => $.people.properties.contactInfo.email.label)}
							readOnly={readOnly}
							{...form.getInputProps('contactInfo.email')}
						/>
						<PhoneInput
							key={form.key('contactInfo.phoneNumber1')}
							label={t(
								($) => $.people.properties.contactInfo.phoneNumber1.label
							)}
							readOnly={readOnly}
							{...form.getInputProps('contactInfo.phoneNumber1')}
						/>
						<PhoneInput
							key={form.key('contactInfo.phoneNumber2')}
							label={t(
								($) => $.people.properties.contactInfo.phoneNumber2.label
							)}
							readOnly={readOnly}
							{...form.getInputProps('contactInfo.phoneNumber2')}
						/>
					</SimpleGrid>
					<Space h="xs" />
					<Text size="xs" c={form.errors.contactInfo ? 'red' : 'gray'}>
						{t(($) => $.people.properties.contactInfo.constraint)}
					</Text>
				</Fieldset>
				<Fieldset legend={t(($) => $.people.properties.document.title)}>
					<SimpleGrid cols={3}>
						<Select
							key={form.key('document.type')}
							label={
								<>
									{t(($) => $.people.properties.document.type.label)}
									{isAdult && <ComplexRequiredAsterisk action="checkIn" />}
								</>
							}
							data={documentTypes.map((dt) => ({
								value: dt,
								label: t(
									($) => $.people.properties.document.type.options[dt as 'NIF']
								),
							}))}
							checkIconPosition="right"
							clearable
							readOnly={readOnly}
							{...form.getInputProps('document.type')}
						/>
						<TextInput
							key={form.key('document.number')}
							label={t(($) => $.people.properties.document.number.label)}
							disabled={!form.values.document.type}
							readOnly={readOnly}
							withAsterisk={!!form.values.document.type}
							{...form.getInputProps('document.number')}
						/>
						<TextInput
							key={form.key('document.supportNumber')}
							label={t(($) => $.people.properties.document.supportNumber.label)}
							disabled={!requiresSupportNumber(form.values.document.type)}
							readOnly={readOnly}
							withAsterisk={requiresSupportNumber(form.values.document.type)}
							{...form.getInputProps('document.supportNumber')}
						/>
					</SimpleGrid>
				</Fieldset>
				<Group>
					<Button
						component={Link}
						to="./scan" // TODO: Implementar
						leftSection={<ScanIcon weight="bold" size={16} />}
						hidden={readOnly}
					>
						{t(($) => $.people.scan.button)}
					</Button>
					<div style={{ flex: 1 }} />
					<Group gap="xs">
						<Button
							type="reset"
							color="gray"
							leftSection={<ArrowUUpLeftIcon weight="bold" size={16} />}
							loading={isSaving}
							hidden={!form.isDirty() || readOnly}
						>
							{t(($) => $.common.buttons.reset)}
						</Button>
						<Button
							type="submit"
							color="green"
							leftSection={<FloppyDiskIcon weight="bold" size={16} />}
							loading={isSaving}
							disabled={!form.isDirty()}
							hidden={readOnly}
						>
							{t(($) =>
								person == null ? $.common.buttons.add : $.common.buttons.save
							)}
						</Button>
					</Group>
				</Group>
			</Stack>
		</form>
	);
}

function requiresSupportNumber(documentType?: string | null) {
	return ['NIF', 'NIE'].includes(documentType ?? '');
}

function isValidNif(nif: string) {
	// Check the format of the NIF / NIE
	if (!/^(\d{8})([A-Z])$/.test(nif) && !/^[XYZ]\d{7}[A-Z]$/.test(nif)) {
		return false;
	}

	// Get number part
	const number = Number.parseInt(
		nif.slice(0, -1).replace('X', '0').replace('Y', '1').replace('Z', '2')
	);

	// Compare the control letter with the expected one
	return 'TRWAGMYFPDXBNJZSQVHLCKE'[number % 23] === nif.charAt(8);
}
