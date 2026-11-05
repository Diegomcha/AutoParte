import { MantineProvider } from "@mantine/core";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";

import type { ReactNode } from "react";

export function renderComponent(ui: ReactNode) {
	return render(
		<QueryClientProvider client={new QueryClient()}>
			<MantineProvider>{ui}</MantineProvider>
		</QueryClientProvider>
	);
}
