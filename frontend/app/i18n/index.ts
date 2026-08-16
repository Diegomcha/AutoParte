import i18n from 'i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import resourcesToBackend from 'i18next-resources-to-backend';
import { initReactI18next } from 'react-i18next';
// Import the default Spanish translation file
import es from './locales/es/translation.json';

// TODO: Translate to other languages
export const supportedLanguages = ['es', 'en'] as const;
type SupportedLanguage = (typeof supportedLanguages)[number];

// Initialize i18next for internationalization
await i18n
	// Pass the i18n instance to react-i18next.
	.use(initReactI18next)
	// Detect user language
	.use(LanguageDetector)
	// Lazy-load other translation files using dynamic imports
	.use(
		resourcesToBackend(
			(language: string, namespace: string) =>
				import(`./locales/${language}/${namespace}.json`)
		)
	)
	.init({
		fallbackLng: 'es',
		supportedLngs: supportedLanguages,
		// Preload the default language resources
		resources: {
			es: {
				translation: es,
			},
		},
		partialBundledLanguages: true,
		// Enable the TypeScript selector for type-safe translations
		enableSelector: true,
		// Interpolation escaping not needed for react as it escapes by default
		interpolation: {
			escapeValue: false,
		},
	});

export default i18n;

export const lang = i18n.language as (typeof supportedLanguages)[number];

// Third-party libraries that require locale configuration

const modulesLocaleMap = {
	dayjs: {
		es: () => import('dayjs/locale/es'),
		en: () => import('dayjs/locale/en'),
	},
	i18nCountries: {
		es: () => import('i18n-iso-countries/langs/es.json'),
		en: () => import('i18n-iso-countries/langs/en.json'),
	},
} satisfies Record<string, Record<SupportedLanguage, () => Promise<unknown>>>;

// Loads the required locale for each third-party library based on the detected language
export const modulesLocales = Object.fromEntries(
	await Promise.all(
		Object.entries(modulesLocaleMap).map(async ([library, map]) => {
			const locale = await map[lang]();

			return [library, locale] as const;
		})
	)
) as {
	[K in keyof typeof modulesLocaleMap]: Awaited<
		ReturnType<(typeof modulesLocaleMap)[K][SupportedLanguage]>
	>;
};
