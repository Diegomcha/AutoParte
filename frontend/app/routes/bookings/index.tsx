import { useState } from "react";
import { Link, Outlet, useNavigate, useOutletContext } from "react-router";

import {
	ActionIcon,
	Box,
	Button,
	DataList,
	Divider,
	Fieldset,
	Group,
	MaskInput,
	Menu,
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
	Tooltip
} from "@mantine/core";
import { DateInput, DateTimePicker } from "@mantine/dates";
import { isNotEmpty, useForm } from "@mantine/form";

import {
	ArrowUUpLeftIcon,
	CaretLeftIcon,
	ChatSlashIcon,
	CheckCircleIcon,
	ClockIcon,
	ExportIcon,
	EyeIcon,
	FileCsvIcon,
	FilePdfIcon,
	FloppyDiskIcon,
	LinkIcon,
	PaperPlaneTiltIcon,
	PulseIcon,
	StarIcon,
	SuitcaseIcon,
	TrashIcon,
	UserListIcon,
	XIcon
} from "@phosphor-icons/react";
import { useMutation, useSuspenseQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import BookingStatusBadge from "~/component/bookings/BookingStatusBadge";
import BooleanInputWithUndefined from "~/component/BooleanInputWithUndefined";
import CommunicationTimelineItem from "~/component/CommunicationTimelineItem";
import ComplexRequiredAsterisk from "~/component/ComplexRequiredLabel";
import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import NotificationsService from "~/services/NotificationsService";
import TimeService from "~/services/TimeService";
import Validators from "~/services/Validators";

import type { BookingDtoRequest, BookingDtoResponse } from "~/@types/api";
import type { Route } from "./+types/index";

interface ContextType {
	booking: BookingDtoResponse;
}

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	await queryClient.query(
		queryFactory.accommodations.bookings.detail(accommodationId, bookingId)
	);
}

export default function BookingsPage({
	params: { accommodationId, bookingId }
}: Route.ComponentProps) {
	const { t } = useTranslation("routes", {
		keyPrefix: "bookings"
	});
	const { t: tCommon } = useTranslation();
	const { t: tBooking } = useTranslation("entities", {
		keyPrefix: "booking"
	});
	const { t: tEntity } = useTranslation("entities", {
		keyPrefix: "common"
	});
	const navigate = useNavigate();

	const { opened, close } = useStaticModalTransition(() => void navigate(`/`));

	const { data: booking } = useSuspenseQuery(
		queryFactory.accommodations.bookings.detail(accommodationId, bookingId)
	);

	// MaskInput requires a rerrender to reset the input value
	const [maskKey, setMaskKey] = useState(false);

	function resetForm() {
		form.reset();
		setMaskKey((prev) => !prev);
	}

	// Some are '' others null depending on how the mantine inputs behave... It's not ideal
	const form = useForm({
		mode: "uncontrolled",
		initialValues: {
			date: [booking.startTime, booking.endTime],
			numberOfPeople: booking.numberOfPeople,
			payment: {
				type: booking.payment?.type ?? null,
				mean: booking.payment?.mean ?? "",
				holder: booking.payment?.holder ?? "",
				date: booking.payment?.date ?? null,
				expiryDate: booking.payment?.expiryDate
					? TimeService(booking.payment.expiryDate).format("MM / YY")
					: ""
			},
			numberOfRooms: booking.numberOfRooms ?? "",
			internetConnection: String(booking.internetConnection ?? undefined)
		},
		validate: {
			date: (value) =>
				(value[0] == null || value[1] == null) &&
				tBooking(($) => $.details.date.errors.undefined),
			numberOfPeople: isNotEmpty(
				tBooking(($) => $.details.numberOfPeople.errors.undefined)
			),
			payment: {
				type: (value) => {
					if (booking.selfCheckInRequested && value == null)
						return tBooking(
							($) => $.payment.type.errors.undefinedWhenSelfCheckInRequested
						);
				},
				expiryDate: (value, values) => {
					if (values.payment.type !== "CREDIT_CARD") return null;

					if (value && !TimeService(value, "MMYY").isValid())
						return tBooking(($) => $.payment.expiryDate.errors.invalid);
					if (
						value &&
						values.payment.date &&
						TimeService(value, "MMYY").isBefore(
							TimeService(values.payment.date)
						)
					)
						return tBooking(
							($) => $.payment.expiryDate.errors.beforePaymentDate
						);
				}
			}
		},
		transformValues: (values) =>
			({
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
								expiryDate:
									values.payment.type === "CREDIT_CARD" &&
									values.payment.expiryDate
										? TimeService(
												values.payment.expiryDate,
												"MMYY"
											).toISOString()
										: undefined
							},
				numberOfRooms: values.numberOfRooms
					? Number(values.numberOfRooms)
					: undefined,
				internetConnection:
					values.internetConnection === "undefined"
						? undefined
						: values.internetConnection === "true"
			}) satisfies BookingDtoRequest,
		onValuesChange: (values, previous) => {
			if (values.payment.type !== previous.payment.type) {
				form.clearFieldError("payment.expiryDate");
			}
		}
	});

	const watchedFormValues = {
		payment: {
			type: form.useWatchValue("payment.type"),
			expiryDate: form.useWatchValue("payment.expiryDate")
		}
	};

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.bookings.update(accommodationId, bookingId)
	);

	return (
		<>
			<Modal
				opened={opened}
				withCloseButton={false}
				onClose={close}
				size="auto"
			>
				<form
					key={booking.id}
					onSubmit={form.onSubmit((data) => {
						mutate(data, {
							onSuccess: (success) => {
								// Handle more people info than slots (409)
								if (!success)
									form.setFieldError(
										"numberOfPeople",
										tBooking(($) => $.details.numberOfPeople.errors.tooFew)
									);
								// Handle success
								else form.resetDirty();
							}
						});
					})}
					onReset={() => {
						resetForm();
					}}
				>
					{/* Header */}
					<Group justify="space-between" align="center">
						<Button
							onClick={close}
							leftSection={<CaretLeftIcon weight="bold" size={16} />}
						>
							{tCommon(($) => $.buttons.back)}
						</Button>
						<Title order={2} size="h3" fw="normal">
							{t(($) => $.index.title)}
						</Title>
						<Group>
							{booking.canBeModified ? (
								<>
									{!form.isDirty() ? (
										<Menu width={120}>
											<Menu.Target>
												<Button
													leftSection={<ExportIcon weight="bold" size={16} />}
													color="grape"
													loading={isPending}
												>
													{tCommon(($) => $.buttons.export)}
												</Button>
											</Menu.Target>
											<Menu.Dropdown>
												<Menu.Label>Formato</Menu.Label>
												<Menu.Item
													// component={PDFDownloadLink}
													// document={<BookingPDF />}
													// fileName={`booking-${booking.id}.pdf`}
													leftSection={<FilePdfIcon weight="bold" size={16} />}
												>
													PDF
												</Menu.Item>
												<Menu.Item
													leftSection={<FileCsvIcon weight="bold" size={16} />}
												>
													CSV
												</Menu.Item>
											</Menu.Dropdown>
										</Menu>
									) : (
										<Button
											type="reset"
											color="gray"
											leftSection={<ArrowUUpLeftIcon weight="bold" size={16} />}
											loading={isPending}
										>
											{tCommon(($) => $.buttons.reset)}
										</Button>
									)}
									<Button
										type="submit"
										color="green"
										leftSection={<FloppyDiskIcon weight="bold" size={16} />}
										disabled={!form.isDirty()}
										loading={isPending}
									>
										{tCommon(($) => $.buttons.save)}
									</Button>
								</>
							) : (
								<Tooltip label={t(($) => $.index.readOnly.description)}>
									<Group c="dark" p="xs" gap="xs">
										<EyeIcon weight="bold" size={18} />
										<Text size="sm">{t(($) => $.index.readOnly.title)}</Text>
									</Group>
								</Tooltip>
							)}
						</Group>
					</Group>
					<Divider my="sm" />
					{/* Form */}
					<Group align="stretch">
						<Stack>
							<Fieldset legend={tBooking(($) => $.details.title)}>
								<DataList
									orientation="vertical"
									style={{
										display: "flex",
										flexDirection: "row",
										gap: "2rem",
										justifyContent: "space-between"
									}}
								>
									<DataList.Item>
										<DataList.ItemLabel>
											<Group gap={4}>
												<PulseIcon />
												{tBooking(($) => $.details.status.label)}
											</Group>
										</DataList.ItemLabel>
										<DataList.ItemValue>
											<BookingStatusBadge status={booking.status} />
										</DataList.ItemValue>
									</DataList.Item>
									<DataList.Item>
										<DataList.ItemLabel>
											<Group gap={4}>
												<ClockIcon />
												{tEntity(($) => $.createdAt.label)}
											</Group>
										</DataList.ItemLabel>
										<DataList.ItemValue>
											{TimeService(booking.createdAt).format("LLL")}
										</DataList.ItemValue>
									</DataList.Item>
									<DataList.Item>
										<DataList.ItemLabel>
											<Group gap={4}>
												<ClockIcon />
												{tEntity(($) => $.updatedAt.label)}
											</Group>
										</DataList.ItemLabel>
										<DataList.ItemValue>
											{TimeService(booking.updatedAt).format("LLL")}
										</DataList.ItemValue>
									</DataList.Item>
								</DataList>
								<Divider my="md" />
								<SimpleGrid cols={2} verticalSpacing="sm">
									<DateTimePicker
										miw="16.5rem"
										key={form.key("date")}
										name="date"
										label={tBooking(($) => $.details.date.label)}
										type="range"
										allowSingleDateInRange={false}
										highlightToday
										withAsterisk
										readOnly={!booking.canBeModified}
										{...form.getInputProps("date")}
									/>
									<NumberInput
										key={form.key("numberOfPeople")}
										name="numberOfPeople"
										label={tBooking(($) => $.details.numberOfPeople.label)}
										withAsterisk
										min={1}
										readOnly={!booking.canBeModified}
										rightSection={
											<Tooltip
												label={t(($) =>
													form.isDirty("numberOfPeople")
														? $.index.people.buttonDisabled
														: $.index.people.button
												)}
											>
												<ActionIcon
													size="input-xs"
													variant="default"
													mr={"xs"}
													component={
														form.isDirty("numberOfPeople") ? undefined : Link
													}
													disabled={form.isDirty("numberOfPeople")}
													to={`/accommodations/${accommodationId}/bookings/${bookingId}/people`}
												>
													<UserListIcon />
												</ActionIcon>
											</Tooltip>
										}
										{...form.getInputProps("numberOfPeople")}
									/>
									<BooleanInputWithUndefined
										key={form.key("internetConnection")}
										name="internetConnection"
										label={tBooking(($) => $.details.internetConnection.label)}
										readOnly={!booking.canBeModified}
										{...form.getInputProps("internetConnection")}
									/>
									<NumberInput
										key={form.key("numberOfRooms")}
										name="numberOfRooms"
										label={tBooking(($) => $.details.numberOfRooms.label)}
										min={1}
										readOnly={!booking.canBeModified}
										{...form.getInputProps("numberOfRooms")}
									/>
								</SimpleGrid>
							</Fieldset>
							<Fieldset legend={tBooking(($) => $.payment.title)}>
								<SimpleGrid cols={2} verticalSpacing="xs">
									<Select
										key={form.key("payment.type")}
										name="payment.type"
										label={
											<>
												{tBooking(($) => $.payment.type.label)}
												<ComplexRequiredAsterisk action="confirm" />
											</>
										}
										data={Object.entries(
											tBooking(($) => $.payment.type.options, {
												returnObjects: true
											})
										)
											.map(([value, label]) => ({ value, label }))
											.sort((a, b) => a.label.localeCompare(b.label))}
										clearable
										searchable
										readOnly={!booking.canBeModified}
										{...form.getInputProps("payment.type")}
									/>
									<DateInput
										key={form.key("payment.date")}
										name="payment.date"
										label={tBooking(($) => $.payment.date.label)}
										valueFormat={tBooking(($) => $.payment.date.format)}
										clearable
										presets={[
											{
												value: TimeService().format("YYYY-MM-DD"),
												label: tCommon(($) => $.dates.today)
											},
											{
												value: TimeService(booking.startTime).format(
													"YYYY-MM-DD"
												),
												label: tCommon(($) => $.dates.checkInDate)
											},
											{
												value: TimeService(booking.endTime).format(
													"YYYY-MM-DD"
												),
												label: tCommon(($) => $.dates.checkOutDate)
											}
										]}
										highlightToday
										disabled={watchedFormValues.payment.type == null}
										readOnly={!booking.canBeModified}
										{...form.getInputProps("payment.date")}
									/>
									<TextInput
										key={form.key("payment.mean")}
										name="payment.mean"
										label={tBooking(($) => $.payment.mean)}
										disabled={watchedFormValues.payment.type == null}
										readOnly={!booking.canBeModified}
										{...form.getInputProps("payment.mean")}
									/>
									<TextInput
										key={form.key("payment.holder")}
										name="payment.holder"
										label={tBooking(($) => $.payment.holder)}
										disabled={watchedFormValues.payment.type == null}
										readOnly={!booking.canBeModified}
										{...form.getInputProps("payment.holder")}
									/>
									<MaskInput
										key={form.key("payment.expiryDate") + maskKey.toString()}
										name="payment.expiryDate"
										label={tBooking(($) => $.payment.expiryDate.label)}
										mask="99 / 99"
										placeholder={tBooking(
											($) => $.payment.expiryDate.placeholder
										)}
										disabled={watchedFormValues.payment.type !== "CREDIT_CARD"}
										defaultValue={watchedFormValues.payment.expiryDate}
										onChangeRaw={(raw) => {
											form.setFieldValue("payment.expiryDate", raw, {
												forceUpdate: false
											});
										}}
										error={form.errors["payment.expiryDate"]}
										readOnly={!booking.canBeModified}
									/>
								</SimpleGrid>
							</Fieldset>
						</Stack>
						{/* Right panel */}
						<Stack>
							{/* Communications log */}
							<Fieldset
								legend={tBooking(($) => $.communications.title)}
								flex={1}
								pr={8}
							>
								<ScrollArea.Autosize h="0" mih="100%" offsetScrollbars>
									<Timeline
										bulletSize={24}
										lineWidth={2}
										active={
											["PENDING", "SENT", "PENDING_VOIDED"].includes(
												booking.communications.at(-1)?.status ?? ""
											)
												? booking.communications.length - 1
												: booking.communications.length
										}
									>
										{/* Creation date */}
										<Timeline.Item
											bullet={<StarIcon weight="fill" />}
											title={tBooking(($) => $.communications.types.CREATED)}
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
									!booking.canBeConfirmed &&
									!booking.canBeCheckedIn &&
									!booking.canSelfCheckInBeRequested &&
									!booking.selfCheckInRequested &&
									!booking.canBeDeleted &&
									!booking.canBeCancelled
								}
							>
								<Button
									component={!form.isDirty() ? Link : undefined}
									to={`/accommodations/${accommodationId}/bookings/${bookingId}/confirm`}
									leftSection={<CheckCircleIcon weight="bold" />}
									color={t(($) => $.confirm.color)}
									hidden={!booking.canBeConfirmed}
									disabled={form.isDirty()}
									loading={isPending}
								>
									{t(($) => $.confirm.button)}
								</Button>
								<Button
									component={!form.isDirty() ? Link : undefined}
									to={`/accommodations/${accommodationId}/bookings/${bookingId}/check-in`}
									leftSection={<SuitcaseIcon weight="bold" />}
									color={t(($) => $.checkIn.color)}
									hidden={!booking.canBeCheckedIn}
									disabled={form.isDirty()}
									loading={isPending}
								>
									{t(($) => $.checkIn.button)}
								</Button>
								<Button
									component={!form.isDirty() ? Link : undefined}
									to={`/accommodations/${accommodationId}/bookings/${bookingId}/request-self-check-in`}
									leftSection={<PaperPlaneTiltIcon weight="bold" />}
									color={t(($) => $.requestSelfCheckIn.color)}
									hidden={!booking.canSelfCheckInBeRequested}
									disabled={form.isDirty()}
									loading={isPending}
								>
									{t(($) => $.requestSelfCheckIn.button)}
								</Button>
								<Box hidden={!booking.selfCheckInRequested}>
									<Button.Group>
										<Button
											color={t(($) => $.requestSelfCheckIn.color)}
											disabled={form.isDirty()}
											loading={isPending}
											onClick={() => {
												NotificationsService.success(
													t(($) => $.requestSelfCheckIn.linkCopied)
												);
												void navigator.clipboard.writeText(
													`${window.location.origin}/check-in/${accommodationId}/${bookingId}`
												);
											}}
										>
											<LinkIcon weight="bold" />
										</Button>
										<Button
											component={!form.isDirty() ? Link : undefined}
											to={`/accommodations/${accommodationId}/bookings/${bookingId}/cancel-self-check-in`}
											leftSection={<ChatSlashIcon weight="bold" />}
											color={t(($) => $.cancelSelfCheckIn.color)}
											disabled={form.isDirty()}
											loading={isPending}
											flex={1}
										>
											{t(($) => $.cancelSelfCheckIn.button)}
										</Button>
									</Button.Group>
								</Box>

								<Divider
									hidden={
										(!booking.canBeConfirmed &&
											!booking.canBeCheckedIn &&
											!booking.canSelfCheckInBeRequested &&
											!booking.selfCheckInRequested) ||
										(!booking.canBeDeleted && !booking.canBeCancelled)
									}
								/>
								<Button
									component={!form.isDirty() ? Link : undefined}
									to={`/accommodations/${accommodationId}/bookings/${bookingId}/delete`}
									leftSection={<TrashIcon weight="bold" />}
									color={t(($) => $.delete.color)}
									disabled={form.isDirty()}
									hidden={!booking.canBeDeleted}
									loading={isPending}
								>
									{t(($) => $.delete.button)}
								</Button>
								<Button
									component={!form.isDirty() ? Link : undefined}
									to={`/accommodations/${accommodationId}/bookings/${bookingId}/cancel`}
									leftSection={<XIcon weight="bold" />}
									color={t(($) => $.cancel.color)}
									disabled={form.isDirty()}
									hidden={!booking.canBeCancelled}
									loading={isPending}
								>
									{t(($) => $.cancel.button)}
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
	return useOutletContext<ContextType>().booking;
}
