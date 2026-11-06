import { useMemo, useRef, useState } from "react";

import {
	AspectRatio,
	Box,
	Button,
	Flex,
	Overlay,
	Paper,
	Stack,
	Text
} from "@mantine/core";
import { useElementSize } from "@mantine/hooks";

import { SignatureIcon, TrashIcon } from "@phosphor-icons/react";
import { useMutation, useSuspenseQuery } from "@tanstack/react-query";
import Signature from "@uiw/react-signature";
import { useTranslation } from "react-i18next";

import { queryFactory } from "~/services/Api";
import TimeService from "~/services/TimeService";

import type { SignatureRef } from "@uiw/react-signature";
import type { operations, PersonDtoResponse } from "~/@types/api";

export interface SignatureBoxProps {
	accommodationId: string;
	bookingId: string;
	person: PersonDtoResponse;
	onSignature?: () => void;
	mode?: "interactive" | "readOnly" | "photo";
}

const RESOLUTION = 10_000;

export default function SignatureBox({
	accommodationId,
	bookingId,
	person,
	onSignature,
	mode: defaultMode = "interactive",
	...props
}: Readonly<SignatureBoxProps>) {
	const { t } = useTranslation("components", { keyPrefix: "signatureBox" });

	const { data: signature } = useSuspenseQuery(
		queryFactory.accommodations.bookings.people.getSignature(
			accommodationId,
			bookingId,
			person.id
		)
	);

	const { mutate: sign, isPending } = useMutation(
		queryFactory.accommodations.bookings.people.sign(
			accommodationId,
			bookingId,
			person.id
		)
	);

	const { ref: containerRef, width, height } = useElementSize();
	const signatureRef = useRef<SignatureRef>(null);

	const [mode, setMode] = useState(defaultMode);
	const [points, setPoints] = useState<
		operations["addSignature"]["requestBody"]["content"]["application/json"]
	>(signature?.paths ?? {});

	function normalizePoint([x, y]: [number, number]): { x: number; y: number } {
		return {
			x: (x / width) * RESOLUTION,
			y: (y / height) * RESOLUTION
		};
	}

	function denormalizePoint({
		x,
		y
	}: {
		x: number;
		y: number;
	}): [number, number] {
		return [(x * width) / RESOLUTION, (y * height) / RESOLUTION];
	}

	const initialPoints = useMemo(
		() =>
			Object.fromEntries(
				Object.entries(points).map(([instant, path]) => [
					instant,
					// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- We are sure that x and y are not null
					path.map(({ x, y }) => denormalizePoint({ x: x!, y: y! }))
				])
			),
		[points, width]
	);

	function handleStroke(rawPoints: number[][]) {
		setPoints((prev) => ({
			...prev,
			[TimeService().toISOString()]: rawPoints.map(([x, y]) =>
				// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- We are sure that x and y are not null
				normalizePoint([x!, y!])
			)
		}));
	}

	return (
		<Stack w="100%">
			<AspectRatio ratio={2}>
				<Paper
					ref={containerRef}
					withBorder
					pos="relative"
					flex={1}
					mah="70vh"
					style={{ overflow: "hidden" }}
				>
					{mode === "readOnly" && (
						<Overlay opacity={0.1} style={{ cursor: "not-allowed" }} />
					)}
					<Signature
						key={`${signature?.signedAt ?? "signature"}-${width.toString()}`}
						ref={signatureRef}
						onPointer={handleStroke}
						defaultPoints={initialPoints}
						readonly={mode !== "interactive"}
						{...props}
					/>
				</Paper>
			</AspectRatio>
			<Flex
				direction={{ base: "column", sm: "row" }}
				align={{ base: "stretch", sm: "center" }}
				justify="space-between"
				gap="xl"
				w="100%"
				hidden={mode === "photo"}
			>
				<Box
					flex={1}
					style={{
						userSelect: "none"
					}}
				>
					<Text fw={600}>
						{person.personalInfo.name} {person.personalInfo.firstSurname}{" "}
						{person.personalInfo.secondSurname}
					</Text>
					<Text size="sm" color="dimmed" textWrap="pretty" maw={750}>
						{mode !== "interactive"
							? t(($) => $.details, {
									signedAt: TimeService(signature?.signedAt).format("LLLL"),
									ip: signature?.ipAddress
								})
							: t(($) => $.disclaimer)}
					</Text>
				</Box>
				<Flex
					gap="xs"
					justify="right"
					hidden={mode !== "interactive"}
					direction={{ base: "row", sm: "column" }}
				>
					<Button
						color="gray"
						loading={isPending}
						disabled={Object.keys(points).length === 0}
						leftSection={<TrashIcon weight="bold" />}
						onClick={() => {
							setPoints({});
							signatureRef.current?.clear();
						}}
					>
						{t(($) => $.buttons.clear)}
					</Button>
					<Button
						color="green"
						loading={isPending}
						disabled={Object.keys(points).length === 0}
						leftSection={<SignatureIcon weight="bold" />}
						onClick={() => {
							sign(points, {
								onSuccess: () => {
									setMode("readOnly");
									onSignature?.();
								}
							});
						}}
					>
						{t(($) => $.buttons.sign)}
					</Button>
				</Flex>
			</Flex>
		</Stack>
	);
}
