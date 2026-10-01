import { useRef, useState } from "react";

import {
	AspectRatio,
	Box,
	Button,
	Center,
	Flex,
	Overlay,
	Paper,
	Progress,
	Stack,
	Text
} from "@mantine/core";

import {
	CameraSlashIcon,
	CaretLeftIcon,
	ScanIcon
} from "@phosphor-icons/react";
import WebCamera from "@shivantra/react-web-camera";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import MrzPlacer from "~/assets/mrz-placer.svg";
import { queryFactory } from "~/services/Api";
import NotificationsService from "~/services/NotificationsService";

import type { PaperProps } from "@mantine/core";
import type { WebCameraHandler } from "@shivantra/react-web-camera";
import type { PartialPersonDtoRequest } from "~/@types/api";

interface ScanDocumentProps {
	onScanSuccess: (data: PartialPersonDtoRequest) => void;
	onBack: () => void;
}

export default function DocumentScanner({
	onScanSuccess,
	onBack,
	...props
}: Readonly<PaperProps & ScanDocumentProps>) {
	const { t } = useTranslation("components", { keyPrefix: "documentScanner" });
	const { t: tCommon } = useTranslation();

	const cam = useRef<WebCameraHandler>(null);

	const [error, setError] = useState(false);
	const [retryKey, setRetryKey] = useState(0);

	const [status, setStatus] = useState<
		"idle" | "scanning" | "noSuccess" | "success"
	>("idle");

	const { mutateAsync: scan } = useMutation(queryFactory.ocr.mrz());

	async function captureAndScan() {
		setStatus("scanning");
		const frame = await cam.current?.capture();
		if (frame) {
			const scanRes = await scan(frame);
			if (scanRes) {
				cam.current?.stop();
				setStatus("success");

				onScanSuccess(scanRes);

				return;
			}
		}
		setStatus("noSuccess");
	}

	return (
		<Paper
			withBorder
			style={{
				overflow: "hidden"
			}}
			{...props}
		>
			<Stack gap={0}>
				{status !== "idle" && (
					<Stack gap="xs" pt="xs">
						<Text ta="center" size="sm">
							{t(($) => $.progress.status.labels[status])}
						</Text>
						<Progress
							value={100}
							radius={0}
							animated={status === "scanning"}
							color={t(($) => $.progress.status.colors[status])}
						/>
					</Stack>
				)}
				<AspectRatio
					ratio={500 / 315}
					pos="relative"
					style={{ containerType: "inline-size" }}
				>
					{error && (
						<Overlay blur={5}>
							<Stack
								w="100%"
								h="100%"
								align="center"
								justify="center"
								c="white"
							>
								<CameraSlashIcon size="25%" weight="bold" />
								<Text fw="bold" size="4cqw">
									{t(($) => $.noCameraError.title)}
								</Text>
								<Text size="2cqw" px="md">
									{t(($) => $.noCameraError.message)}
								</Text>
								<Button
									color="gray"
									mt="sm"
									onClick={() => {
										setRetryKey((prev) => prev + 1);
										setError(false);
									}}
								>
									{t(($) => $.noCameraError.retryButton)}
								</Button>
							</Stack>
						</Overlay>
					)}
					<WebCamera
						key={retryKey}
						ref={cam}
						captureMode="back"
						style={{
							height: "100%",
							width: "100%",
							objectFit: "cover",
							backgroundColor: "black"
						}}
						onError={(err) => {
							NotificationsService.error(t(($) => $.noCameraError.title));
							console.error(
								"Error when accessing the camera, check if the camera is connected and if the browser and page have access to it\n",
								err
							);
							setError(true);
						}}
					/>
					<Center
						pos="absolute"
						top={0}
						left={0}
						w="100%"
						h="100%"
						ta="center"
						opacity={0.75}
						style={{ pointerEvents: "none" }}
					>
						<Text
							pos="absolute"
							bottom="43.55%"
							w="100%"
							size="2.2cqw"
							ff="OCR B"
							tt="uppercase"
						>
							{t(($) => $.mrzZone.title)}
						</Text>
						<img
							src={MrzPlacer}
							alt={t(($) => $.mrzZone.imgLabel)}
							width="90%"
							height="90%"
						/>
					</Center>
				</AspectRatio>
				<Flex
					direction={{ base: "column", md: "row" }}
					align={{ base: "stretch", md: "center" }}
					justify="space-between"
					gap="md"
					w="100%"
					p="md"
				>
					<Box flex={1}>
						<Text fw="bold">{t(($) => $.explanation.title)}</Text>
						<Text c="dimmed" size="sm" textWrap="pretty">
							{t(($) => $.explanation.message)}
						</Text>
					</Box>

					<Flex
						gap="xs"
						justify="right"
						direction={{ base: "row", md: "column" }}
					>
						<Button
							color="gray"
							loading={status === "scanning"}
							leftSection={<CaretLeftIcon weight="bold" />}
							onClick={onBack}
						>
							{tCommon(($) => $.buttons.back)}
						</Button>
						<Button
							loading={status === "scanning"}
							leftSection={<ScanIcon weight="bold" />}
							onClick={() => {
								void captureAndScan();
							}}
						>
							{tCommon(($) => $.buttons.scan)}
						</Button>
					</Flex>
				</Flex>
			</Stack>
		</Paper>
	);
}
