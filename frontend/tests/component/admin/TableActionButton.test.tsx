import { screen } from "@testing-library/react";
import { expect, test } from "vitest";

import TableActionButton from "~/component/admin/TableActionButton";

import { renderComponent } from "../../test-utils";

test("renders its action content", () => {
	renderComponent(<TableActionButton tooltip="Edit">Edit</TableActionButton>);
	expect(screen.getByText("Edit")).toBeInTheDocument();
});
