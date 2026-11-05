import { screen } from "@testing-library/react";
import { expect, test } from "vitest";

import CommunicationTimeline from "~/component/communicationTimeline/CommunicationTimeline";

import { renderComponent } from "../../test-utils";

test("renders the communication timeline", () => {
	renderComponent(
		<CommunicationTimeline
			accommodationId="a1"
			bookingId="b1"
			createdAt={new Date()}
		/>
	);
	expect(screen.getByText("Creación")).toBeInTheDocument();
});
