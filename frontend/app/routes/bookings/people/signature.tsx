import { useNavigate } from "react-router";

import { Modal } from "@mantine/core";

import SignatureBox from "~/component/input/SignatureBox";
import useStaticModalTransition from "~/hooks/useStaticModalTransition";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import { usePerson } from "./edit";

import type { Route } from "./+types/signature";

export async function clientLoader({
	params: { accommodationId, bookingId, id }
}: Route.ClientLoaderArgs) {
	const signature = await queryClient.query(
		queryFactory.accommodations.bookings.people.getSignature(
			accommodationId,
			bookingId,
			id
		)
	);

	if (!signature)
		throw Validators.throwValidationErrorResponse("Signature not found");
}

export default function PersonSignature({
	params: { accommodationId, bookingId }
}: Route.ComponentProps) {
	const navigate = useNavigate();
	const { opened, close } = useStaticModalTransition(() => void navigate(".."));

	const person = usePerson();

	return (
		<Modal opened={opened} onClose={close} size="auto">
			<SignatureBox
				accommodationId={accommodationId}
				bookingId={bookingId}
				person={person}
				mode="readOnly"
			/>
		</Modal>
	);
}
