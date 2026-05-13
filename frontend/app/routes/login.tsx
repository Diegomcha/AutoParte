import {
	Button,
	Checkbox,
	Paper,
	PasswordInput,
	TextInput,
	Title,
} from '@mantine/core';
import classes from '~/style/login.module.css';

export default function LoginPage() {
	return (
		<main className={classes.wrapper}>
			<Paper className={classes.form}>
				{/* TODO: Replace by logo */}
				<Title order={1} className={classes.title}>
					AutoParte
				</Title>

				<TextInput
					label="Email address"
					placeholder="hello@email.com"
					size="md"
					radius="md"
				/>
				<PasswordInput
					label="Password"
					placeholder="Your password"
					mt="md"
					size="md"
					radius="md"
				/>
				<Checkbox label="Keep me logged in" mt="xl" size="md" />
				<Button fullWidth mt="xl" size="md" radius="md">
					Login
				</Button>
			</Paper>
		</main>
	);
}
