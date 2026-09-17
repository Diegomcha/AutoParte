import { useState } from "react";
import { useNavigate } from "react-router";

import {
	Button,
	Group,
	Modal,
	Paper,
	Stack,
	Table,
	Text,
	useMantineTheme
} from "@mantine/core";
import { useMediaQuery } from "@mantine/hooks";

import { CheckIcon, SignatureIcon, SuitcaseIcon } from "@phosphor-icons/react";
import { useMutation, useSuspenseQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import SignatureBox from "~/component/SignatureBox";
import { queryClient, queryFactory } from "~/services/Api";
import Validators from "~/services/Validators";

import type { PersonDtoResponse } from "~/@types/api";
import type { Route } from "./+types/send";

export async function clientLoader({
	params: { accommodationId, bookingId }
}: Route.ClientLoaderArgs) {
	const [booking, _] = await Promise.all([
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

	if (booking.status !== "CHECK_IN_READY")
		throw Validators.throwValidationErrorResponse(
			"Booking cannot be checked-in."
		);
}

export default function SendCheckIn({
	params: { accommodationId, bookingId }
}: Route.ComponentProps) {
	const navigate = useNavigate();

	const { t } = useTranslation("routes", {
		keyPrefix: "checkIn.send"
	});
	const { t: tCommon } = useTranslation();

	const { data: people } = useSuspenseQuery(
		queryFactory.accommodations.bookings.people.list(accommodationId, bookingId)
	);

	const { mutate: checkIn, isPending: isCheckingIn } = useMutation(
		queryFactory.accommodations.bookings.checkIn(accommodationId, bookingId)
	);

	// Not everyone needs to sign
	const signers = people.filter((person) => person.mustSign);

	const [signer, setSigner] = useState<PersonDtoResponse | null>(null);

	const theme = useMantineTheme();
	const isMobile = useMediaQuery(`(max-width: ${theme.breakpoints.md})`);

	return (
		<>
			<Stack>
				<Text textWrap="pretty">{t(($) => $.explanation)}</Text>

				<Paper withBorder shadow="none">
					<Table withRowBorders={false}>
						<Table.Thead>
							<Table.Tr>
								<Table.Th>{t(($) => $.table.header.person)}</Table.Th>
								<Table.Th>{t(($) => $.table.header.signature)}</Table.Th>
							</Table.Tr>
						</Table.Thead>
						<Table.Tbody>
							{signers.map((person) => (
								<Table.Tr key={person.id}>
									<Table.Td>
										{person.personalInfo.name}{" "}
										{person.personalInfo.firstSurname}{" "}
										{person.personalInfo.secondSurname}
									</Table.Td>
									<Table.Td>
										<Button
											fullWidth
											onClick={() => {
												setSigner(person);
											}}
											disabled={person.hasSigned}
											leftSection={
												person.hasSigned ? (
													<CheckIcon weight="bold" />
												) : (
													<SignatureIcon weight="bold" />
												)
											}
										>
											{t(($) =>
												person.hasSigned
													? $.table.signatureButton.signed
													: $.table.signatureButton.sign
											)}
										</Button>
									</Table.Td>
								</Table.Tr>
							))}
						</Table.Tbody>
					</Table>
				</Paper>
				<Group justify="right">
					<Button
						color="green"
						leftSection={<SuitcaseIcon weight="bold" />}
						disabled={people.some(
							(person) => person.mustSign && !person.hasSigned
						)}
						loading={isCheckingIn}
						onClick={() => {
							checkIn(undefined, {
								onSuccess: () => {
									void navigate("/check-in-completed");
								}
							});
						}}
					>
						{tCommon(($) => $.buttons.finish)}
					</Button>
				</Group>
			</Stack>

			<Modal
				fullScreen={isMobile}
				size="auto"
				opened={!!signer}
				onClose={() => {
					setSigner(null);
				}}
			>
				{signer && (
					<SignatureBox
						accommodationId={accommodationId}
						bookingId={bookingId}
						person={signer}
					/>
				)}
			</Modal>
		</>
	);
}
