import { Scroller, Stepper } from "@mantine/core";

import { useTranslation } from "react-i18next";

import type { PersonDtoResponse } from "~/@types/api";

export default function CheckInPersonSelector({
	numberOfPeople,
	people,
	activePersonIndex,
	setActivePersonIndex,
	...props
}: Omit<
	React.ComponentProps<typeof Stepper>,
	"children" | "active" | "onStepClick"
> & {
	numberOfPeople: number;
	people: PersonDtoResponse[];
	activePersonIndex: number;
	setActivePersonIndex: (index: number) => void;
}) {
	const { t } = useTranslation("components", {
		keyPrefix: "checkIn.personSelector"
	});

	const personNames = Array.from({ length: numberOfPeople }, (_, index) => {
		const person = people.at(index);
		return [
			index,
			person
				? // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
					`${person.personalInfo.name} ${person.personalInfo.firstSurname.at(0)!.toUpperCase()}.`
				: t(($) => $.travellerPlaceholder, { number: index + 1 })
		];
	});

	return (
		<Scroller
			styles={{
				content: {
					minWidth: "100%"
				}
			}}
		>
			<Stepper
				active={activePersonIndex}
				onStepClick={setActivePersonIndex}
				size="sm"
				wrap={false}
				labelPosition="bottom"
				px="lg"
				miw="100%"
				py={1}
				styles={{
					separator: {
						minWidth: "1em"
					},
					stepLabel: {
						textWrap: "nowrap"
					}
				}}
				{...props}
			>
				{personNames.map(([index, name]) => (
					<Stepper.Step key={index} label={name} />
				))}
			</Stepper>
		</Scroller>
	);
}
