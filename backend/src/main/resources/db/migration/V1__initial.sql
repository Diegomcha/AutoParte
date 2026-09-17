CREATE TABLE accommodation
(
    internet_connection BOOLEAN,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at          TIMESTAMP WITHOUT TIME ZONE,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version             BIGINT                      NOT NULL,
    id                  UUID                    NOT NULL,
    name                VARCHAR(255)                NOT NULL,
    ses_code            VARCHAR(255)                NOT NULL,
    CONSTRAINT accommodation_pkey PRIMARY KEY (id)
);

CREATE TABLE accommodation_employees
(
    accommodations_id UUID NOT NULL,
    employees_id      UUID NOT NULL,
    CONSTRAINT accommodation_employees_pkey PRIMARY KEY (accommodations_id, employees_id)
);

CREATE TABLE account
(
    enabled         BOOLEAN                     NOT NULL,
    requires_reset  BOOLEAN                     NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at      TIMESTAMP WITHOUT TIME ZONE,
    disabled_at     TIMESTAMP WITHOUT TIME ZONE,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version         BIGINT                      NOT NULL,
    id              UUID                    NOT NULL,
    hashed_password VARCHAR(255)                NOT NULL,
    username        VARCHAR(255)                NOT NULL,
    roles           VARCHAR(255)[]              NOT NULL,
    CONSTRAINT account_pkey PRIMARY KEY (id)
);

CREATE TABLE address
(
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version       BIGINT                      NOT NULL,
    id            UUID                    NOT NULL,
    dtype         VARCHAR(31)                 NOT NULL,
    address_line1 VARCHAR(255)                NOT NULL,
    address_line2 VARCHAR(255),
    country       VARCHAR(255)                NOT NULL,
    municipality  VARCHAR(255)                NOT NULL,
    postal_code   VARCHAR(255)                NOT NULL,
    CONSTRAINT address_pkey PRIMARY KEY (id)
);

CREATE TABLE booking
(
    internet_connection     BOOLEAN,
    number_of_people        INTEGER                     NOT NULL,
    number_of_rooms         INTEGER,
    self_check_in_requested BOOLEAN                     NOT NULL,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    start_time              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version                 BIGINT                      NOT NULL,
    accommodation_id        UUID                    NOT NULL,
    created_by_id           UUID,
    id                      UUID                    NOT NULL,
    last_modified_by_id     UUID,
    payment_id              UUID,
    CONSTRAINT booking_pkey PRIMARY KEY (id)
);

CREATE TABLE communication
(
    batch_order    INTEGER,
    status         SMALLINT                    NOT NULL,
    type           SMALLINT                    NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    sent_timestamp TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version        BIGINT                      NOT NULL,
    batch_id       UUID,
    booking_id     UUID                    NOT NULL,
    id             UUID                    NOT NULL,
    ses_id         UUID,
    dtype          VARCHAR(31)                 NOT NULL,
    error          VARCHAR(255),
    CONSTRAINT communication_pkey PRIMARY KEY (id)
);

CREATE TABLE configuration
(
    digital_signature_enabled BOOLEAN                     NOT NULL,
    logs_retention_days       INTEGER                     NOT NULL,
    manual_review_enabled     BOOLEAN                     NOT NULL,
    ses_credentials_valid     BOOLEAN                     NOT NULL,
    created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version                   BIGINT                      NOT NULL,
    id                        UUID                    NOT NULL,
    remember_me_key           VARCHAR(255)                NOT NULL,
    ses_landlord_code         VARCHAR(255),
    ses_password              VARCHAR(255),
    ses_username              VARCHAR(255),
    CONSTRAINT configuration_pkey PRIMARY KEY (id)
);

CREATE TABLE document
(
    type           SMALLINT                    NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version        BIGINT                      NOT NULL,
    id             UUID                    NOT NULL,
    dtype          VARCHAR(31)                 NOT NULL,
    number         VARCHAR(255)                NOT NULL,
    support_number VARCHAR(255),
    CONSTRAINT document_pkey PRIMARY KEY (id)
);

CREATE TABLE employee
(
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version    BIGINT                      NOT NULL,
    account_id UUID                    NOT NULL,
    id         UUID                    NOT NULL,
    name       VARCHAR(255)                NOT NULL,
    surname    VARCHAR(255)                NOT NULL,
    CONSTRAINT employee_pkey PRIMARY KEY (id)
);

CREATE TABLE payment
(
    type        SMALLINT                    NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    date        TIMESTAMP WITHOUT TIME ZONE,
    expiry_date TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version     BIGINT                      NOT NULL,
    id          UUID                    NOT NULL,
    dtype       VARCHAR(31)                 NOT NULL,
    holder      VARCHAR(255),
    mean        VARCHAR(255),
    CONSTRAINT payment_pkey PRIMARY KEY (id)
);

CREATE TABLE person
(
    gender         SMALLINT,
    relationship   SMALLINT,
    birth_date     TIMESTAMP WITHOUT TIME ZONE,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    signed_at      TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version        BIGINT                      NOT NULL,
    address_id     UUID,
    booking_id     UUID                    NOT NULL,
    document_id    UUID,
    id             UUID                    NOT NULL,
    email          VARCHAR(255),
    first_surname  VARCHAR(255),
    ip_address     VARCHAR(255),
    name           VARCHAR(255),
    nationality    VARCHAR(255),
    phone_number1  VARCHAR(255),
    phone_number2  VARCHAR(255),
    second_surname VARCHAR(255),
    user_agent     VARCHAR(255),
    paths          JSONB,
    CONSTRAINT person_pkey PRIMARY KEY (id)
);

CREATE TABLE security_event
(
    method         SMALLINT                    NOT NULL,
    type           SMALLINT                    NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    timestamp      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version        BIGINT                      NOT NULL,
    account_id     UUID,
    id             UUID                    NOT NULL,
    remote_address VARCHAR(255)                NOT NULL,
    CONSTRAINT security_event_pkey PRIMARY KEY (id)
);

ALTER TABLE booking
    ADD CONSTRAINT booking_payment_id_key UNIQUE (payment_id);

ALTER TABLE employee
    ADD CONSTRAINT employee_account_id_key UNIQUE (account_id);

ALTER TABLE person
    ADD CONSTRAINT person_document_id_key UNIQUE (document_id);

ALTER TABLE accommodation
    ADD CONSTRAINT unique_accommodation_name UNIQUE (name, deleted_at);

ALTER TABLE accommodation
    ADD CONSTRAINT unique_accommodation_ses_code UNIQUE (ses_code, deleted_at);

ALTER TABLE account
    ADD CONSTRAINT unique_account_username UNIQUE (username, deleted_at);

ALTER TABLE booking
    ADD CONSTRAINT fk30ag6bppm7d4eaxepa2cis7sh FOREIGN KEY (created_by_id) REFERENCES account (id) ON DELETE NO ACTION;

ALTER TABLE accommodation_employees
    ADD CONSTRAINT fk4ykl401ub0rwmdghxtyopsrml FOREIGN KEY (accommodations_id) REFERENCES accommodation (id) ON DELETE NO ACTION;

ALTER TABLE booking
    ADD CONSTRAINT fk5uxucbfmlrnnjunuxoei5ux0s FOREIGN KEY (accommodation_id) REFERENCES accommodation (id) ON DELETE NO ACTION;

ALTER TABLE booking
    ADD CONSTRAINT fk70t92vvx289ayx2hq2v4hdcjl FOREIGN KEY (payment_id) REFERENCES payment (id) ON DELETE NO ACTION;

ALTER TABLE communication
    ADD CONSTRAINT fk7uktjs903rws1g728cc89wgul FOREIGN KEY (booking_id) REFERENCES booking (id) ON DELETE NO ACTION;

ALTER TABLE booking
    ADD CONSTRAINT fk96jkaoulp144fv4m8wy9hyo9q FOREIGN KEY (last_modified_by_id) REFERENCES account (id) ON DELETE NO ACTION;

ALTER TABLE person
    ADD CONSTRAINT fkahsnx7wkyvehbeknklxn4365c FOREIGN KEY (document_id) REFERENCES document (id) ON DELETE NO ACTION;

ALTER TABLE employee
    ADD CONSTRAINT fkcfg6ajo8oske94exynxpf7tf9 FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE NO ACTION;

ALTER TABLE accommodation_employees
    ADD CONSTRAINT fkjlrlbnbe1d4ex5uwumc403svx FOREIGN KEY (employees_id) REFERENCES employee (id) ON DELETE NO ACTION;

ALTER TABLE person
    ADD CONSTRAINT fkk7rgn6djxsv2j2bv1mvuxd4m9 FOREIGN KEY (address_id) REFERENCES address (id) ON DELETE NO ACTION;

ALTER TABLE person
    ADD CONSTRAINT fknw70weghtddca0o49ce3vmh7s FOREIGN KEY (booking_id) REFERENCES booking (id) ON DELETE NO ACTION;

ALTER TABLE security_event
    ADD CONSTRAINT fktfm07ubstvg7j01uwcvbd9hu8 FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE NO ACTION;