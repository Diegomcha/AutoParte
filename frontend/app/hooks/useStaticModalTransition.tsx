import { useCallback, useEffect, useState } from 'react';

export const EXIT_DURATION_DEFAULT = 250; // Default mantine exit duration

/**
 * Manages modal open/close states with mount-frame opening
 * and delayed closing for exit transitions.
 * @param onClose Callback executed after exit animation completes (e.g., navigate('/admin/employees')).
 * @returns An object containing the `opened` state and a `close` function to trigger the exit animation.
 */
export default function useStaticModalTransition(onClose: () => void) {
	const [opened, setOpened] = useState(false);

	// Auto-open on mount after first paint
	useEffect(() => {
		const timer = requestAnimationFrame(() => {
			setOpened(true);
		});
		return () => {
			cancelAnimationFrame(timer);
		};
	}, []);

	// Trigger exit animation before calling onClose
	const close = useCallback(() => {
		setOpened(false);
		const timer = setTimeout(() => {
			onClose();
		}, EXIT_DURATION_DEFAULT);

		return () => {
			clearTimeout(timer);
		};
	}, [EXIT_DURATION_DEFAULT, onClose]);

	return { opened, close };
}
