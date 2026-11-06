import { Text, Timeline } from "@mantine/core";

import { StarIcon } from "@phosphor-icons/react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import { queryFactory } from "~/services/Api";
import TimeService from "~/services/TimeService";

import CommunicationTimelineItem from "./CommunicationTimelineItem";

import type { TimelineProps } from "@mantine/core";

interface CommunicationsTimelineProps extends TimelineProps {
	accommodationId: string;
	bookingId: string;
	createdAt: Date;
}

export default function CommunicationTimeline({
	accommodationId,
	bookingId,
	createdAt,
	...props
}: Readonly<CommunicationsTimelineProps>) {
	const { t } = useTranslation("components", {
		keyPrefix: "communicationTimeline"
	});
	const { data: communications } = useSuspenseQuery(
		queryFactory.accommodations.bookings.communications.list(
			accommodationId,
			bookingId
		)
	);

	return (
		<Timeline
			bulletSize={24}
			lineWidth={2}
			active={
				["PENDING", "SENT", "PENDING_VOIDED"].includes(
					communications.at(-1)?.status ?? ""
				)
					? communications.length - 1
					: communications.length
			}
			{...props}
		>
			{/* Creation date */}
			<Timeline.Item
				bullet={<StarIcon weight="fill" />}
				title={t(($) => $.creationFakeEvent.title)}
			>
				<Text size="sm" c="dark">
					{TimeService(createdAt).fromNow()}
				</Text>
			</Timeline.Item>
			{/* Regular communications */}
			{communications.map((communication) => (
				<CommunicationTimelineItem
					key={communication.id}
					communication={communication}
				/>
			))}
		</Timeline>
	);
}
