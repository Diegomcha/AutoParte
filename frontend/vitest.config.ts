import { defineConfig } from "vitest/config";

export default defineConfig({
	test: {
		// environment: "jsdom", // TODO: This breaks testing
		setupFiles: ["./vitest.setup.ts"],
		coverage: {
			reporter: ["text", "lcov"]
		}
	}
});
