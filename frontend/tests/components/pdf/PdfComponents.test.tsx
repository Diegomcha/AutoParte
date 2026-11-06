import React from "react";

import { screen } from "@testing-library/react";
import { expect, test, vi } from "vitest";

import Attribute from "~/components/pdf/components/Attribute";
import {
	Table,
	TableCell,
	TableHeader,
	TableRow
} from "~/components/pdf/components/Table";

import { renderComponent } from "../../test-utils";

vi.mock("@react-pdf/renderer", () => ({
	StyleSheet: {
		create: <T,>(styles: T) => styles
	},
	Text: ({ children }: { children?: React.ReactNode }) =>
		React.createElement("span", null, children),
	View: ({ children }: { children?: React.ReactNode }) =>
		React.createElement("div", null, children)
}));

test("renders a PDF attribute through the renderer boundary", () => {
	renderComponent(<Attribute title="Name" value="Ada Lovelace" />);

	expect(screen.getByText("Name")).toBeInTheDocument();
	expect(screen.getByText("Ada Lovelace")).toBeInTheDocument();
});

test("renders PDF table headers, cells, and rows", () => {
	renderComponent(
		<Table>
			<TableRow>
				<TableHeader>Header</TableHeader>
				<TableCell>Cell</TableCell>
			</TableRow>
		</Table>
	);

	expect(screen.getByText("Header")).toBeInTheDocument();
	expect(screen.getByText("Cell")).toBeInTheDocument();
});
