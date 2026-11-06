import { screen } from "@testing-library/react"; // or whichever testing library utility you use
import { expect, test } from "vitest";

import JSONViewer from "~/components/JSONViewer";

import { renderComponent } from "../test-utils";

test("renders the JSON viewer with provided values", () => {
	renderComponent(<JSONViewer value={{ enabled: true }} />);

	// Assert that the text or key/value pair actually appears in the DOM
	expect(screen.getByText(/enabled/i)).toBeInTheDocument();
	expect(screen.getByText(/true/i)).toBeInTheDocument();
});
