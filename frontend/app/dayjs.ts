import dayjs from 'dayjs';
import localizedFormat from 'dayjs/plugin/localizedFormat';
import { lang } from './i18n';
// Languages
import 'dayjs/locale/en';
import 'dayjs/locale/es';

dayjs.extend(localizedFormat);
dayjs.locale(lang);
