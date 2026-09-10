import dayjs from "dayjs";
// Plugins
import customParseFormat from "dayjs/plugin/customParseFormat";
import duration from "dayjs/plugin/duration";
import isBetween from "dayjs/plugin/isBetween";
import isoWeek from "dayjs/plugin/isoWeek";
import localizedFormat from "dayjs/plugin/localizedFormat";
import relativeTime from "dayjs/plugin/relativeTime";

import { modulesLocales } from "~/i18n";

dayjs.extend(isoWeek);
dayjs.extend(relativeTime);
dayjs.extend(customParseFormat);
dayjs.extend(localizedFormat);
dayjs.extend(isBetween);
dayjs.extend(duration);

// Set the locale for dayjs based on the detected language
dayjs.locale(modulesLocales.dayjs);

export default dayjs;
