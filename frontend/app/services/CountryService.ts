import { lang, modulesLocales } from '~/i18n';
import i18nCountries from 'i18n-iso-countries';
import type { Alpha3Code } from 'i18n-iso-countries';

class CountryService {
	/**
	 * Gets the flag emoji for a given country code.
	 * @param countryCode The ISO 3166-1 alpha-3 country code (e.g., "USA", "ESP").
	 * @returns The flag emoji corresponding to the country code.
	 * @throws Will throw an error if the country code is invalid or not found.
	 */
	getFlag(countryCode: Alpha3Code) {
		const alpha2Code = i18nCountries.alpha3ToAlpha2(countryCode);
		if (!alpha2Code) throw new Error(`Invalid country code: ${countryCode}`);

		const codePoints = alpha2Code
			.toUpperCase()
			.split('')
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- There is always at least one character in 'char'.
			.map((char) => 127397 + char.codePointAt(0)!);
		return String.fromCodePoint(...codePoints);
	}

	/**
	 * Gets the name of a country based on its ISO 3166-1 alpha-3 code.
	 * @param countryCode The ISO 3166-1 alpha-3 country code (e.g., "USA", "ESP").
	 * @returns The name of the country in the current language, or undefined if not found.
	 * @throws Will throw an error if the country code is invalid or not found.
	 */
	getName(countryCode: Alpha3Code) {
		const name = i18nCountries.getName(countryCode, lang);
		if (!name) throw new Error(`Invalid country code: ${countryCode}`);
		return name;
	}
}

// Configure countries library with the detected language
i18nCountries.registerLocale(modulesLocales.i18nCountries);

export default new CountryService();

export type { Alpha3Code as CountryCode };
