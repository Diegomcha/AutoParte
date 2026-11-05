// Import custom DOM testing matchers from Testing Library for Vitest assertions
import "@testing-library/jest-dom/vitest";

import React from "react";

import { vi } from "vitest";

// Initialize application internationalization (i18n) settings for tests
import "~/i18n";

/**
 * Polyfill window.matchMedia for components that check media queries or screen sizes (e.g., Mantine UI)
 */
Object.defineProperty(window, "matchMedia", {
	writable: true,
	value: vi.fn().mockImplementation((query: string) => ({
		matches: false,
		media: query,
		onchange: null,
		addListener: vi.fn(),
		removeListener: vi.fn(),
		addEventListener: vi.fn(),
		removeEventListener: vi.fn(),
		dispatchEvent: vi.fn()
	}))
});

/**
 * Mock ResizeObserver globally to prevent errors in components tracking element dimensions
 */
class ResizeObserverMock {
	observe(): void {
		return;
	}
	unobserve(): void {
		return;
	}
	disconnect(): void {
		return;
	}
}

Object.assign(globalThis, { ResizeObserver: ResizeObserverMock });

/**
 * Global mock for @tanstack/react-query to return pre-configured stubs
 * for mutations, queries, and suspense hooks without requiring a real Provider or network calls.
 */
vi.mock("@tanstack/react-query", async () => {
	const actual = await vi.importActual<typeof import("@tanstack/react-query")>(
		"@tanstack/react-query"
	);
	return {
		...actual,
		useSuspenseQuery: () => ({ data: [] }),
		useSuspenseQueries: (options: unknown) => {
			if (
				typeof options === "object" &&
				options !== null &&
				"combine" in options
			) {
				const queries =
					"queries" in options && Array.isArray(options.queries)
						? options.queries
						: [];
				const combine = (
					options as {
						combine?: (results: { data: unknown }[]) => unknown;
					}
				).combine;
				if (combine) return combine(queries.map(() => ({ data: [] })));
			}
			return [];
		},
		useQueries: () => [
			{ data: {}, isLoading: false },
			{ data: {}, isLoading: false }
		]
	};
});

/**
 * Stub @mantine/schedule component into a lightweight interactive button list for testing
 */
vi.mock("@mantine/schedule", () => ({
	ResourcesSchedule: (props: {
		events?: { id: string }[];
		onEventClick?: (event: { id: string; resourceId: string }) => void;
	}) =>
		React.createElement(
			"div",
			null,
			props.events?.map((event) =>
				React.createElement(
					"button",
					{
						key: event.id,
						onClick: () => props.onEventClick?.({ ...event, resourceId: "a1" })
					},
					event.id
				)
			)
		)
}));

/**
 * Stub mantine-datatable to simplify table rendering and row interaction in tests
 */
vi.mock("mantine-datatable", () => ({
	DataTable: (props: {
		records?: { id: string }[];
		onRowClick?: (event: { record: { id: string } }) => void;
	}) =>
		React.createElement(
			"div",
			null,
			props.records?.map((record) =>
				React.createElement(
					"button",
					{
						key: record.id,
						onClick: () => props.onRowClick?.({ record })
					},
					record.id
				)
			)
		),
	useDataTableColumns: ({ columns }: { columns: unknown[] }) => ({
		effectiveColumns: columns
	})
}));

/**
 * Replace the electronic signature canvas component with a lightweight test element
 */
vi.mock("@uiw/react-signature", () => ({
	default: () =>
		React.createElement("div", { "data-testid": "signature-canvas" })
}));

/**
 * Replace the web camera component with a test placeholder element
 */
vi.mock("@shivantra/react-web-camera", () => ({
	default: (props: { onError?: (error: Error) => void }) =>
		React.createElement(
			"div",
			{ "data-testid": "camera" },
			React.createElement(
				"button",
				{
					"data-testid": "camera-error",
					onClick: () => props.onError?.(new Error("camera unavailable"))
				},
				"camera error"
			)
		)
}));

/**
 * Mock the core API client using a JavaScript Proxy to safely handle any chained method calls
 */
vi.mock("~/services/Api", () => ({
	queryClient: {
		query: vi.fn(() => Promise.resolve(null)),
		invalidateQueries: vi.fn(() => Promise.resolve()),
		removeQueries: vi.fn()
	},
	executeMutation: vi.fn(() => Promise.resolve(undefined)),
	queryFactory: (() => {
		const factory = new Proxy(() => ({}), {
			get: () => factory,
			apply: () => ({})
		});
		return factory;
	})()
}));

/**
 * Mock CountryService helper utility for deterministic testing
 */
vi.mock("~/services/CountryService", () => ({
	default: {
		getName: (country: string) => country,
		getFlag: () => "flag",
		getPhoneCountryCode: () => undefined
	}
}));

/**
 * Mock PasswordService utility to return predictable strength metrics
 */
vi.mock("~/services/PasswordService", () => ({
	default: { check: () => ({ score: 3, feedback: {} }) }
}));
