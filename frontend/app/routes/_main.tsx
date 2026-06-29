import {
	AppShell,
	Button,
	Group,
	Menu,
	Select,
	Text,
	Title,
} from '@mantine/core';
import { CaretRightIcon, UserCircleIcon } from '@phosphor-icons/react';
import { useSuspenseQuery } from '@tanstack/react-query';
import api, { throwErrors } from '~/api';
import AuthService from '~/services/AuthService';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, Outlet, useNavigate } from 'react-router';
import type { Route } from './+types/_main';

export async function clientLoader({ request }: Route.ClientLoaderArgs) {
	// Ensure the user is authenticated before allowing access to any protected routes.
	// This should only be required in dev mode, as in production the server will handle route protection.
	if (!(await AuthService.isAuthenticated()))
		return AuthService.getLoginRedirection(request);

	return {
		account: await AuthService.getLoggedInUser(),
		isAdmin: await AuthService.isAdmin(),
	};
}

export default function ProtectedLayout({ loaderData }: Route.ComponentProps) {
	const { account, isAdmin } = loaderData;

	const { t } = useTranslation();
	const navigate = useNavigate();

	const { data: accommodations } = useSuspenseQuery({
		queryKey: ['accommodations'],
		queryFn: async () =>
			throwErrors(
				await api.GET('/api/accommodations', {
					params: { query: { page: 0, size: 0 } },
				})
			).content,
	});

	const [accountMenuOpened, setAccountMenuOpened] = useState(false);

	return (
		<AppShell header={{ height: 60 }} padding="md">
			<AppShell.Header px="md">
				<Group justify="space-between" className="h-full">
					<Title size="h2">{t(($) => $.meta.name)}</Title>
					<Select
						w={250}
						placeholder={t(($) => $.header.accommodationSelector.placeholder)}
						data={accommodations?.map((a) => ({
							value: a.id,
							label: a.name,
						}))}
						nothingFoundMessage={t(
							($) => $.header.accommodationSelector.nothingFound
						)}
						allowDeselect={false}
						onChange={(value) => {
							if (value) void navigate(`/${value}`);
						}}
					/>
					<Menu
						shadow="xs"
						opened={accountMenuOpened}
						onChange={setAccountMenuOpened}
					>
						<Menu.Target>
							<Button
								variant="outline"
								leftSection={<Text>{<UserCircleIcon />}</Text>}
								rightSection={
									<CaretRightIcon
										className={`${accountMenuOpened ? 'rotate-90' : ''} transition-transform`}
									/>
								}
							>
								{account?.username}
							</Button>
						</Menu.Target>

						<Menu.Dropdown>
							{isAdmin && (
								<>
									<Menu.Item component={Link} to="/admin">
										{t(($) => $.header.admin)}
									</Menu.Item>
									<Menu.Divider />
								</>
							)}
							<Menu.Item color="red" component={Link} to="/auth/logout">
								{t(($) => $.header.logout)}
							</Menu.Item>
						</Menu.Dropdown>
					</Menu>
				</Group>
			</AppShell.Header>
			<AppShell.Main>
				<Outlet />
			</AppShell.Main>
		</AppShell>
	);
}
