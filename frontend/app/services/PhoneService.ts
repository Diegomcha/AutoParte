import { parsePhoneNumberFromString } from "libphonenumber-js/min";

class PhoneService {
	/**
	 * Parses and formats a raw phone number into international format.
	 * @param rawPhoneNumber Raw phone number to parse and format
	 * @returns The formatted phone number in international format.
	 * @throws Will throw an error if the phone number is invalid.
	 */
	format(rawPhoneNumber: string): string {
		const phoneNumber = parsePhoneNumberFromString(rawPhoneNumber);
		if (!phoneNumber)
			throw new Error(`Invalid phone number: ${rawPhoneNumber}`);

		return phoneNumber.formatInternational();
	}
}

export default new PhoneService();
