import { redirect } from "react-router";

import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { Route } from "./+types";

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId);

	const people = await queryClient.query(
		queryFactory.accommodations.bookings.people.list(accommodationId, bookingId)
	);

	const titular = people.at(0);
	return redirect(
		titular
			? `/accommodations/${accommodationId}/bookings/${bookingId}/people/${titular.id}`
			: `/accommodations/${accommodationId}/bookings/${bookingId}/people/new`
	);
}
