import { Timeline } from "@mantine/core";

import { screen } from "@testing-library/react";
import { expect, test } from "vitest";

import CommunicationTimelineItem from "~/component/communicationTimeline/CommunicationTimelineItem";

import { renderComponent } from "../../test-utils";

test("renders communication status and error details", () => {
	renderComponent(
		<Timeline>
			<CommunicationTimelineItem
				communication={
					{
						status: "FAILED",
						type: "BOOKING",
						error: "Delivery failed",
						sentTimestamp: null
					} as never
				}
			/>
		</Timeline>
	);

	expect(screen.getByText("Delivery failed")).toBeInTheDocument();
	expect(screen.getByText("Confirmación")).toBeInTheDocument();
});
