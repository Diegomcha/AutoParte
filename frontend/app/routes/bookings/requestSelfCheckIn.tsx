import { useNavigate } from "react-router";

import {
	Alert,
	Button,
	Group,
	Modal,
	Space,
	Stack,
	Text,
	TextInput,
	useModalsStack
} from "@mantine/core";

import {
	LinkIcon,
	PaperPlaneTiltIcon,
	WarningIcon
} from "@phosphor-icons/react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import useStaticModalStackTransition from "~/hooks/useStaticModalStackTransition";
import { queryClient, queryFactory } from "~/services/Api";
import NotificationsService from "~/services/NotificationsService";
import TimeService from "~/services/TimeService";
import Validators from "~/services/Validators";

import type { Route } from "./+types/requestSelfCheckIn";

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const booking = await queryClient.query(
		queryFactory.accommodations.bookings.detail(accommodationId, bookingId)
	);

	if (!booking.canSelfCheckInBeRequested)
		throw Validators.throwValidationErrorResponse(
			"Booking is not in a state that allows requesting self-check-in."
		);

	return {
		isCheckInDay: TimeService(booking.startTime).isSame(TimeService(), "day")
	};
}

export default function RequestSelfCheckInForBooking({
	params: { accommodationId, bookingId },
	loaderData: { isCheckInDay }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { t } = useTranslation("routes", {
		keyPrefix: "bookings.requestSelfCheckIn"
	});
	const { t: tCommon } = useTranslation();

	const stack = useModalsStack(["request", "link"]);
	const { close } = useStaticModalStackTransition(
		stack,
		"request",
		() => void navigate("..")
	);

	const { mutate, isPending } = useMutation(
		queryFactory.accommodations.bookings.requestSelfCheckIn(
			accommodationId,
			bookingId
		)
	);

	return (
		<>
			<Modal
				{...stack.register("request")}
				onClose={close}
				title={t(($) => $.title)}
			>
				{!isCheckInDay && (
					<>
						<Alert color="yellow" icon={<WarningIcon weight="bold" />}>
							{t(($) => $.notCheckInDayWarning)}
						</Alert>
						<Space h="md" />
					</>
				)}
				{t(($) => $.description)}

				<Group justify="right" mt="md" gap="xs">
					<Button onClick={close} color="gray">
						{tCommon(($) => $.buttons.cancel)}
					</Button>
					<Button
						leftSection={<PaperPlaneTiltIcon weight="bold" />}
						color={t(($) => $.color)}
						loading={isPending}
						onClick={() => {
							mutate(undefined, {
								onSuccess: () => {
									stack.open("link");
								}
							});
						}}
					>
						{t(($) => $.button)}
					</Button>
				</Group>
			</Modal>
			<Modal
				{...stack.register("link")}
				onClose={close}
				title={t(($) => $.linkModal.title)}
			>
				<Stack>
					<Alert color="yellow" icon={<WarningIcon weight="bold" />}>
						{t(($) => $.linkModal.alert)}
					</Alert>
					{/* TODO: Cuando este el sistema de email habrá que cambiar esto */}
					<Text>{t(($) => $.linkModal.description)}</Text>
					<TextInput
						readOnly
						leftSectionPointerEvents="none"
						leftSection={<LinkIcon />}
						label={t(($) => $.linkModal.label)}
						value={`${window.location.origin}/check-in/${accommodationId}/${bookingId}`}
						onClick={(event) => {
							event.currentTarget.select();
							NotificationsService.success(t(($) => $.linkCopied));
							void navigator.clipboard.writeText(
								`${window.location.origin}/check-in/${accommodationId}/${bookingId}`
							);
						}}
					/>
				</Stack>
			</Modal>
		</>
	);
}
