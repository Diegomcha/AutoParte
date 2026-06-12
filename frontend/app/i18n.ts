import i18n from 'i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import HttpAPI from 'i18next-http-backend';
import { initReactI18next } from 'react-i18next';

await i18n
	// Pass the i18n instance to react-i18next.
	.use(initReactI18next)
	// Detect user language
	.use(LanguageDetector)
	// Load translation using http -> see /public/locales
	.use(HttpAPI)
	.init({
		lng: 'es', // TODO: remove this when we have the translations ready
		fallbackLng: 'es',
		enableSelector: true,
		interpolation: {
			escapeValue: false, // not needed for react as it escapes by default
		},
	});

export default i18n;
