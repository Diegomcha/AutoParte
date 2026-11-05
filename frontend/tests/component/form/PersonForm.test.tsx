import { cleanup, fireEvent, screen } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";

import PersonForm from "~/component/form/PersonForm";

import { renderComponent } from "../../test-utils";

afterEach(cleanup);

test("renders required person fields independently from API data", () => {
	renderComponent(
		<PersonForm
			accommodationId="accommodation-1"
			bookingId="booking-1"
			onCreatedPerson={vi.fn()}
		/>
	);

	expect(screen.getByLabelText(/nombre/i)).toBeInTheDocument();
	expect(screen.getByLabelText(/primer apellido/i)).toBeInTheDocument();
	expect(screen.getByRole("button", { name: /añadir/i })).toBeDisabled();
});

test("validates incomplete contact data on submit", async () => {
	renderComponent(
		<PersonForm
			accommodationId="accommodation-1"
			bookingId="booking-1"
			onCreatedPerson={vi.fn()}
		/>
	);

	fireEvent.change(screen.getByLabelText(/nombre/i), {
		target: { value: "Ada" }
	});
	fireEvent.click(screen.getByRole("button", { name: /añadir/i }));

	expect(
		await screen.findByText("Al menos un método de contacto es obligatorio")
	).toBeInTheDocument();
});

test("supports switching from document scanning back to manual entry", () => {
	renderComponent(
		<PersonForm
			accommodationId="accommodation-1"
			bookingId="booking-1"
			defaultMode="scanning"
		/>
	);

	expect(screen.getByTestId("camera")).toBeInTheDocument();
	fireEvent.click(screen.getByRole("button", { name: /volver/i }));

	expect(screen.getByLabelText(/nombre/i)).toBeInTheDocument();
});
