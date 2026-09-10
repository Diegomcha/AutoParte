import i18n from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import resourcesToBackend from "i18next-resources-to-backend";
import { initReactI18next } from "react-i18next";

import common from "./locales/es/common.json";
import components from "./locales/es/components.json";
import entities from "./locales/es/entities.json";
import routes from "./locales/es/routes.json";
import services from "./locales/es/services.json";

import type { InitOptions } from "i18next";

export const i18nConfig = {
	fallbackLng: "es",
	// TODO: Translate to other languages
	supportedLngs: ["es"],
	defaultNS: "common",
	fallbackNS: "common",
	ns: ["common", "components", "services", "routes", "entities"],
	// Preload the default language resources
	resources: {
		es: {
			common,
			components,
			services,
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
		es: () => import("dayjs/locale/es")
		// en: () => import("dayjs/locale/en")
	},
	i18nCountries: {
		es: () => import("i18n-iso-countries/langs/es.json")
		// en: () => import("i18n-iso-countries/langs/en.json")
	}
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
