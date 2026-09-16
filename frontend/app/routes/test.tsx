import { Box, Center } from "@mantine/core";

import { useSuspenseQuery } from "@tanstack/react-query";

import SignatureBox from "~/component/SignatureBox";
import { queryFactory } from "~/services/Api";

// TODO: REmove ROUTE!
export default function TestRoute() {
	const { data: person } = useSuspenseQuery(
		queryFactory.accommodations.bookings.people.detail(
			"01a0349c-9ab9-7945-9d4d-dbe50e00207b",
			"01a0a0a0-f173-77dc-be30-aab7dc9f6a90",
			"01a0a101-724c-7250-8de6-e8ad790531ad"
		)
	);

	return (
		<Box maw={950} h={750} bg="gray" p="md">
			<Center>
				<SignatureBox
					accommodationId="01a0349c-9ab9-7945-9d4d-dbe50e00207b"
					bookingId="01a0a0a0-f173-77dc-be30-aab7dc9f6a90"
					person={person}
				/>
			</Center>
		</Box>
	);
}
