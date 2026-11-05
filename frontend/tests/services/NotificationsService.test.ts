import { notifications } from "@mantine/notifications";

import { expect, test, vi } from "vitest";

import NotificationsService from "~/services/NotificationsService";

test("maps success notifications to the frontend notification adapter", () => {
	const show = vi.spyOn(notifications, "show").mockImplementation(() => "");

	NotificationsService.success("Saved");

	expect(show).toHaveBeenCalledWith(
		expect.objectContaining({
			message: "Saved",
			withBorder: true
		})
	);

	show.mockRestore();
});
