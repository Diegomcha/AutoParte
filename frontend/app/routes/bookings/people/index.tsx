import {
	ActionIcon,
	Button,
	Fieldset,
	Group,
	Modal,
	Scroller,
	Select,
	SimpleGrid,
	Space,
	Stack,
	Tabs,
	Text,
	TextInput,
} from '@mantine/core';
import { DateInput } from '@mantine/dates';
import { formRootRule, isEmail, isNotEmpty, useForm } from '@mantine/form';
import {
	ArrowUUpLeftIcon,
	FloppyDiskIcon,
	PlusIcon,
	ScanIcon,
	XIcon,
} from '@phosphor-icons/react';
import { useMutation, useSuspenseQuery } from '@tanstack/react-query';
import AddressSelect from '~/component/AddressSelect';
import ComplexRequiredAsterisk from '~/component/ComplexRequiredLabel';
import CountrySelect from '~/component/CountrySelect';
import PhoneInput, { isValidPhoneNumber } from '~/component/PhoneInput';
import useStaticModalTransition from '~/hooks/useStaticModalTransition';
import { queryClient, queryFactory } from '~/services/Api';
import TimeService from '~/services/TimeService';
import Validators from '~/services/Validators';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, Outlet, useNavigate, useOutletContext } from 'react-router';
import { useBooking } from '..';
import type { Route } from './+types/index';
import type {
	AddressDtoResponse,
	PersonDtoRequest,
	PersonDtoResponse,
} from '~/@types/api';
import type { CountryCode } from '~/services/CountryService';

interface ContextType {
	handleNewAddress: (newAddress: AddressDtoResponse) => void;
	resetPerson: () => void;
}

export async function clientLoader({
	params: { accommodationId, bookingId },
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const [
		_people,
		_addresses,
		countries,
		genders,
		relationships,
		documentTypes,
	] = await Promise.all([
		// Pre-fetch people and addresses
		queryClient.query(
			queryFactory.accommodations.bookings.people.list(
				accommodationId,
				bookingId
			)
		),
		queryClient.query(
			queryFactory.accommodations.bookings.addresses.list(
				accommodationId,
				bookingId
			)
		),
		// Catalogues
		queryClient.query(queryFactory.catalogue.countries.list()),
		queryClient.query(queryFactory.catalogue.genders()),
		queryClient.query(queryFactory.catalogue.relationships()),
		queryClient.query(queryFactory.catalogue.documentTypes()),
	]);

	return {
		countries,
		genders,
		relationships,
		documentTypes,
	};
}

export default function BookingPeople({
	params: { accommodationId, bookingId },
	loaderData: { countries, genders, relationships, documentTypes },
}: Route.ComponentProps) {
	const { t } = useTranslation();
	const navigate = useNavigate();

	const { opened, close } = useStaticModalTransition(() => void navigate('..'));
	const { booking } = useBooking();

	const { data: people } = useSuspenseQuery(
		queryFactory.accommodations.bookings.people.list(accommodationId, bookingId)
	);

	const [personId, setPersonId] = useState(people.at(0)?.id ?? null);

	function changePerson(id: string | null) {
		setPersonId(id);

		form.setInitialValues(getInitialValues(people, id));
		form.reset();
	}

	function resetPerson() {
		// changePerson(people.at(0)?.id ?? null);
	}

	// TODO: remove this effect
	useEffect(() => {
		if (personId && people.every((p) => p.id !== personId))
			setPersonId(people.at(0)?.id ?? null);

		form.setInitialValues(getInitialValues(people, personId));
		form.reset();
	}, [people]);

	const { data: addresses } = useSuspenseQuery(
		queryFactory.accommodations.bookings.addresses.list(
			accommodationId,
			bookingId
		)
	);

	const [newAddresses, setNewAddresses] = useState<AddressDtoResponse[]>([]);

	function handleNewAddress(newAddress: AddressDtoResponse) {
		setNewAddresses((prev) => [...prev, newAddress]);
		form.setFieldValue('address', newAddress.id);
	}

	const form = useForm({
		initialValues: getInitialValues(people, personId),
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
			personId ?? 'non-existent-id'
		)
	);

	const isSaving = isUpdating || isCreating;

	return (
		<>
			<Modal
				opened={opened}
				onClose={close}
				title={t(($) => $.people.title)}
				size="auto"
			>
				<Text size="xs" c="gray" mb={'sm'}>
					{t(($) => $.people.requirement)}
				</Text>
				{/* People switcher */}
				<Tabs
					mb="md"
					value={personId ?? 'new'}
					onChange={(id) => {
						changePerson(id !== 'new' ? id : null);
					}}
					w={0}
					miw={'100%'}
				>
					<Tabs.List>
						<Scroller>
							{people.map((person) => (
								<Tabs.Tab
									value={person.id}
									key={person.id}
									styles={{
										tabLabel: {
											textWrap: 'nowrap',
										},
									}}
									rightSection={
										booking.canBeModified && (
											<ActionIcon
												size="xs"
												color="red"
												component={Link}
												to={`./${person.id}/delete`}
											>
												<XIcon />
											</ActionIcon>
										)
									}
								>
									{person.personalInfo.name}{' '}
									{person.personalInfo.firstSurname.at(0)?.toUpperCase()}.
								</Tabs.Tab>
							))}
							{booking.canBeModified &&
								booking.numberOfPeople > people.length && (
									<Tabs.Tab value="new">
										<PlusIcon size={16} />
									</Tabs.Tab>
								)}
						</Scroller>
					</Tabs.List>
				</Tabs>
				{/* Person form */}
				<form
					onSubmit={form.onSubmit((values) => {
						// Handle updating
						if (personId != null) update(values);
						// Handle creating
						else
							create(values, {
								onSuccess: (created) => {
									changePerson(created.id);
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
									readOnly={!booking.canBeModified}
									{...form.getInputProps('personalInfo.name')}
								/>
								<TextInput
									key={form.key('personalInfo.firstSurname')}
									label={t(
										($) => $.people.properties.personalInfo.firstSurname.label
									)}
									withAsterisk
									readOnly={!booking.canBeModified}
									{...form.getInputProps('personalInfo.firstSurname')}
								/>
								<TextInput
									key={form.key('personalInfo.secondSurname')}
									label={
										<>
											{t(
												($) =>
													$.people.properties.personalInfo.secondSurname.label
											)}
											{form.values.document.type === 'NIF' && (
												<ComplexRequiredAsterisk action="checkIn" />
											)}
										</>
									}
									readOnly={!booking.canBeModified}
									{...form.getInputProps('personalInfo.secondSurname')}
								/>
								<CountrySelect
									key={form.key('personalInfo.nationality')}
									label={t(
										($) => $.people.properties.personalInfo.nationality.label
									)}
									countries={countries as CountryCode[]}
									clearable
									readOnly={!booking.canBeModified}
									{...form.getInputProps('personalInfo.nationality')}
								/>
								<DateInput
									key={form.key('personalInfo.birthDate')}
									label={
										<>
											{t(
												($) => $.people.properties.personalInfo.birthDate.label
											)}
											{<ComplexRequiredAsterisk action="checkIn" />}
										</>
									}
									valueFormat={t(
										($) => $.people.properties.personalInfo.birthDate.format
									)}
									clearable
									readOnly={!booking.canBeModified}
									{...form.getInputProps('personalInfo.birthDate')}
								/>
								<Select
									key={form.key('personalInfo.gender')}
									label={t(
										($) => $.people.properties.personalInfo.gender.label
									)}
									data={genders.map((g) => ({
										value: g,
										label: t(
											($) =>
												$.people.properties.personalInfo.gender.options[
													g as 'MALE'
												]
										),
									}))}
									checkIconPosition="right"
									clearable
									readOnly={!booking.canBeModified}
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
									bookingAddresses={addresses}
									newAddresses={newAddresses}
									clearable
									onNew={() => {
										void navigate('new-address');
									}}
									readOnly={!booking.canBeModified}
									{...form.getInputProps('address')}
								/>
								<Select
									key={form.key('relationship')}
									label={
										<>
											{t(
												($) =>
													$.people.properties.personalInfo.relationship.label
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
									readOnly={!booking.canBeModified}
									{...form.getInputProps('relationship')}
								/>
							</SimpleGrid>
						</Fieldset>
						<Fieldset legend={t(($) => $.people.properties.contactInfo.title)}>
							<SimpleGrid cols={3}>
								<TextInput
									key={form.key('contactInfo.email')}
									label={t(($) => $.people.properties.contactInfo.email.label)}
									readOnly={!booking.canBeModified}
									{...form.getInputProps('contactInfo.email')}
								/>
								<PhoneInput
									key={form.key('contactInfo.phoneNumber1')}
									label={t(
										($) => $.people.properties.contactInfo.phoneNumber1.label
									)}
									readOnly={!booking.canBeModified}
									{...form.getInputProps('contactInfo.phoneNumber1')}
								/>
								<PhoneInput
									key={form.key('contactInfo.phoneNumber2')}
									label={t(
										($) => $.people.properties.contactInfo.phoneNumber2.label
									)}
									readOnly={!booking.canBeModified}
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
											($) =>
												$.people.properties.document.type.options[dt as 'NIF']
										),
									}))}
									checkIconPosition="right"
									clearable
									readOnly={!booking.canBeModified}
									{...form.getInputProps('document.type')}
								/>
								<TextInput
									key={form.key('document.number')}
									label={t(($) => $.people.properties.document.number.label)}
									disabled={!form.values.document.type}
									readOnly={!booking.canBeModified}
									withAsterisk={!!form.values.document.type}
									{...form.getInputProps('document.number')}
								/>
								<TextInput
									key={form.key('document.supportNumber')}
									label={t(
										($) => $.people.properties.document.supportNumber.label
									)}
									disabled={!requiresSupportNumber(form.values.document.type)}
									readOnly={!booking.canBeModified}
									withAsterisk={requiresSupportNumber(
										form.values.document.type
									)}
									{...form.getInputProps('document.supportNumber')}
								/>
							</SimpleGrid>
						</Fieldset>
						<Group>
							<Button
								component={Link}
								to="./scan" // TODO: Implementar
								leftSection={<ScanIcon weight="bold" size={16} />}
								hidden={!booking.canBeModified}
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
									hidden={!form.isDirty() || !booking.canBeModified}
								>
									{t(($) => $.common.buttons.reset)}
								</Button>
								<Button
									type="submit"
									color="green"
									leftSection={<FloppyDiskIcon weight="bold" size={16} />}
									loading={isSaving}
									disabled={!form.isDirty()}
									hidden={!booking.canBeModified}
								>
									{t(($) =>
										personId == null
											? $.common.buttons.add
											: $.common.buttons.save
									)}
								</Button>
							</Group>
						</Group>
					</Stack>
				</form>
			</Modal>
			<Outlet
				context={
					{
						handleNewAddress,
						resetPerson,
					} satisfies ContextType
				}
			/>
		</>
	);
}

export function useNewAddressHandler() {
	return useOutletContext<ContextType>().handleNewAddress;
}

export function useResetPerson() {
	return useOutletContext<ContextType>().resetPerson;
}

function getInitialValues(
	people: PersonDtoResponse[],
	personId: string | null
) {
	const person = people.find((p) => p.id === personId) ?? null;
	return {
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
	};
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
