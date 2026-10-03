create table accommodation
(
    internet_connection boolean,
    created_at          timestamp(6) with time zone not null,
    deleted_at          timestamp(6) with time zone,
    updated_at          timestamp(6) with time zone not null,
    version             bigint                      not null,
    id                  uuid                        not null,
    name                varchar(255)                not null,
    ses_code            varchar(255)                not null,
    primary key (id)
);
create table accommodation_employees
(
    accommodations_id uuid not null,
    employees_id      uuid not null,
    primary key (accommodations_id, employees_id)
);
create table account
(
    enabled         boolean                     not null,
    requires_reset  boolean                     not null,
    created_at      timestamp(6) with time zone not null,
    deleted_at      timestamp(6) with time zone,
    disabled_at     timestamp(6) with time zone,
    updated_at      timestamp(6) with time zone not null,
    version         bigint                      not null,
    employee_id     uuid unique,
    id              uuid                        not null,
    hashed_password varchar(255)                not null,
    username        varchar(255)                not null,
    roles           varchar(255) array          not null,
    primary key (id)
);
create table address
(
    created_at    timestamp(6) with time zone not null,
    updated_at    timestamp(6) with time zone not null,
    version       bigint                      not null,
    id            uuid                        not null,
    dtype         varchar(31)                 not null check ((dtype in ('Address', 'SpanishAddress'))),
    address_line1 bytea                       not null,
    address_line2 bytea,
    country       bytea                       not null,
    municipality  bytea                       not null,
    postal_code   bytea                       not null,
    primary key (id)
);
create table booking
(
    internet_connection     boolean,
    number_of_people        integer                     not null,
    number_of_rooms         integer,
    self_check_in_requested boolean                     not null,
    created_at              timestamp(6) with time zone not null,
    end_time                timestamp(6) with time zone not null,
    start_time              timestamp(6) with time zone not null,
    updated_at              timestamp(6) with time zone not null,
    version                 bigint                      not null,
    accommodation_id        uuid                        not null,
    created_by_id           uuid,
    id                      uuid                        not null,
    last_modified_by_id     uuid,
    payment_id              uuid unique,
    primary key (id)
);
create table communication
(
    batch_order    integer,
    status         smallint                    not null check ((status between 0 and 5)),
    type           smallint                    not null check ((type between 0 and 2)),
    created_at     timestamp(6) with time zone not null,
    sent_timestamp timestamp(6) with time zone,
    updated_at     timestamp(6) with time zone not null,
    version        bigint                      not null,
    batch_id       uuid,
    booking_id     uuid                        not null,
    id             uuid                        not null,
    ses_id         uuid,
    dtype          varchar(31)                 not null check ((dtype in
                                                                ('Communication',
                                                                 'CancellationCommunication'))),
    error          varchar(255),
    primary key (id)
);
create table configuration
(
    bookings_retention_days   integer                     not null,
    digital_signature_enabled boolean                     not null,
    logs_retention_days       integer                     not null,
    manual_review_enabled     boolean                     not null,
    ses_credentials_valid     boolean                     not null,
    created_at                timestamp(6) with time zone not null,
    updated_at                timestamp(6) with time zone not null,
    version                   bigint                      not null,
    id                        uuid                        not null,
    remember_me_key           bytea                       not null,
    ses_landlord_code         bytea,
    ses_password              bytea,
    ses_username              bytea,
    primary key (id)
);
create table document
(
    type           smallint                    not null check ((type between 0 and 3)),
    created_at     timestamp(6) with time zone not null,
    updated_at     timestamp(6) with time zone not null,
    version        bigint                      not null,
    id             uuid                        not null,
    dtype          varchar(31)                 not null check ((dtype in ('Document', 'DniDocument'))),
    number         bytea                       not null,
    support_number bytea,
    primary key (id)
);
create table employee
(
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version    bigint                      not null,
    account_id uuid                        not null unique,
    id         uuid                        not null,
    name       bytea                       not null,
    surname    bytea                       not null,
    primary key (id)
);
create table payment
(
    type        smallint                    not null check ((type between 0 and 7)),
    created_at  timestamp(6) with time zone not null,
    date        timestamp(6) with time zone,
    expiry_date timestamp(6) with time zone,
    updated_at  timestamp(6) with time zone not null,
    version     bigint                      not null,
    id          uuid                        not null,
    dtype       varchar(31)                 not null check ((dtype in ('Payment', 'CreditCardPayment'))),
    mean        varchar(255),
    holder      bytea,
    primary key (id)
);
create table person
(
    relationship   smallint check ((relationship between 0 and 14)),
    created_at     timestamp(6) with time zone not null,
    signed_at      timestamp(6) with time zone,
    updated_at     timestamp(6) with time zone not null,
    version        bigint                      not null,
    address_id     uuid,
    booking_id     uuid                        not null,
    document_id    uuid unique,
    id             uuid                        not null,
    birth_date     bytea,
    email          bytea,
    first_surname  bytea,
    gender         bytea,
    ip_address     bytea,
    name           bytea,
    nationality    bytea,
    paths          bytea,
    phone_number1  bytea,
    phone_number2  bytea,
    second_surname bytea,
    user_agent     bytea,
    primary key (id)
);
create table security_event
(
    method         smallint                    not null check ((method between 0 and 1)),
    type           smallint                    not null check ((type between 0 and 6)),
    created_at     timestamp(6) with time zone not null,
    timestamp      timestamp(6) with time zone not null,
    updated_at     timestamp(6) with time zone not null,
    version        bigint                      not null,
    account_id     uuid,
    id             uuid                        not null,
    remote_address bytea                       not null,
    primary key (id)
);
alter table if exists accommodation_employees
    add constraint FKjlrlbnbe1d4ex5uwumc403svx foreign key (employees_id) references employee;
alter table if exists accommodation_employees
    add constraint FK4ykl401ub0rwmdghxtyopsrml foreign key (accommodations_id) references accommodation;
alter table if exists account
    add constraint FK1kec5bwba2rl0j8garlarwe3d foreign key (employee_id) references employee;
alter table if exists booking
    add constraint FK5uxucbfmlrnnjunuxoei5ux0s foreign key (accommodation_id) references accommodation;
alter table if exists booking
    add constraint FK30ag6bppm7d4eaxepa2cis7sh foreign key (created_by_id) references account;
alter table if exists booking
    add constraint FK96jkaoulp144fv4m8wy9hyo9q foreign key (last_modified_by_id) references account;
alter table if exists booking
    add constraint FK70t92vvx289ayx2hq2v4hdcjl foreign key (payment_id) references payment;
alter table if exists communication
    add constraint FK7uktjs903rws1g728cc89wgul foreign key (booking_id) references booking;
alter table if exists employee
    add constraint FKcfg6ajo8oske94exynxpf7tf9 foreign key (account_id) references account;
alter table if exists person
    add constraint FKk7rgn6djxsv2j2bv1mvuxd4m9 foreign key (address_id) references address;
alter table if exists person
    add constraint FKnw70weghtddca0o49ce3vmh7s foreign key (booking_id) references booking;
alter table if exists person
    add constraint FKahsnx7wkyvehbeknklxn4365c foreign key (document_id) references document;
alter table if exists security_event
    add constraint FKtfm07ubstvg7j01uwcvbd9hu8 foreign key (account_id) references account;

-- Soft-delete unique constraints
ALTER TABLE account
    ADD CONSTRAINT unique_account_username UNIQUE NULLS NOT DISTINCT (username, deleted_at);
ALTER TABLE accommodation
    ADD CONSTRAINT unique_accommodation_name UNIQUE NULLS NOT DISTINCT (name, deleted_at);
ALTER TABLE accommodation
    ADD CONSTRAINT unique_accommodation_ses_code UNIQUE NULLS NOT DISTINCT (ses_code, deleted_at);