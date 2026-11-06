import { cleanup, fireEvent, screen, waitFor } from "@testing-library/react";
import { afterEach, expect, test, vi } from "vitest";

import AdminDeleteModal from "~/components/admin/AdminDeleteModal";

import { renderComponent } from "../../test-utils";

afterEach(cleanup);

test("deletes selected records and closes", async () => {
	const setSelected = vi.fn();
	const setOpen = vi.fn();
	const mutationFn = vi.fn().mockResolvedValue(undefined);
	renderComponent(
		<AdminDeleteModal
			mutation={{ mutationFn }}
			selected={[{ id: "1" }]}
			setSelected={setSelected}
			deleteModalOpen
			setDeleteModalOpen={setOpen}
			messages={{
				title: "Delete",
				description: (count) => `${count.toString()} item`
			}}
		/>
	);
	const buttons = screen.getAllByRole("button");
	const deleteButton = buttons[buttons.length - 1];
	if (!deleteButton) throw new Error("Delete button was not rendered");
	fireEvent.click(deleteButton);
	await waitFor(() => {
		expect(setSelected).toHaveBeenCalledWith([]);
		expect(setOpen).toHaveBeenCalledWith(false);
	});
});

test("cancels without deleting selected records", () => {
	const setOpen = vi.fn();
	const mutationFn = vi.fn();
	renderComponent(
		<AdminDeleteModal
			mutation={{ mutationFn }}
			selected={[{ id: "1" }]}
			setSelected={vi.fn()}
			deleteModalOpen
			setDeleteModalOpen={setOpen}
			messages={{
				title: "Delete",
				description: (count) => `${count.toString()} item`
			}}
		/>
	);

	fireEvent.click(screen.getByRole("button", { name: /cancelar/i }));

	expect(setOpen).toHaveBeenCalledWith(false);
	expect(mutationFn).not.toHaveBeenCalled();
});
