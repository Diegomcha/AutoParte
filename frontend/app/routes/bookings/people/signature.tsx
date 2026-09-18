import { useNavigate } from "react-router";

import { Modal } from "@mantine/core";

import SignatureBox from "~/component/SignatureBox";
import useStaticModalTransition from "~/hooks/useStaticModalTransition";

import { usePerson } from "./edit";

import type { Route } from "./+types/signature";

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
