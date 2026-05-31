import {
	Button,
	Code,
	Modal,
	Text,
	TextInput,
	useModalsStack,
} from '@mantine/core';
import { isEmail, isNotEmpty, useForm } from '@mantine/form';
import api from '~/api';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router';
import type {
	EmployeeDtoCreate,
	EmployeeDtoCreatedResponse,
	ProblemDetail,
} from '~/@types/api';

export default function NewEmployee() {
	const navigate = useNavigate();
	const { t } = useTranslation();

	const stack = useModalsStack(['new', 'created']);
	const form = useForm({
		initialValues: {
			name: '',
			surname: '',
			email: '',
		},
		validate: {
			name: isNotEmpty(t(($) => $.employees.new.form.errors.noName)),
			surname: isNotEmpty(t(($) => $.employees.new.form.errors.noSurname)),
			email: isEmail(t(($) => $.employees.new.form.errors.invalidEmail)),
		},
	});

	const [created, setCreated] = useState<EmployeeDtoCreatedResponse>();

	const createEmployee = async (values: EmployeeDtoCreate) => {
		const { data, error, response } = await api.POST('/api/employees', {
			body: values,
		});

		if (!response.ok) {
			if (response.status === 409) {
				form.setFieldError(
					'email',
					t(($) => $.employees.new.form.errors.emailInUse)
				);
				return;
			}
			throw new Error((error as ProblemDetail).detail);
		}

		setCreated(data);
		stack.open('created');
	};

	return (
		<Modal.Stack>
			<Modal
				{...stack.register('new')}
				opened
				onClose={() => void navigate('/employees')}
				title={t(($) => $.employees.new.title)}
			>
				<form onSubmit={form.onSubmit(createEmployee)}>
					<TextInput
						key={form.key('name')}
						name="name"
						label={t(($) => $.employees.new.form.name)}
						{...form.getInputProps('name')}
					/>
					<TextInput
						key={form.key('surname')}
						name="surname"
						label={t(($) => $.employees.new.form.surname)}
						{...form.getInputProps('surname')}
					/>
					<TextInput
						key={form.key('email')}
						name="email"
						label={t(($) => $.employees.new.form.email)}
						{...form.getInputProps('email')}
					/>

					<Button type="submit">{t(($) => $.employees.new.form.submit)}</Button>
				</form>
			</Modal>
			{created && (
				<Modal
					{...stack.register('created')}
					onClose={() => void navigate('/employees')}
					title={t(($) => $.employees.new.created.title)}
				>
					<Text>{t(($) => $.employees.new.created.description)}</Text>
					<Text>{t(($) => $.employees.new.created.credentials.email)}</Text>
					<Code block>{created.email}</Code>
					<Text>{t(($) => $.employees.new.created.credentials.password)}</Text>
					<Code block>{created.password}</Code>
					<Text>{t(($) => $.employees.new.created.warning)}</Text>
					<Link to={`/employees/${created.id}`}>
						<Button>{t(($) => $.employees.new.created.view)}</Button>
					</Link>
				</Modal>
			)}
		</Modal.Stack>
	);
}
