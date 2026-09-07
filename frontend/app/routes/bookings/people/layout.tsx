import { ActionIcon, Modal, Scroller, Tabs, Text } from '@mantine/core';
import { PlusIcon, XIcon } from '@phosphor-icons/react';
import { useSuspenseQuery } from '@tanstack/react-query';
import useStaticModalTransition from '~/hooks/useStaticModalTransition';
import { queryFactory } from '~/services/Api';
import { useTranslation } from 'react-i18next';
import { Link, Outlet, useLocation, useNavigate } from 'react-router';
import { useBooking } from '..';
import type { Route } from './+types/layout';

export default function BookingPeople({
	params: { accommodationId, bookingId },
}: Route.ComponentProps) {
	const { t } = useTranslation();
	const navigate = useNavigate();
	const location = useLocation();

	const { opened, close } = useStaticModalTransition(() => void navigate('..'));
	const booking = useBooking();

	const { data: people } = useSuspenseQuery(
		queryFactory.accommodations.bookings.people.list(accommodationId, bookingId)
	);

	return (
		<Modal
			opened={opened}
			onClose={close}
			title={t(($) => $.people.title)}
			size="auto"
		>
			<Text size="xs" c="gray" mb={'sm'}>
				{t(($) => $.people.requirement)}
			</Text>
			{/* People switcher */}
			<Tabs mb="md" value={location.pathname} w={0} miw={'100%'}>
				<Tabs.List>
					<Scroller>
						{people.map((person) => (
							<Tabs.Tab
								key={person.id}
								value={`/accommodations/${accommodationId}/bookings/${bookingId}/people/${person.id}`}
								renderRoot={(props) => (
									<Link
										to={`/accommodations/${accommodationId}/bookings/${bookingId}/people/${person.id}`}
										{...props}
									/>
								)}
								styles={{
									tabLabel: {
										textWrap: 'nowrap',
									},
								}}
								rightSection={
									booking.canBeModified && (
										<ActionIcon
											size="xs"
											color="red"
											component={Link}
											to={`/accommodations/${accommodationId}/bookings/${bookingId}/people/${person.id}/delete`}
										>
											<XIcon />
										</ActionIcon>
									)
								}
							>
								{person.personalInfo.name}{' '}
								{person.personalInfo.firstSurname.at(0)?.toUpperCase()}.
							</Tabs.Tab>
						))}
						{booking.canBeModified &&
							booking.numberOfPeople > people.length && (
								<Tabs.Tab
									value={`/accommodations/${accommodationId}/bookings/${bookingId}/people/new`}
									renderRoot={(props) => (
										<Link
											to={`/accommodations/${accommodationId}/bookings/${bookingId}/people/new`}
											{...props}
										/>
									)}
								>
									<PlusIcon size={16} />
								</Tabs.Tab>
							)}
					</Scroller>
				</Tabs.List>
			</Tabs>
			<Outlet context={{ booking }} />
		</Modal>
	);
}
