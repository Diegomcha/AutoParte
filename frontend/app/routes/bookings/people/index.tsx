import {
	ActionIcon,
	Button,
	Fieldset,
	Group,
	Modal,
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
import api, { queryClient, throwErrors } from '~/api';
import AddressSelect from '~/component/AddressSelect';
import CountrySelect from '~/component/CountrySelect';
import PhoneInput, { isValidPhoneNumber } from '~/component/PhoneInput';
import TimeService from '~/services/TimeService';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, Outlet, useLocation, useNavigate } from 'react-router';
import { useBooking } from '..';
import type { Route } from './+types/index';
import type {
	AddressDtoResponse,
	PersonDtoRequest,
	PersonDtoResponse,
} from '~/@types/api';
import type { CountryCode } from '~/services/CountryService';

export async function clientLoader({
	params: { accommodationId, bookingId },
}: Route.ClientLoaderArgs) {
	await queryClient.prefetchQuery({
		queryKey: ['bookings', accommodationId, bookingId, 'people'],
		queryFn: async () =>
			throwErrors(
				await api.GET(
					'/api/accommodations/{accommodationId}/bookings/{bookingId}/people',
					{
						params: { path: { accommodationId, bookingId } },
					}
				)
			),
	});

	await queryClient.prefetchQuery({
		queryKey: ['bookings', accommodationId, bookingId, 'addresses'],
		queryFn: async () =>
			throwErrors(
				await api.GET(
					'/api/accommodations/{accommodationId}/bookings/{bookingId}/addresses',
					{
						params: { path: { accommodationId, bookingId } },
					}
				)
			),
	});

	return {
		countries: await queryClient.fetchQuery({
			queryKey: ['catalogue', 'countries'],
			queryFn: async () =>
				throwErrors(await api.GET('/api/catalogue/countries')),
		}),
		genders: await queryClient.fetchQuery({
			queryKey: ['catalogue', 'genders'],
			queryFn: async () =>
				throwErrors(await api.GET('/api/catalogue/person/genders')),
		}),
		relationships: await queryClient.fetchQuery({
			queryKey: ['catalogue', 'relationships'],
			queryFn: async () =>
				throwErrors(await api.GET('/api/catalogue/person/relationships')),
		}),
		documentTypes: await queryClient.fetchQuery({
			queryKey: ['catalogue', 'documentTypes'],
			queryFn: async () =>
				throwErrors(await api.GET('/api/catalogue/document/types')),
		}),
	};
}

export default function BookingPeople({
	params: { accommodationId, bookingId },
	loaderData: { countries, genders, relationships, documentTypes },
}: Route.ComponentProps) {
	const { t } = useTranslation();
	const navigate = useNavigate();
	const location = useLocation();
	const { booking } = useBooking();

	const { data: people } = useSuspenseQuery({
		queryKey: ['bookings', accommodationId, bookingId, 'people'],
		queryFn: async () =>
			throwErrors(
				await api.GET(
					'/api/accommodations/{accommodationId}/bookings/{bookingId}/people',
					{
						params: { path: { accommodationId, bookingId } },
					}
				)
			),
	});

	const { data: addresses } = useSuspenseQuery({
		queryKey: ['bookings', accommodationId, bookingId, 'addresses'],
		queryFn: async () =>
			throwErrors(
				await api.GET(
					'/api/accommodations/{accommodationId}/bookings/{bookingId}/addresses',
					{
						params: { path: { accommodationId, bookingId } },
					}
				)
			),
	});

	const [personId, setPersonId] = useState(people.at(0)?.id ?? null);

	const form = useForm({
		mode: 'uncontrolled',
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
					if (form.getValues().document.type) {
						if (isNotEmpty()(value))
							return t(
								($) => $.people.properties.document.number.errors.undefined
							);

						if (
							['NIF', 'NIE'].includes(form.getValues().document.type ?? '') &&
							!isValidNif(value)
						)
							return t(
								($) => $.people.properties.document.number.errors.invalidDni
							);
					}
				},
				supportNumber: (value) => {
					if (requiresSupportNumber(form.getValues().document.type)) {
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
	});

	useEffect(() => {
		const address = (
			location.state as { address?: AddressDtoResponse } | undefined
		)?.address;
		if (address) {
			addresses.push(address);
			form.setFieldValue('address', address.id);
		}
	}, [location.state]);

	useEffect(() => {
		// If personId stopped being in the people list, set it to the first person or null if there are no people
		if (personId && !people.some((p) => p.id === personId))
			setPersonId(people.at(0)?.id ?? null);

		form.setInitialValues(getInitialValues(people, personId));
		form.reset();
	}, [personId, people]);

	useEffect(() => {
		form.clearFieldError('document.number');
		form.clearFieldError('document.supportNumber');
	}, [form.getValues().document.type]);

	const { mutate: save, isPending: isSaving } = useMutation({
		throwOnError: true,
		mutationFn: async (values: PersonDtoRequest) =>
			personId != null
				? (throwErrors(
						await api.PUT(
							'/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}',
							{
								params: {
									path: {
										accommodationId,
										bookingId,
										id: personId,
									},
								},
								body: values,
							}
						)
					) as undefined) // PUT returns no content
				: throwErrors(
						await api.POST(
							'/api/accommodations/{accommodationId}/bookings/{bookingId}/people',
							{
								params: {
									path: {
										accommodationId,
										bookingId,
									},
								},
								body: values,
							}
						)
					),
		onSuccess: async (created) => {
			await queryClient.invalidateQueries({
				queryKey: ['bookings', accommodationId, bookingId],
			});
			if (created != null) setPersonId(created.id);
		},
	});

	return (
		<>
			<Modal
				opened
				onClose={() => {
					void navigate('..');
				}}
				title={t(($) => $.people.title)}
				size="auto"
			>
				{/* People switcher */}
				<Tabs
					mb="md"
					value={personId ?? 'new'}
					onChange={(id) => {
						setPersonId(id === 'new' ? null : id);
					}}
				>
					<Tabs.List>
						{people.map((person) => (
							<Tabs.Tab
								value={person.id}
								key={person.id}
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
					</Tabs.List>
				</Tabs>
				{/* Person form */}
				<form
					onSubmit={form.onSubmit((values) => {
						save(values);
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
									label={t(
										($) => $.people.properties.personalInfo.secondSurname.label
									)}
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
									label={t(
										($) => $.people.properties.personalInfo.birthDate.label
									)}
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
									label={t(
										($) => $.people.properties.personalInfo.address.label
									)}
									addresses={addresses}
									clearable
									onNew={() => {
										void navigate('new-address');
									}}
									readOnly={!booking.canBeModified}
									{...form.getInputProps('address')}
								/>
								<Select
									key={form.key('relationship')}
									label={t(
										($) => $.people.properties.personalInfo.relationship.label
									)}
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
									label={t(($) => $.people.properties.document.type.label)}
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
									disabled={!form.getValues().document.type}
									readOnly={!booking.canBeModified}
									withAsterisk={!!form.getValues().document.type}
									{...form.getInputProps('document.number')}
								/>
								<TextInput
									key={form.key('document.supportNumber')}
									label={t(
										($) => $.people.properties.document.supportNumber.label
									)}
									disabled={
										!requiresSupportNumber(form.getValues().document.type)
									}
									readOnly={!booking.canBeModified}
									withAsterisk={requiresSupportNumber(
										form.getValues().document.type
									)}
									{...form.getInputProps('document.supportNumber')}
								/>
							</SimpleGrid>
						</Fieldset>
						<Group>
							<Button
								leftSection={<ScanIcon weight="bold" size={16} />}
								onClick={() => navigate('./scan')} // TODO: Cambia la ruta según tu configuración de React Router
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
			<Outlet />
		</>
	);
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
