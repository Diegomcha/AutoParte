import { ZxcvbnFactory } from "@zxcvbn-ts/core";
import * as zxcvbnCommonPackage from "@zxcvbn-ts/language-common";

import { modulesLocales } from "~/i18n";

const langPackage = await modulesLocales.zxcvbn();

export default new ZxcvbnFactory({
	translations: langPackage.translations,
	graphs: zxcvbnCommonPackage.adjacencyGraphs,
	dictionary: {
		...zxcvbnCommonPackage.dictionary,
		...langPackage.dictionary
	}
});
