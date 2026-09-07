import { useCallback, useEffect } from "react";

import { useModalsStack } from "@mantine/core";

import { EXIT_DURATION_DEFAULT } from "./useStaticModalTransition";

/**
 * Manages stack modal open/close states with mount-frame opening
 * and delayed closing for exit transitions.
 * @param stack The modals stack returned by `useModalsStack`.
 * @param initialModal The initial modal to open on mount.
 * @param options Options for exit duration and onClose callback.
 * @returns An object containing a `close` function to trigger the exit animation.
 */
export default function useStaticModalStackTransition<T extends string>(
	stack: ReturnType<typeof useModalsStack<T>>,
	initialModal: T,
	onClose: () => void
) {
	// Auto-open on mount after first paint
	useEffect(() => {
		const timer = requestAnimationFrame(() => {
			stack.open(initialModal);
		});
		return () => {
			cancelAnimationFrame(timer);
		};
	}, []);

	// Trigger exit animation before calling onClose
	const close = useCallback(() => {
		stack.closeAll();
		const timer = setTimeout(() => {
			onClose();
		}, EXIT_DURATION_DEFAULT);

		return () => {
			clearTimeout(timer);
		};
	}, [EXIT_DURATION_DEFAULT, onClose]);

	return { close };
}
