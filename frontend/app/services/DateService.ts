import dayjs from 'dayjs';
import isoWeek from 'dayjs/plugin/isoWeek';
import type { ResourcesScheduleViewLevel } from '@mantine/schedule';

dayjs.extend(isoWeek);

class DateService {
	getDateRange(unit: ResourcesScheduleViewLevel, date: Date | string) {
		const baseDate = dayjs(date);

		// Makes sure that the week starts on Monday instead of Sunday
		const rangeUnit = unit === 'week' ? 'isoWeek' : unit;

		const start = baseDate.startOf(rangeUnit).toISOString();
		const end = baseDate.endOf(rangeUnit).toISOString();

		return [start, end] as const;
	}
}

export default new DateService();
