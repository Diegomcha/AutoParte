import { Text, ThemeIcon, Timeline, Tooltip } from '@mantine/core';
import {
	CalendarCheckIcon,
	CalendarSlashIcon,
	CalendarXIcon,
	SignInIcon,
	SpinnerGapIcon,
	XIcon,
} from '@phosphor-icons/react';
import TimeService from '~/services/TimeService';
import { useTranslation } from 'react-i18next';
import type { CommunicationDtoResponse } from '~/@types/api';

const COMMUNICATION_STATUS_ICONS: Record<
	Exclude<CommunicationDtoResponse['status'], 'SUCCEEDED'>,
	React.ReactNode
> = {
	PENDING: <SpinnerGapIcon weight="bold" className="animate-spin" />,
	PENDING_VOIDED: <SpinnerGapIcon weight="bold" className="animate-spin" />,
	SENT: <SpinnerGapIcon weight="bold" className="animate-spin" />,
	FAILED: <XIcon weight="bold" />,
	VOIDED: <CalendarSlashIcon weight="bold" />,
};

const COMMUNICATION_TYPE_ICONS: Record<
	CommunicationDtoResponse['type'],
	React.ReactNode
> = {
	BOOKING: <CalendarCheckIcon weight="bold" />,
	CHECKIN: <SignInIcon weight="bold" />,
	CANCELLATION: <CalendarXIcon weight="bold" />,
};

export default function CommunicationTimelineItem({
	communication,
	...props
}: Timeline.Item.Props & { communication: CommunicationDtoResponse }) {
	const { t } = useTranslation();

	return (
		<Timeline.Item
			{...props}
			bullet={
				<Tooltip
					label={t(
						($) =>
							$.bookings.properties.communications.status[communication.status]
								.label
					)}
				>
					<ThemeIcon
						radius="xl"
						size={22}
						color={t(
							($) =>
								$.bookings.properties.communications.status[
									communication.status
								].color
						)}
					>
						{communication.status === 'SUCCEEDED'
							? COMMUNICATION_TYPE_ICONS[communication.type]
							: COMMUNICATION_STATUS_ICONS[communication.status]}
					</ThemeIcon>
				</Tooltip>
			}
			title={
				<p
					className={
						['FAILED', 'VOIDED'].includes(communication.status)
							? 'line-through'
							: ''
					}
				>
					{t(
						($) =>
							$.bookings.properties.communications.types[communication.type]
					)}
				</p>
			}
		>
			{communication.sentTimestamp && (
				<Text size="sm" c="dark">
					{t(($) => $.bookings.properties.communications.sentDate, {
						date: TimeService(communication.sentTimestamp).fromNow(),
					})}
				</Text>
			)}
			{communication.error && (
				<Text size="xs" c="red" w="0" miw="100%">
					{communication.error}
				</Text>
			)}
		</Timeline.Item>
	);
}
