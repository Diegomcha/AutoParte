import { screen } from "@testing-library/react";
import { expect, test, vi } from "vitest";

import CheckInPersonSelector from "~/component/checkIn/CheckInPersonSelector";

import { renderComponent } from "../../test-utils";

test("creates a step for each person", () => {
	renderComponent(
		<CheckInPersonSelector
			numberOfPeople={2}
			people={[]}
			activePersonIndex={0}
			setActivePersonIndex={vi.fn()}
		/>
	);
	expect(screen.getByText("Viajero 1")).toBeInTheDocument();
	expect(screen.getByText("Viajero 2")).toBeInTheDocument();
});
