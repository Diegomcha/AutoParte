import { ZxcvbnFactory } from "@zxcvbn-ts/core";
import * as zxcvbnCommonPackage from "@zxcvbn-ts/language-common";

import { modulesLocales } from "~/i18n";

export default new ZxcvbnFactory({
	translations: modulesLocales.zxcvbn.translations,
	graphs: zxcvbnCommonPackage.adjacencyGraphs,
	dictionary: {
		...zxcvbnCommonPackage.dictionary,
		...modulesLocales.zxcvbn.dictionary
	}
});
