import "i18next";

import type { i18nConfig } from "~/i18n";

type I18nConfig = typeof i18nConfig;

declare module "i18next" {
	interface CustomTypeOptions extends I18nConfig {
		resources: (typeof i18nConfig.resources)["es"];
	}
}
