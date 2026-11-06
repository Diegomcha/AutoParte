import { fileURLToPath } from "node:url";

import { defineConfig } from "vitest/config";

export default defineConfig({
	resolve: {
		alias: {
			"~": fileURLToPath(new URL("./app", import.meta.url))
		}
	},
	test: {
		reporters: [
			"default", // Vitest's default reporter so that terminal output is still visible
			[
				"vitest-sonar-reporter",
				{
					outputFile: "sonar-report.xml",
					onWritePath(path: string) {
						// Prefix all paths with root directory
						return `frontend/${path}`;
					}
				}
			]
		],
		environment: "jsdom",
		pool: "vmThreads",
		setupFiles: ["./vitest.setup.ts"],
		coverage: {
			reporter: ["text", "lcov"]
		}
	}
});
