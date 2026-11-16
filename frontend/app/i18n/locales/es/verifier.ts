import common from "./common.json";
import components from "./components.json";
import entities from "./entities.json";
import routes from "./routes.json";

import type { i18nConfig } from "~/i18n";

type BaseLanguage = (typeof i18nConfig.resources)["en"];

// This is a workaround to ensure that the TypeScript compiler recognizes the structure of the Spanish translation files as equivalent to the English ones.
// It helps maintain type safety across different language resources.
const equalityValidation: BaseLanguage = {
	common,
	components,
	entities,
	routes
};

export default { _DO_NOT_USE_ES: equalityValidation };
