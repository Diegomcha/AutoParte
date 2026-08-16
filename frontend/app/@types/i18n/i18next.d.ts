import 'i18next';
import resources from '../../i18n/locales/es/translation.json';

declare module 'i18next' {
	interface CustomTypeOptions {
		resources: {
			translation: typeof resources;
		};
		enableSelector: true;
	}
}
