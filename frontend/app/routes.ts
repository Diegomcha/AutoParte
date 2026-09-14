import { index, route } from "@react-router/dev/routes";

import type { RouteConfig } from "@react-router/dev/routes";

export default [
	// TODO: REMOVE!
	route("test", "./routes/test.tsx"),
	route("/", "./routes/index.tsx", [
		route(
			"/accommodations/:accommodationId/bookings/:bookingId",
			"./routes/bookings/index.tsx",
			[
				route("people", "./routes/bookings/people/layout.tsx", [
					index("./routes/bookings/people/index.tsx"),
					route("new", "./routes/bookings/people/new.tsx"),
					route(":id", "./routes/bookings/people/edit.tsx", [
						route("delete", "./routes/bookings/people/delete.tsx")
					])
				]),
				route("confirm", "./routes/bookings/confirm.tsx"),
				route("check-in", "./routes/bookings/checkIn.tsx"),
				route(
					"request-self-check-in",
					"./routes/bookings/requestSelfCheckIn.tsx"
				),
				route("cancel", "./routes/bookings/cancel.tsx"),
				route("delete", "./routes/bookings/delete.tsx")
			]
		)
	]),
	route("check-in/:accommodationId/:bookingId", "./routes/checkIn/layout.tsx", [
		index("./routes/checkIn/index.tsx"),
		route("verify-booking", "./routes/checkIn/verifyBooking.tsx"),
		route("input-guest-details", "./routes/checkIn/inputGuestDetails.tsx"),
		route("send", "./routes/checkIn/send.tsx")
	]),
	route("auth", "./routes/auth/layout.tsx", [
		index("./routes/auth/index.tsx"),
		route("login", "./routes/auth/login.tsx"),
		route("logout", "./routes/auth/logout.tsx")
	]),
	route("admin", "./routes/admin/layout.tsx", [
		index("./routes/admin/index.tsx"),
		route("configuration", "./routes/admin/configuration.tsx"),
		route("employees", "./routes/admin/employees/index.tsx", [
			route("new", "./routes/admin/employees/new.tsx"),
			route(":id", "./routes/admin/employees/view.tsx"),
			route(":id/edit", "./routes/admin/employees/edit.tsx"),
			route(":id/delete", "./routes/admin/employees/delete.tsx"),
			route(":id/reset-password", "./routes/admin/employees/resetPassword.tsx")
		]),
		route("accommodations", "./routes/admin/accommodations/index.tsx", [
			route("new", "./routes/admin/accommodations/new.tsx"),
			route(":id", "./routes/admin/accommodations/view.tsx"),
			route(":id/edit", "./routes/admin/accommodations/edit.tsx"),
			route(":id/delete", "./routes/admin/accommodations/delete.tsx")
		])
		// TODO: Logs routes
	])
] satisfies RouteConfig;
