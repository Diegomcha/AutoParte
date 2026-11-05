import { expect, test, vi } from "vitest";

import CountryService from "~/services/CountryService";

vi.unmock("~/services/CountryService");

test("converts supported country codes and creates flags", () => {
	expect(CountryService.getAlpha2Code("ESP")).toBe("ES");
	expect(CountryService.getPhoneCountryCode("ESP")).toBe("ES");
	expect(CountryService.getFlag("ESP")).toBe("🇪🇸");
});

test("rejects unknown country codes", () => {
	expect(() => CountryService.getFlag("XXX" as never)).toThrow(
		"Invalid country code: XXX"
	);
	expect(() => CountryService.getName("XXX" as never)).toThrow(
		"Invalid country code: XXX"
	);
	expect(CountryService.getAlpha2Code("XXX" as never)).toBeUndefined();
});
