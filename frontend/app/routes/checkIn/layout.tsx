import { Outlet, useOutletContext } from "react-router";

import {
	Box,
	Center,
	Divider,
	Group,
	Paper,
	Stack,
	Stepper,
	Text,
	Title
} from "@mantine/core";

import {
	SealCheckIcon,
	SignatureIcon,
	UserListIcon
} from "@phosphor-icons/react";
import { useTranslation } from "react-i18next";

import CheckInBookingDetailsSidebar from "~/component/checkIn/CheckInBookingDetails";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { BookingDtoResponse } from "~/@types/api";
import type { Route } from "./+types/layout";

interface ContextType {
	booking: BookingDtoResponse;
}

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const booking = await queryClient.query(
		queryFactory.accommodations.bookings.detail(accommodationId, bookingId)
	);

	if (!booking.canBeModified)
		throw Validators.throwValidationErrorResponse("Booking cannot be modified");

	return {
		booking
	};
}

const STEP_ROUTES = ["verify-booking", "input-guest-details", "send"] as const;

export default function CheckInRoute({
	loaderData: { booking }
}: Route.ComponentProps) {
	const { t } = useTranslation("routes", {
		keyPrefix: "checkIn"
	});

	const activeRoute = location.pathname
		.split("/")
		.at(-1) as (typeof STEP_ROUTES)[number];

	const activeStep = STEP_ROUTES.indexOf(activeRoute);
	const showBookingDetailsSidebar = activeRoute !== "verify-booking";

	return (
		<Center mih="100vh" bg="dark">
			<Paper withBorder shadow="sm">
				<Stack gap={0}>
					{/* Header */}
					<Box component="header" p={{ md: "xl", base: "lg" }}>
						<Title order={1} size="h2">
							{t(($) => $[activeRoute].title)}
						</Title>
						<Text c="dark" size="md">
							{t(($) => $[activeRoute].description)}
						</Text>
					</Box>

					<Divider />

					<Group gap={0} align="stretch">
						{/* Sidebar */}
						<Stack gap={0} visibleFrom="md">
							{/* Progress (nav) */}
							<Stack component="nav" p={{ md: "xl", base: "lg" }} gap="lg">
								<Title order={2} size="h5">
									{t(($) => $.progressSidebar.title)}
								</Title>
								<Stepper
									component="section"
									active={activeStep}
									orientation="vertical"
								>
									<Stepper.Step
										icon={<SealCheckIcon size={20} weight="bold" />}
										label={t(
											($) => $.progressSidebar.steps.verifyBooking.title
										)}
										description={t(
											($) => $.progressSidebar.steps.verifyBooking.description
										)}
									/>
									<Stepper.Step
										icon={<UserListIcon size={20} weight="bold" />}
										label={t(
											($) => $.progressSidebar.steps.inputGuestDetails.title
										)}
										description={t(
											($) =>
												$.progressSidebar.steps.inputGuestDetails.description
										)}
									/>
									<Stepper.Step
										icon={<SignatureIcon size={20} weight="bold" />}
										label={t(($) => $.progressSidebar.steps.send.title)}
										description={t(
											($) => $.progressSidebar.steps.send.description
										)}
									/>
								</Stepper>
							</Stack>
							<Divider hidden={!showBookingDetailsSidebar} />
							{/* Booking details (aside) */}
							<CheckInBookingDetailsSidebar
								booking={booking}
								component="aside"
								hidden={!showBookingDetailsSidebar}
							/>
						</Stack>

						<Divider visibleFrom="md" orientation="vertical" />

						<Box component="main" maw={800} p="lg" flex={1}>
							<Outlet context={{ booking } satisfies ContextType} />
						</Box>
					</Group>
				</Stack>
			</Paper>
		</Center>
	);
}

export function useCheckInRouteContext() {
	return useOutletContext<ContextType>();
}
