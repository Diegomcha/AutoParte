import {
	Badge,
	Button,
	DataList,
	Divider,
	Fieldset,
	Group,
	MaskInput,
	Modal,
	NumberInput,
	Select,
	SimpleGrid,
	Stack,
	Text,
	TextInput,
	Timeline,
	Title,
} from '@mantine/core';
import { DateInput, DateTimePicker } from '@mantine/dates';
import { isNotEmpty, useForm } from '@mantine/form';
import {
	ArrowUUpLeftIcon,
	CaretLeftIcon,
	ClockIcon,
	FloppyDiskIcon,
	PulseIcon,
	StarIcon,
} from '@phosphor-icons/react';
import { useMutation } from '@tanstack/react-query';
import api, { queryClient, throwErrors } from '~/api';
import BooleanInputWithUndefined from '~/component/BooleanInputWithUndefined';
import CommunicationTimelineItem from '~/component/CommunicationTimelineItem';
import dayjs from 'dayjs';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import relativeTime from 'dayjs/plugin/relativeTime';
import { useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router';
import type { Route } from './+types/edit';
import type { BookingDtoRequest } from '~/@types/api';

dayjs.extend(relativeTime);
dayjs.extend(customParseFormat);

export async function clientLoader({
	params: { accommodationId, bookingId },
}: Route.ClientLoaderArgs) {
	return {
		booking: await queryClient.fetchQuery({
			queryKey: ['bookings', accommodationId, bookingId],
			queryFn: async () =>
				throwErrors(
					await api.GET('/api/accommodations/{accommodationId}/bookings/{id}', {
						params: { path: { accommodationId, id: bookingId } },
					})
				),
		}),
	};
}

// TODO: Finish. we are missing people
export default function BookingsPage({
	loaderData: { booking },
	params: { accommodationId, bookingId },
}: Route.ComponentProps) {
	const { t } = useTranslation();
	const navigate = useNavigate();

	const resetRef = useRef<() => void>(null);

	// Some are '' others null depending on how the mantine inputs behave... It's not ideal
	const form = useForm({
		initialValues: {
			date: [booking.startTime, booking.endTime],
			numberOfPeople: booking.numberOfPeople,
			payment: {
				type: booking.payment?.type ?? null,
				mean: booking.payment?.mean ?? '',
				holder: booking.payment?.holder ?? '',
				date: booking.payment?.date ?? null,
				expiryDate: booking.payment?.expiryDate
					? dayjs(booking.payment.expiryDate).format('MM / YY')
					: '',
			},
			numberOfRooms: booking.numberOfRooms ?? '',
			internetConnection: String(booking.internetConnection ?? undefined),
		},
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

					if (value && !dayjs(value, 'MM / YY').isValid())
						return t(
							($) => $.bookings.properties.payment.expiryDate.errors.invalid
						);
					if (
						value &&
						values.payment.date &&
						dayjs(value, 'MM / YY').isBefore(dayjs(values.payment.date))
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
			startTime: dayjs(values.date[0]).toISOString(),
			endTime: dayjs(values.date[1]).toISOString(),
			numberOfPeople: values.numberOfPeople,
			payment:
				values.payment.type == null
					? undefined
					: {
							type: values.payment.type,
							mean: values.payment.mean || undefined,
							holder: values.payment.holder || undefined,
							date: values.payment.date
								? dayjs(values.payment.date).toISOString()
								: undefined,
							expiryDate: values.payment.expiryDate
								? dayjs(values.payment.expiryDate, 'MM / YY').toISOString()
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
		mutationFn: async (values: BookingDtoRequest) =>
			throwErrors(
				await api.PUT('/api/accommodations/{accommodationId}/bookings/{id}', {
					params: {
						path: { accommodationId, id: bookingId },
					},
					body: values,
				})
			),
		onSuccess: async () => {
			await queryClient.invalidateQueries({
				queryKey: ['bookings'],
			});
		},
	});

	return (
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
					form.setInitialValues(form.values);
				})}
				onReset={(e) => {
					form.onReset(e);
					resetRef.current?.();
				}}
			>
				{/* Header */}
				<Group justify="space-between">
					<Button
						component={Link}
						to="/"
						leftSection={<CaretLeftIcon weight="bold" size={16} />}
					>
						Back
					</Button>
					<Title order={2} size="h3" fw="normal">
						{t(($) => $.bookings.view.title)}
					</Title>
					<Group>
						<Button
							type="reset"
							color="gray"
							leftSection={<ArrowUUpLeftIcon weight="bold" size={16} />}
							loading={isPending}
						>
							{t(($) => $.common.buttons.reset)}
						</Button>
						<Button
							type="submit"
							color="green"
							leftSection={<FloppyDiskIcon weight="bold" size={16} />}
							loading={isPending}
						>
							{t(($) => $.common.buttons.save)}
						</Button>
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
										{dayjs(booking.createdAt).format('LLL')}
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
										{dayjs(booking.updatedAt).format('LLL')}
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
									{...form.getInputProps('date')}
								/>
								<NumberInput
									key={form.key('numberOfPeople')}
									name="numberOfPeople"
									label={t(
										($) => $.bookings.properties.details.numberOfPeople.label
									)}
									withAsterisk
									min={1}
									{...form.getInputProps('numberOfPeople')}
								/>
								<BooleanInputWithUndefined
									key={form.key('internetConnection')}
									name="internetConnection"
									label={t(
										($) => $.bookings.properties.details.internetConnection
									)}
									{...form.getInputProps('internetConnection')}
								/>
								<NumberInput
									key={form.key('numberOfRooms')}
									name="numberOfRooms"
									label={t(($) => $.bookings.properties.details.numberOfRooms)}
									min={1}
									{...form.getInputProps('numberOfRooms')}
								/>
							</SimpleGrid>
						</Fieldset>
						<Fieldset legend={t(($) => $.bookings.properties.payment.title)}>
							<SimpleGrid cols={2} verticalSpacing="xs">
								<Select
									key={form.key('payment.type')}
									name="payment.type"
									label={t(($) => $.bookings.properties.payment.type.label)}
									data={Object.entries(
										t(($) => $.bookings.properties.payment.type.options, {
											returnObjects: true,
										})
									)
										.map(([value, label]) => ({ value, label }))
										.sort((a, b) => a.label.localeCompare(b.label))}
									clearable
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
											value: dayjs().format('YYYY-MM-DD'),
											label: t(($) => $.common.dates.today),
										},
										{
											value: dayjs(booking.startTime).format('YYYY-MM-DD'),
											label: t(($) => $.common.dates.checkInDate),
										},
										{
											value: dayjs(booking.endTime).format('YYYY-MM-DD'),
											label: t(($) => $.common.dates.checkOutDate),
										},
									]}
									disabled={form.values.payment.type == null}
									highlightToday
									{...form.getInputProps('payment.date')}
								/>
								<TextInput
									key={form.key('payment.mean')}
									name="payment.mean"
									label={t(($) => $.bookings.properties.payment.mean)}
									disabled={form.values.payment.type == null}
									{...form.getInputProps('payment.mean')}
								/>
								<TextInput
									key={form.key('payment.holder')}
									name="payment.holder"
									label={t(($) => $.bookings.properties.payment.holder)}
									disabled={form.values.payment.type == null}
									{...form.getInputProps('payment.holder')}
								/>
								<MaskInput
									key={form.key('payment.expiryDate')}
									name="payment.expiryDate"
									label={t(
										($) => $.bookings.properties.payment.expiryDate.label
									)}
									mask="99 / 99"
									placeholder={t(
										($) => $.bookings.properties.payment.expiryDate.placeholder
									)}
									disabled={form.values.payment.type !== 'CREDIT_CARD'}
									defaultValue={form.values.payment.expiryDate}
									onChangeRaw={(raw) => {
										form.setFieldValue('payment.expiryDate', raw, {
											forceUpdate: false,
										});
									}}
									error={form.errors['payment.expiryDate']}
									resetRef={resetRef}
								/>
							</SimpleGrid>
						</Fieldset>
					</Stack>
					<Fieldset legend={t(($) => $.bookings.view.communications.title)}>
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
								title={t(($) => $.bookings.view.communications.types.CREATED)}
							>
								<Text size="sm" c="dark">
									{dayjs(booking.createdAt).fromNow()}
								</Text>
							</Timeline.Item>
							{booking.communications.map((communication) => (
								<CommunicationTimelineItem
									key={communication.id}
									communication={communication}
								/>
							))}
						</Timeline>
					</Fieldset>
				</Group>
			</form>
		</Modal>
	);
}
