import { notifications } from '@mantine/notifications';
import { t } from 'i18next';

class NotificationsService {
	/**
	 * Shows a success notification with the given message.
	 * @param message The message to display in the notification.
	 */
	success(message: string) {
		this.showNotification('success', message);
	}

	/**
	 * Shows a warning notification with the given message.
	 * @param message The message to display in the notification.
	 */
	warning(message: string) {
		this.showNotification('warning', message);
	}

	/**
	 * Shows an error notification with the given message.
	 * @param message The message to display in the notification.
	 */
	error(message: string) {
		this.showNotification('error', message);
	}

	private showNotification(
		type: 'success' | 'warning' | 'error',
		message: string
	) {
		notifications.show({
			color: t(($) => $.notifications[type].color),
			title: t(($) => $.notifications[type].title),
			withBorder: true,
			message,
		});
	}
}

export default new NotificationsService();
