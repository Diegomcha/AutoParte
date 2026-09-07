import { useNavigate } from "react-router";

import PersonForm from "~/component/PersonForm";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { Route } from "./+types/new";

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const [booking, people] = await Promise.all([
		queryClient.query(
			queryFactory.accommodations.bookings.detail(accommodationId, bookingId)
		),
		queryClient.query(
			queryFactory.accommodations.bookings.people.list(
				accommodationId,
				bookingId
			)
		)
	]);

	if (!booking.canBeModified || booking.numberOfPeople === people.length)
		throw Validators.throwValidationErrorResponse(
			"Booking cannot be modified."
		);
}

export default function NewPerson({
	params: { accommodationId, bookingId }
}: Route.ComponentProps) {
	const navigate = useNavigate();

	return (
		<PersonForm
			accommodationId={accommodationId}
			bookingId={bookingId}
			handleCreatedPerson={(personId) => void navigate(`../${personId}`)}
		/>
	);
}
