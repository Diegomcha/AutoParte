import { Outlet, useOutletContext } from "react-router";

import { useBooking } from "..";

import PersonForm from "~/component/form/PersonForm";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { PersonDtoResponse } from "~/@types/api";
import type { BookingsContextType } from "..";
import type { Route } from "./+types/edit";

export interface EditPersonOutletContext extends BookingsContextType {
	person: PersonDtoResponse;
}

export async function clientLoader({
	params: { accommodationId, bookingId, id }
}: Route.ClientLoaderArgs) {
	Validators.validateUuids(accommodationId, bookingId, id);

	return {
		person: await queryClient.query(
			queryFactory.accommodations.bookings.people.detail(
				accommodationId,
				bookingId,
				id
			)
		)
	};
}

export default function EditPerson({
	params: { accommodationId, bookingId },
	loaderData: { person }
}: Route.ComponentProps) {
	const booking = useBooking();

	return (
		<>
			<PersonForm
				key={person.id}
				accommodationId={accommodationId}
				bookingId={bookingId}
				person={person}
				readOnly={!booking.canBeModified}
			/>
			<Outlet context={{ booking, person } satisfies EditPersonOutletContext} />
		</>
	);
}

export function usePerson() {
	return useOutletContext<EditPersonOutletContext>().person;
}
