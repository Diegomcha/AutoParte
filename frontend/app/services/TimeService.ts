import { modulesLocales } from '~/i18n';
import dayjs from 'dayjs';
// Plugins
import customParseFormat from 'dayjs/plugin/customParseFormat';
import isoWeek from 'dayjs/plugin/isoWeek';
import localizedFormat from 'dayjs/plugin/localizedFormat';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(isoWeek);
dayjs.extend(relativeTime);
dayjs.extend(customParseFormat);
dayjs.extend(localizedFormat);

// Set the locale for dayjs based on the detected language
dayjs.locale(modulesLocales.dayjs);

export default dayjs;
