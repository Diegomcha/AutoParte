import {
	ActionIcon,
	Badge,
	Button,
	DataList,
	Divider,
	Fieldset,
	Group,
	MaskInput,
	Modal,
	NumberInput,
	ScrollArea,
	Select,
	SimpleGrid,
	Stack,
	Text,
	TextInput,
	Timeline,
	Title,
	Tooltip,
} from '@mantine/core';
import { DateInput, DateTimePicker } from '@mantine/dates';
import { isNotEmpty, useForm } from '@mantine/form';
import {
	ArrowUUpLeftIcon,
	CaretLeftIcon,
	CheckCircleIcon,
	ClockIcon,
	EyeIcon,
	FloppyDiskIcon,
	PaperPlaneTiltIcon,
	PulseIcon,
	StarIcon,
	SuitcaseIcon,
	TrashIcon,
	UserListIcon,
	XIcon,
} from '@phosphor-icons/react';
import { useMutation, useSuspenseQuery } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import BooleanInputWithUndefined from '~/component/BooleanInputWithUndefined';
import CommunicationTimelineItem from '~/component/CommunicationTimelineItem';
import ComplexRequiredAsterisk from '~/component/ComplexRequiredLabel';
import TimeService from '~/services/TimeService';
import Validators from '~/services/Validators';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, Outlet, useNavigate, useOutletContext } from 'react-router';
import type { Route } from './+types/index';
import type { BookingDtoRequest, BookingDtoResponse } from '~/@types/api';

interface ContextType {
	booking: BookingDtoResponse;
}

export async function clientLoader({
	params: { accommodationId, bookingId },
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	await queryClient.prefetchQuery({
		queryKey: ['bookings', accommodationId, bookingId],
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/accommodations/{accommodationId}/bookings/{id}', {
					params: { path: { accommodationId, id: bookingId } },
				})
			),
	});
}

export default function BookingsPage({
	params: { accommodationId, bookingId },
}: Route.ComponentProps) {
	const { t } = useTranslation();
	const navigate = useNavigate();

	const { data: booking } = useSuspenseQuery({
		queryKey: ['bookings', accommodationId, bookingId],
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/accommodations/{accommodationId}/bookings/{id}', {
					params: { path: { accommodationId, id: bookingId } },
				})
			),
	});

	// MaskInput requires a rerrender to reset the input value
	const [maskKey, setMaskKey] = useState(false);

	function resetForm() {
		form.reset();
		setMaskKey((prev) => !prev);
	}

	// Some are '' others null depending on how the mantine inputs behave... It's not ideal
	const form = useForm({
		initialValues: getInitialValues(booking),
		validate: {
			date: (value) =>
				(value[0] == null || value[1] == null) &&
				t(($) => $.bookings.properties.details.date.errors.undefined),
			numberOfPeople: isNotEmpty(
				t(($) => $.bookings.properties.details.numberOfPeople.errors.undefined)
			),
			payment: {
				expiryDate: (value, values) => {
					if (values.payment.type !== 'CREDIT_CARD') return null;

					if (value && !TimeService(value, 'MMYY').isValid())
						return t(
							($) => $.bookings.properties.payment.expiryDate.errors.invalid
						);
					if (
						value &&
						values.payment.date &&
						TimeService(value, 'MMYY').isBefore(
							TimeService(values.payment.date)
						)
					)
						return t(
							($) =>
								$.bookings.properties.payment.expiryDate.errors
									.beforePaymentDate
						);
				},
			},
		},
		transformValues: (values) => ({
			startTime: TimeService(values.date[0]).toISOString(),
			endTime: TimeService(values.date[1]).toISOString(),
			numberOfPeople: values.numberOfPeople,
			payment:
				values.payment.type == null
					? undefined
					: {
							type: values.payment.type,
							mean: values.payment.mean || undefined,
							holder: values.payment.holder || undefined,
							date: values.payment.date
								? TimeService(values.payment.date).toISOString()
								: undefined,
							expiryDate: values.payment.expiryDate
								? TimeService(values.payment.expiryDate, 'MMYY').toISOString()
								: undefined,
						},
			numberOfRooms: values.numberOfRooms
				? Number(values.numberOfRooms)
				: undefined,
			internetConnection:
				values.internetConnection === 'undefined'
					? undefined
					: values.internetConnection === 'true',
		}),
	});

	const { mutate, isPending } = useMutation({
		throwOnError: true,
		mutationFn: async (values: BookingDtoRequest) => {
			const response = await api.PUT(
				'/api/accommodations/{accommodationId}/bookings/{id}',
				{
					params: {
						path: { accommodationId, id: bookingId },
					},
					body: values,
				}
			);

			// Handle more people info. than slots (409)
			if (!response.response.ok && response.response.status === 409) {
				form.setFieldError(
					'numberOfPeople',
					t(($) => $.bookings.properties.details.numberOfPeople.errors.tooFew)
				);
				return false;
			}

			throwErrors(response);
			return true;
		},
		onSuccess: async (success) => {
			if (success) {
				await queryClient.invalidateQueries({
					queryKey: ['bookings'],
				});
			}
		},
	});

	useEffect(() => {
		form.clearFieldError('payment.expiryDate');
	}, [form.values.payment.type]);

	useEffect(() => {
		form.setInitialValues(getInitialValues(booking));
		resetForm();
	}, [booking]);

	return (
		<>
			<Modal
				opened
				withCloseButton={false}
				onClose={() => {
					void navigate('/');
				}}
				size="auto"
			>
				<form
					onSubmit={form.onSubmit((data) => {
						mutate(data);
					})}
					onReset={() => {
						resetForm();
					}}
				>
					{/* Header */}
					<Group justify="space-between" align="center">
						<Button
							component={Link}
							to="/"
							leftSection={<CaretLeftIcon weight="bold" size={16} />}
						>
							{t(($) => $.common.buttons.back)}
						</Button>
						<Title order={2} size="h3" fw="normal">
							{t(($) => $.bookings.edit.title)}
						</Title>
						<Group>
							{booking.canBeModified ? (
								<>
									<Button
										type="reset"
										color="gray"
										leftSection={<ArrowUUpLeftIcon weight="bold" size={16} />}
										loading={isPending}
										hidden={!form.isDirty() || !booking.canBeModified}
									>
										{t(($) => $.common.buttons.reset)}
									</Button>
									<Button
										type="submit"
										color="green"
										leftSection={<FloppyDiskIcon weight="bold" size={16} />}
										loading={isPending}
										disabled={!form.isDirty()}
										hidden={!booking.canBeModified}
									>
										{t(($) => $.common.buttons.save)}
									</Button>
								</>
							) : (
								<Tooltip label={t(($) => $.bookings.edit.readOnly.description)}>
									<Group c="dark" p="xs" gap="xs">
										<EyeIcon weight="bold" size={18} />
										<Text size="sm">
											{t(($) => $.bookings.edit.readOnly.title)}
										</Text>
									</Group>
								</Tooltip>
							)}
						</Group>
					</Group>
					<Divider my="sm" />
					{/* Form */}
					<Group align="stretch">
						<Stack>
							<Fieldset legend={t(($) => $.bookings.properties.details.title)}>
								<DataList
									orientation="vertical"
									style={{
										display: 'flex',
										flexDirection: 'row',
										gap: '2rem',
										justifyContent: 'space-between',
									}}
								>
									<DataList.Item>
										<DataList.ItemLabel>
											<Group gap={4}>
												<PulseIcon />
												{t(($) => $.bookings.properties.details.status.label)}
											</Group>
										</DataList.ItemLabel>
										<DataList.ItemValue>
											<Badge
												color={t(
													($) =>
														$.bookings.properties.details.status.states[
															booking.status
														].color
												)}
											>
												{t(
													($) =>
														$.bookings.properties.details.status.states[
															booking.status
														].label
												)}
											</Badge>
										</DataList.ItemValue>
									</DataList.Item>
									<DataList.Item>
										<DataList.ItemLabel>
											<Group gap={4}>
												<ClockIcon />
												{t(($) => $.common.properties.createdAt)}
											</Group>
										</DataList.ItemLabel>
										<DataList.ItemValue>
											{TimeService(booking.createdAt).format('LLL')}
										</DataList.ItemValue>
									</DataList.Item>
									<DataList.Item>
										<DataList.ItemLabel>
											<Group gap={4}>
												<ClockIcon />
												{t(($) => $.common.properties.updatedAt)}
											</Group>
										</DataList.ItemLabel>
										<DataList.ItemValue>
											{TimeService(booking.updatedAt).format('LLL')}
										</DataList.ItemValue>
									</DataList.Item>
								</DataList>
								<Divider my="md" />
								<SimpleGrid cols={2} verticalSpacing="sm">
									<DateTimePicker
										miw="16.5rem"
										key={form.key('date')}
										name="date"
										label={t(($) => $.bookings.properties.details.date.label)}
										type="range"
										allowSingleDateInRange={false}
										highlightToday
										withAsterisk
										readOnly={!booking.canBeModified}
										{...form.getInputProps('date')}
									/>
									<Group align="end" gap="xs">
										<NumberInput
											key={form.key('numberOfPeople')}
											name="numberOfPeople"
											label={t(
												($) =>
													$.bookings.properties.details.numberOfPeople.label
											)}
											withAsterisk
											min={1}
											readOnly={!booking.canBeModified}
											className="grow"
											rightSection={
												<Tooltip
													label={t(($) =>
														form.isDirty('numberOfPeople')
															? $.bookings.people.buttonDisabled
															: $.bookings.people.button
													)}
												>
													<ActionIcon
														size="input-xs"
														variant="default"
														mr={'xs'}
														component={
															form.isDirty('numberOfPeople') ? undefined : Link
														}
														disabled={form.isDirty('numberOfPeople')}
														to={`/accommodations/${accommodationId}/bookings/${bookingId}/people`}
													>
														<UserListIcon />
													</ActionIcon>
												</Tooltip>
											}
											{...form.getInputProps('numberOfPeople')}
										/>
									</Group>
									<BooleanInputWithUndefined
										key={form.key('internetConnection')}
										name="internetConnection"
										label={t(
											($) => $.bookings.properties.details.internetConnection
										)}
										readOnly={!booking.canBeModified}
										{...form.getInputProps('internetConnection')}
									/>
									<NumberInput
										key={form.key('numberOfRooms')}
										name="numberOfRooms"
										label={t(
											($) => $.bookings.properties.details.numberOfRooms
										)}
										min={1}
										readOnly={!booking.canBeModified}
										{...form.getInputProps('numberOfRooms')}
									/>
								</SimpleGrid>
							</Fieldset>
							<Fieldset legend={t(($) => $.bookings.properties.payment.title)}>
								<SimpleGrid cols={2} verticalSpacing="xs">
									<Select
										key={form.key('payment.type')}
										name="payment.type"
										label={
											<>
												{t(($) => $.bookings.properties.payment.type.label)}
												<ComplexRequiredAsterisk action="confirm" />
											</>
										}
										data={Object.entries(
											t(($) => $.bookings.properties.payment.type.options, {
												returnObjects: true,
											})
										)
											.map(([value, label]) => ({ value, label }))
											.sort((a, b) => a.label.localeCompare(b.label))}
										clearable
										searchable
										readOnly={!booking.canBeModified}
										{...form.getInputProps('payment.type')}
									/>
									<DateInput
										key={form.key('payment.date')}
										name="payment.date"
										label={t(($) => $.bookings.properties.payment.date.label)}
										valueFormat={t(
											($) => $.bookings.properties.payment.date.format
										)}
										clearable
										presets={[
											{
												value: TimeService().format('YYYY-MM-DD'),
												label: t(($) => $.common.dates.today),
											},
											{
												value: TimeService(booking.startTime).format(
													'YYYY-MM-DD'
												),
												label: t(($) => $.common.dates.checkInDate),
											},
											{
												value: TimeService(booking.endTime).format(
													'YYYY-MM-DD'
												),
												label: t(($) => $.common.dates.checkOutDate),
											},
										]}
										highlightToday
										disabled={form.values.payment.type == null}
										readOnly={!booking.canBeModified}
										{...form.getInputProps('payment.date')}
									/>
									<TextInput
										key={form.key('payment.mean')}
										name="payment.mean"
										label={t(($) => $.bookings.properties.payment.mean)}
										disabled={form.values.payment.type == null}
										readOnly={!booking.canBeModified}
										{...form.getInputProps('payment.mean')}
									/>
									<TextInput
										key={form.key('payment.holder')}
										name="payment.holder"
										label={t(($) => $.bookings.properties.payment.holder)}
										disabled={form.values.payment.type == null}
										readOnly={!booking.canBeModified}
										{...form.getInputProps('payment.holder')}
									/>
									<MaskInput
										key={form.key('payment.expiryDate') + maskKey.toString()}
										name="payment.expiryDate"
										label={t(
											($) => $.bookings.properties.payment.expiryDate.label
										)}
										mask="99 / 99"
										placeholder={t(
											($) =>
												$.bookings.properties.payment.expiryDate.placeholder
										)}
										disabled={form.values.payment.type !== 'CREDIT_CARD'}
										defaultValue={form.values.payment.expiryDate}
										onChangeRaw={(raw) => {
											form.setFieldValue('payment.expiryDate', raw, {
												forceUpdate: false,
											});
										}}
										error={form.errors['payment.expiryDate']}
										readOnly={!booking.canBeModified}
									/>
								</SimpleGrid>
							</Fieldset>
						</Stack>
						{/* Right panel */}
						<Stack>
							{/* Communications log */}
							<Fieldset
								legend={t(($) => $.bookings.properties.communications.title)}
								className="grow"
								pr={8}
							>
								<ScrollArea.Autosize h="0" mih="100%" offsetScrollbars>
									<Timeline
										bulletSize={24}
										lineWidth={2}
										active={
											['PENDING', 'SENT', 'PENDING_VOIDED'].includes(
												booking.communications.at(-1)?.status ?? ''
											)
												? booking.communications.length - 1
												: booking.communications.length
										}
									>
										{/* Creation date */}
										<Timeline.Item
											bullet={<StarIcon weight="fill" />}
											title={t(
												($) =>
													$.bookings.properties.communications.types.CREATED
											)}
										>
											<Text size="sm" c="dark">
												{TimeService(booking.createdAt).fromNow()}
											</Text>
										</Timeline.Item>
										{booking.communications.map((communication) => (
											<CommunicationTimelineItem
												key={communication.id}
												communication={communication}
											/>
										))}
									</Timeline>
								</ScrollArea.Autosize>
							</Fieldset>
							{/* Action buttons */}
							<Stack
								gap="xs"
								hidden={
									booking.status !== 'CONFIRMATION_READY' &&
									booking.status !== 'CHECK_IN_READY' &&
									(booking.selfCheckInRequested || !booking.canBeModified) &&
									['PENDING_CANCELLATION', 'CANCELLED'].includes(booking.status)
								}
							>
								<Button
									component={!form.isDirty() ? Link : undefined}
									to={`/accommodations/${accommodationId}/bookings/${bookingId}/confirm`}
									leftSection={<CheckCircleIcon weight="bold" />}
									color={t(($) => $.bookings.confirm.color)}
									hidden={booking.status !== 'CONFIRMATION_READY'}
									disabled={form.isDirty()}
								>
									{t(($) => $.bookings.confirm.button)}
								</Button>
								<Button
									component={!form.isDirty() ? Link : undefined}
									to={`/accommodations/${accommodationId}/bookings/${bookingId}/check-in`}
									leftSection={<SuitcaseIcon weight="bold" />}
									color={t(($) => $.bookings.checkIn.color)}
									hidden={booking.status !== 'CHECK_IN_READY'}
									disabled={form.isDirty()}
								>
									{t(($) => $.bookings.checkIn.button)}
								</Button>
								<Button
									component={!form.isDirty() ? Link : undefined}
									to={`/accommodations/${accommodationId}/bookings/${bookingId}/request-self-check-in`}
									leftSection={<PaperPlaneTiltIcon weight="bold" />}
									color={t(($) => $.bookings.requestSelfCheckIn.color)}
									hidden={
										booking.selfCheckInRequested || !booking.canBeModified
									}
									disabled={form.isDirty()}
								>
									{t(($) => $.bookings.requestSelfCheckIn.button)}
								</Button>
								<Divider
									hidden={
										(booking.status !== 'CONFIRMATION_READY' &&
											booking.status !== 'CHECK_IN_READY' &&
											(booking.selfCheckInRequested ||
												!booking.canBeModified)) ||
										['PENDING_CANCELLATION', 'CANCELLED'].includes(
											booking.status
										)
									}
								/>
								<Button
									component={!form.isDirty() ? Link : undefined}
									to={`/accommodations/${accommodationId}/bookings/${bookingId}/${booking.canBeDeleted ? 'delete' : 'cancel'}`}
									leftSection={
										booking.canBeDeleted ? (
											<TrashIcon weight="bold" />
										) : (
											<XIcon weight="bold" />
										)
									}
									color={t(($) =>
										booking.canBeDeleted
											? $.bookings.delete.color
											: $.bookings.cancel.color
									)}
									disabled={form.isDirty()}
									hidden={['PENDING_CANCELLATION', 'CANCELLED'].includes(
										booking.status
									)}
								>
									{booking.canBeDeleted
										? t(($) => $.bookings.delete.button)
										: t(($) => $.bookings.cancel.button)}
								</Button>
							</Stack>
						</Stack>
					</Group>
				</form>
			</Modal>
			<Outlet context={{ booking } satisfies ContextType} />
		</>
	);
}

export function useBooking() {
	return useOutletContext<ContextType>();
}

function getInitialValues(booking: BookingDtoResponse) {
	return {
		date: [booking.startTime, booking.endTime],
		numberOfPeople: booking.numberOfPeople,
		payment: {
			type: booking.payment?.type ?? null,
			mean: booking.payment?.mean ?? '',
			holder: booking.payment?.holder ?? '',
			date: booking.payment?.date ?? null,
			expiryDate: booking.payment?.expiryDate
				? TimeService(booking.payment.expiryDate).format('MM / YY')
				: '',
		},
		numberOfRooms: booking.numberOfRooms ?? '',
		internetConnection: String(booking.internetConnection ?? undefined),
	};
}
