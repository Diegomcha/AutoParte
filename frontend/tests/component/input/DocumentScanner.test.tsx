import { cleanup, fireEvent, screen, waitFor } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";

import DocumentScanner from "~/component/input/DocumentScanner";

import { renderComponent } from "../../test-utils";

afterEach(cleanup);

test("renders camera controls", () => {
	renderComponent(<DocumentScanner onBack={vi.fn()} onScanSuccess={vi.fn()} />);
	expect(screen.getByTestId("camera")).toBeInTheDocument();
});

test("shows a retry state when camera access fails", () => {
	renderComponent(<DocumentScanner onBack={vi.fn()} onScanSuccess={vi.fn()} />);

	const cameraError = screen.getAllByTestId("camera-error")[0];
	if (!cameraError) throw new Error("Camera error control was not rendered");
	fireEvent.click(cameraError);

	expect(
		screen.getByRole("button", { name: /reintentar/i })
	).toBeInTheDocument();
});

test("reports when a scan captures no frame", async () => {
	renderComponent(<DocumentScanner onBack={vi.fn()} onScanSuccess={vi.fn()} />);

	fireEvent.click(screen.getByRole("button", { name: /escanear/i }));

	await waitFor(() => {
		expect(
			screen.getByText(/no se detectó ningún documento/i)
		).toBeInTheDocument();
	});
});

test("calls the back callback", () => {
	const onBack = vi.fn();
	renderComponent(<DocumentScanner onBack={onBack} onScanSuccess={vi.fn()} />);

	fireEvent.click(screen.getByRole("button", { name: /volver/i }));

	expect(onBack).toHaveBeenCalled();
});
