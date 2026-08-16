import { index, route } from '@react-router/dev/routes';
import type { RouteConfig } from '@react-router/dev/routes';

export default [
	route('/', './routes/index.tsx', [
		route(
			'/accommodations/:accommodationId/bookings/:bookingId',
			'./routes/bookings/index.tsx',
			[
				route('people', './routes/bookings/people/index.tsx', [
					route(':id/delete', './routes/bookings/people/delete.tsx'),
					route('new-address', './routes/bookings/people/newAddress.tsx'),
				]),
				route('confirm', './routes/bookings/confirm.tsx'),
				route('check-in', './routes/bookings/checkIn.tsx'),
				route(
					'request-self-check-in',
					'./routes/bookings/requestSelfCheckIn.tsx'
				),
				route('cancel', './routes/bookings/cancel.tsx'),
				route('delete', './routes/bookings/delete.tsx'),
			]
		),
	]),
	// TODO:
	// route('check-in/:accommodationId/:bookingId', './routes/check-in.tsx'),
	route('auth', './routes/auth/layout.tsx', [
		index('./routes/auth/index.tsx'),
		route('login', './routes/auth/login.tsx'),
		route('logout', './routes/auth/logout.tsx'),
	]),
	route('admin', './routes/admin/layout.tsx', [
		index('./routes/admin/index.tsx'),
		route('configuration', './routes/admin/configuration.tsx'),
		route('employees', './routes/admin/employees/index.tsx', [
			route('new', './routes/admin/employees/new.tsx'),
			route(':id', './routes/admin/employees/view.tsx'),
			route(':id/edit', './routes/admin/employees/edit.tsx'),
			route(':id/delete', './routes/admin/employees/delete.tsx'),
			route(':id/reset-password', './routes/admin/employees/resetPassword.tsx'),
		]),
		route('accommodations', './routes/admin/accommodations/index.tsx', [
			route('new', './routes/admin/accommodations/new.tsx'),
			route(':id', './routes/admin/accommodations/view.tsx'),
			route(':id/edit', './routes/admin/accommodations/edit.tsx'),
			route(':id/delete', './routes/admin/accommodations/delete.tsx'),
		]),
		// TODO: Logs routes
	]),
] satisfies RouteConfig;
