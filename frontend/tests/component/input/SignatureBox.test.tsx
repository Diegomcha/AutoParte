import React from "react";

import { cleanup, fireEvent, screen } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";

import SignatureBox from "~/component/input/SignatureBox";

import { renderComponent } from "../../test-utils";

afterEach(cleanup);

vi.mock("@uiw/react-signature", () => ({
	default: React.forwardRef(function SignatureMock(
		props: {
			onPointer?: (points: number[][]) => void;
			readonly?: boolean;
		},
		ref
	) {
		React.useImperativeHandle(ref, () => ({ clear: vi.fn() }));
		return React.createElement(
			"button",
			{
				disabled: props.readonly,
				onClick: () => props.onPointer?.([[10, 20]])
			},
			"stroke"
		);
	})
}));

vi.mock("@tanstack/react-query", async () => ({
	...(await vi.importActual<typeof import("@tanstack/react-query")>(
		"@tanstack/react-query"
	)),
	useSuspenseQuery: () => ({ data: undefined }),
	useMutation: () => ({
		isPending: false,
		mutate: (_data: unknown, options?: { onSuccess?: () => void }) =>
			options?.onSuccess?.()
	})
}));

test("renders signature canvas", () => {
	renderComponent(
		<SignatureBox
			accommodationId="a1"
			bookingId="b1"
			person={
				{
					id: "p1",
					personalInfo: { name: "Ada", firstSurname: "Lovelace" }
				} as never
			}
		/>
	);
	expect(screen.getByRole("button", { name: "stroke" })).toBeInTheDocument();
});

test("hides editing controls in photo mode", () => {
	renderComponent(
		<SignatureBox
			mode="photo"
			accommodationId="a1"
			bookingId="b1"
			person={
				{
					id: "p1",
					personalInfo: { name: "Ada", firstSurname: "Lovelace" }
				} as never
			}
		/>
	);

	expect(
		screen.queryByRole("button", { name: /firmar/i })
	).not.toBeInTheDocument();
});

test("renders read-only signature details", () => {
	renderComponent(
		<SignatureBox
			mode="readOnly"
			accommodationId="a1"
			bookingId="b1"
			person={
				{
					id: "p1",
					personalInfo: { name: "Ada", firstSurname: "Lovelace" }
				} as never
			}
		/>
	);

	expect(screen.getByText(/firmado electrónicamente/i)).toBeInTheDocument();
});

test("clears and submits an interactive signature", () => {
	const onSignature = vi.fn();
	renderComponent(
		<SignatureBox
			accommodationId="a1"
			bookingId="b1"
			onSignature={onSignature}
			person={
				{
					id: "p1",
					personalInfo: { name: "Ada", firstSurname: "Lovelace" }
				} as never
			}
		/>
	);

	fireEvent.click(screen.getByRole("button", { name: "stroke" }));
	fireEvent.click(screen.getByRole("button", { name: /firmar/i }));

	expect(onSignature).toHaveBeenCalled();
});
