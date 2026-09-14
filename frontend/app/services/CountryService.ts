import i18nCountries from "i18n-iso-countries";
import { isSupportedCountry } from "libphonenumber-js";

import { lang, modulesLocales } from "~/i18n";

import type { Alpha2Code, Alpha3Code } from "i18n-iso-countries";
import type { CountryCode as PhoneCountryCode } from "libphonenumber-js";

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
			.split("")
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion -- There is always at least one character in 'char'.
			.map((char) => 127397 + char.codePointAt(0)!);
		return String.fromCodePoint(...codePoints);
	}

	/**
	 * Converts an ISO 3166-1 alpha-3 country code to its corresponding alpha-2 code.
	 * @param countryCode The ISO 3166-1 alpha-3 country code (e.g., "USA", "ESP").
	 * @returns The corresponding ISO 3166-1 alpha-2 country code (e.g., "US", "ES") or undefined if the alpha-3 code is invalid or not found.
	 */
	getAlpha2Code(countryCode: Alpha3Code) {
		const alpha2Code = i18nCountries.alpha3ToAlpha2(countryCode);
		if (!alpha2Code) return undefined;
		return alpha2Code as Alpha2Code;
	}

	/**
	 * Gets the phone country code for a given ISO 3166-1 alpha-3 country code.
	 * @param countryCode The ISO 3166-1 alpha-3 country code (e.g., "USA", "ESP").
	 * @returns The corresponding phone country code (e.g., "US", "ES") or undefined if the alpha-3 code is invalid or not found.
	 */
	getPhoneCountryCode(countryCode: Alpha3Code) {
		const alpha2Code = this.getAlpha2Code(countryCode);
		if (!alpha2Code || !isSupportedCountry(alpha2Code)) return undefined;
		return alpha2Code as PhoneCountryCode;
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
