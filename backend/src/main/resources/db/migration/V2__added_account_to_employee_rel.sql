ALTER TABLE account
    ADD employee_id UUID;

ALTER TABLE account
    ADD CONSTRAINT account_employee_id_key UNIQUE (employee_id);

ALTER TABLE account
    ADD CONSTRAINT fk1kec5bwba2rl0j8garlarwe3d FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE NO ACTION;