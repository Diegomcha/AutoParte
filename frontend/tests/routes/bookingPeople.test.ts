import React from "react";

import { fireEvent, screen } from "@testing-library/react";
import { expect, test, vi } from "vitest";

import NewPerson, { clientLoader } from "~/routes/bookings/people/new";
import { queryClient } from "~/services/Api";

import { renderComponent } from "../test-utils";

const navigate = vi.fn();

vi.mock("react-router", async () => ({
	...(await vi.importActual<typeof import("react-router")>("react-router")),
	useNavigate: () => {
		return navigate;
	}
}));

vi.mock("~/components/form/PersonForm", () => ({
	default: (props: { onCreatedPerson: (id: string) => void }) =>
		React.createElement(
			"button",
			{
				onClick: () => {
					props.onCreatedPerson("person-1");
				}
			},
			"create person"
		)
}));

const validParams = {
	accommodationId: "123e4567-e89b-12d3-a456-426614174000",
	bookingId: "123e4567-e89b-12d3-a456-426614174001"
};

test("rejects invalid booking route parameters", async () => {
	await expect(
		clientLoader({
			params: { accommodationId: "bad", bookingId: validParams.bookingId }
		} as never)
	).rejects.toMatchObject({ status: 400 });
});

test("rejects adding a person to a full booking", async () => {
	const query = vi.spyOn(queryClient, "query");
	query
		.mockResolvedValueOnce({ canBeModified: true, numberOfPeople: 1 })
		.mockResolvedValueOnce([{ id: "person-1" }]);

	await expect(clientLoader({ params: validParams } as never)).rejects.toThrow(
		"Booking cannot be modified."
	);
});

test("allows adding a person to a modifiable booking", async () => {
	const query = vi.spyOn(queryClient, "query");
	query
		.mockResolvedValueOnce({ canBeModified: true, numberOfPeople: 2 })
		.mockResolvedValueOnce([{ id: "person-1" }]);

	await expect(
		clientLoader({ params: validParams } as never)
	).resolves.toBeUndefined();
});

test("navigates to the created person", () => {
	renderComponent(
		React.createElement(NewPerson, {
			params: { accommodationId: "a1", bookingId: "b1" }
		} as never)
	);

	fireEvent.click(screen.getByRole("button", { name: "create person" }));

	expect(navigate).toHaveBeenCalledWith("../person-1");
});
