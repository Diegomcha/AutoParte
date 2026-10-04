import { reactRouter } from "@react-router/dev/vite";
import { sentryReactRouter } from "@sentry/react-router";
import { defineConfig } from "vite";
import devTools from "vite-plugin-devtools-json";

export default defineConfig((config) => ({
	plugins: [
		devTools(),
		reactRouter(),
		sentryReactRouter(
			{
				org: "diegomcha",
				project: "autoparte-front",
				telemetry: false
			},
			config
		)
	],
	resolve: {
		tsconfigPaths: true
	},
	server: {
		proxy: {
			"/api": {
				target: "http://localhost:8080",
				changeOrigin: true,
				proxyTimeout: 5000
			}
		}
	},
	build: {
		rollupOptions: {
			output: {
				manualChunks(id) {
					if (id.includes("node_modules")) {
						if (id.includes("@mantine") || id.includes("mantine-datatable"))
							return "vendor-mantine";

						if (id.includes("@phosphor-icons")) return "vendor-icons";

						if (id.includes("@tanstack")) return "vendor-tanstack";

						if (id.includes("@sentry")) return "vendor-sentry";

						if (id.includes("@zxcvbn-ts")) return "vendor-zxcvbn";

						if (id.includes("@react-pdf")) return "vendor-pdf";
					}
				}
			}
		}
	}
}));
