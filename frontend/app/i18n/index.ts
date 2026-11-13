import i18n from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import resourcesToBackend from "i18next-resources-to-backend";
import { initReactI18next } from "react-i18next";

import common from "./locales/en/common.json";
import components from "./locales/en/components.json";
import entities from "./locales/en/entities.json";
import routes from "./locales/en/routes.json";

import type { InitOptions } from "i18next";

export const i18nConfig = {
	fallbackLng: "en",
	supportedLngs: ["es", "en"],
	defaultNS: "common",
	fallbackNS: "common",
	ns: ["common", "components", "routes", "entities"],
	// Preload the default language resources
	resources: {
		en: {
			common,
			components,
			routes,
			entities
		}
	},
	partialBundledLanguages: true,
	// Enable the TypeScript selector for type-safe translations
	enableSelector: true,
	// Interpolation escaping not needed for react as it escapes by default
	interpolation: {
		escapeValue: false
	}
} as const satisfies InitOptions;

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
	.init(i18nConfig);

export default i18n;

type SupportedLanguage = (typeof i18nConfig.supportedLngs)[number];
export const lang = i18n.language as (typeof i18nConfig.supportedLngs)[number];

// Third-party libraries that require locale configuration

const modulesLocaleMap = {
	dayjs: {
		en: () => import("dayjs/locale/en-gb"),
		es: () => import("dayjs/locale/es")
	},
	i18nCountries: {
		en: () => import("i18n-iso-countries/langs/en.json"),
		es: () => import("i18n-iso-countries/langs/es.json")
	},
	zxcvbn: {
		en: () => import("@zxcvbn-ts/language-en"),
		es: () => import("@zxcvbn-ts/language-es-es")
	}
} satisfies Record<string, Record<SupportedLanguage, () => Promise<unknown>>>;

// Loads the required locale for each third-party library based on the detected language
export const modulesLocales = Object.fromEntries(
	Object.entries(modulesLocaleMap).map(([library, map]) => {
		const locale = map[lang];

		return [library, locale] as const;
	})
) as {
	[
		K in keyof typeof modulesLocaleMap
	]: (typeof modulesLocaleMap)[K][SupportedLanguage];
};
