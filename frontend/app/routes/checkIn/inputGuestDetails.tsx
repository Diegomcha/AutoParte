import { useState } from "react";
import { useNavigate } from "react-router";

import { Stack } from "@mantine/core";

import { useSuspenseQuery } from "@tanstack/react-query";

import CheckInPersonSelector from "~/component/checkIn/CheckInPersonSelector";
import GuestDataInputSelector from "~/component/GuestDataInputSelector";
import PersonForm from "~/component/PersonForm";
import { queryClient, queryFactory } from "~/services/Api";

import { useCheckInRouteContext } from "./layout";

import type { Route } from "./+types/inputGuestDetails";

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	await queryClient.query(
		queryFactory.accommodations.bookings.people.list(accommodationId, bookingId)
	);
}

export default function CheckInVerifyBookingRoute({
	params: { accommodationId }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { booking } = useCheckInRouteContext();

	// const { t } = useTranslation("routes", {
	// 	keyPrefix: "checkIn.input-guest-details"
	// });

	const { data: people } = useSuspenseQuery(
		queryFactory.accommodations.bookings.people.list(
			accommodationId,
			booking.id
		)
	);

	const [activePersonIndex, setActivePersonIndex] = useState(0);
	const [defaultMode, setDefaultMode] = useState<"scanning" | "manual">();

	return !defaultMode ? (
		<GuestDataInputSelector onSelection={setDefaultMode} />
	) : (
		<Stack gap="lg">
			<CheckInPersonSelector
				activePersonIndex={activePersonIndex}
				setActivePersonIndex={setActivePersonIndex}
				allowNextStepsSelect={false}
				numberOfPeople={booking.numberOfPeople}
				people={people}
			/>
			<PersonForm
				key={activePersonIndex}
				accommodationId={accommodationId}
				bookingId={booking.id}
				person={people.at(activePersonIndex)}
				checkInMode
				defaultMode={defaultMode}
				onSubmit={() => {
					if (activePersonIndex < booking.numberOfPeople - 1)
						setActivePersonIndex((prevIndex) => prevIndex + 1);
					else void navigate(`../send`);
				}}
			/>
		</Stack>
	);
}
