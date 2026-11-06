import { forwardRef, useImperativeHandle, useRef } from "react";

import { Calendar } from "@mantine/dates";

import { toPng } from "html-to-image";

import { lang } from "~/i18n";
import TimeService from "~/services/TimeService";

import type { BookingDtoResponse } from "~/@types/api";

export interface CalendarRendererRef {
	getImage: () => Promise<string>;
}

export interface CalendarRendererProps {
	booking: BookingDtoResponse;
}

export const CalendarRenderer = forwardRef<
	CalendarRendererRef,
	CalendarRendererProps
>(({ booking }, ref) => {
	const calendarRef = useRef<HTMLDivElement>(null);

	function inRange(date: string) {
		return TimeService(date).isBetween(
			booking.startTime,
			booking.endTime,
			"day",
			"[]"
		);
	}

	useImperativeHandle(ref, () => ({
		async getImage(): Promise<string> {
			if (!calendarRef.current)
				throw new Error("Calendar element is not mounted.");

			return await toPng(calendarRef.current, {
				cacheBust: true,
				pixelRatio: 2,
				style: {
					opacity: "1"
				}
			});
		}
	}));

	return (
		<div
			style={{
				position: "absolute",
				top: "-9999px",
				left: "-9999px",
				pointerEvents: "none"
			}}
		>
			<Calendar
				style={{
					opacity: 0
				}}
				ref={calendarRef}
				static
				defaultDate={booking.startTime}
				minDate={booking.startTime}
				maxDate={booking.endTime}
				maxLevel="month"
				getDayProps={(date) => {
					const dateObj = TimeService(date);
					return {
						selected:
							dateObj.isSame(booking.startTime, "day") ||
							dateObj.isSame(booking.endTime, "day"),
						inRange: inRange(date),
						firstInRange: dateObj.isSame(booking.startTime, "day"),
						lastInRange: dateObj.isSame(booking.endTime, "day"),
						disabled: false
					};
				}}
				locale={lang}
			/>
		</div>
	);
});

CalendarRenderer.displayName = "CalendarRenderer";
