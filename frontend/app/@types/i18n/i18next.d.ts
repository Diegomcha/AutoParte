import 'i18next';
import resources from '../../../public/locales/es/translation.json';

declare module 'i18next' {
	interface CustomTypeOptions {
		resources: {
			translation: typeof resources;
		};
		enableSelector: true;
	}
}
