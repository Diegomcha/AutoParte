import dayjs from 'dayjs';
import localizedFormat from 'dayjs/plugin/localizedFormat';
import i18n from './i18n';
// Languages
import 'dayjs/locale/es';
import 'dayjs/locale/en';

dayjs.extend(localizedFormat);
dayjs.locale(i18n.language.split('-')[0] ?? 'es');
