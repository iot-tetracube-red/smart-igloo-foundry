CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
create schema if not exists igloo_hub;

create table igloo_hub.houses
(
    id         uuid         not null primary key,
    name       varchar(255) not null unique,
    house_type varchar(50)  not null
);

create table igloo_hub.environments
(
    id       uuid         not null primary key,
    name     varchar(255) not null,
    house_id uuid         not null references igloo_hub.houses (id)
);

create table igloo_hub.trusted_broker_clients
(
    id          uuid         not null primary key,
    client_id   varchar(100) not null unique,
    username    varchar(100) not null unique,
    password    varchar(255) not null,
    client_type varchar(50)  not null
);

create table igloo_hub.accounts
(
    id       uuid         not null primary key,
    username varchar(255) not null unique,
    password varchar(255) not null
);

create table igloo_hub.houses_accounts
(
    id         uuid        not null primary key,
    account_id uuid        not null references igloo_hub.accounts (id),
    house_id   uuid        not null references igloo_hub.houses (id),
    role       varchar(50) not null
);

create table igloo_hub.switchers
(
    id                       uuid                  not null primary key,
    display_name             varchar(255)          not null unique,
    display_color            varchar(20)           not null,
    feedback_topic           varchar(255)          not null unique,
    command_topic            varchar(255)          not null unique,
    is_online                boolean default false not null,
    house_id                 uuid                  not null references igloo_hub.houses (id),
    environment_id           uuid                  not null references igloo_hub.environments (id),
    trusted_broker_client_id uuid                  not null references igloo_hub.trusted_broker_clients (id)
);

create table igloo_hub.switchers_actions
(
    id           uuid         not null primary key,
    display_name varchar(255) not null,
    logic_state  varchar(50)  not null,
    mqtt_command varchar(255) not null,
    switcher_id  uuid         not null references igloo_hub.switchers (id)
);

create table igloo_hub.switchers_logs
(
    id          uuid         not null primary key,
    stored_at   timestamp    not null,
    logic_state varchar(255) not null,
    switcher_id uuid         not null references igloo_hub.switchers (id)
);

/*create table igloo_hub.light_tricks*/
/*(*/
/*    id             uuid         not null primary key,*/
/*    display_name   varchar(255) not null,*/
/*    response_topic varchar(255) not null unique,*/
/*    nest_id        uuid         not null references igloo_hub.nests (id),*/
/*    room_id        uuid         not null references igloo_hub.rooms (id),*/
/*    color_code     varchar(20),*/
/*    switch_topic   varchar(255) not null,*/
/*    api_topic      varchar(255) not null,*/
/*    color_topic    varchar(255) not null,*/
/*    credential_id  uuid         not null references igloo_hub.broker_credentials (id)*/
/*);*/
/**/
/*create table igloo_hub.light_tricks_patterns*/
/*(*/
/*    id                     uuid         not null primary key,*/
/*    firmware_id            integer      not null,*/
/*    human_name             varchar(255) not null,*/
/*    light_tricks_effect_id uuid         not null references igloo_hub.light_tricks (id)*/
/*);*/
/**/
/*create table igloo_hub.light_tricks_history*/
/*(*/
/*    id                     uuid        not null primary key,*/
/*    stored_at              timestamp   not null,*/
/*    light_tricks_effect_id uuid        not null references igloo_hub.light_tricks (id),*/
/*    logic_state            varchar(10) not null,*/
/*    palette                integer     not null,*/
/*    brightness             integer     not null,*/
/*    effect_pattern         integer     not null*/
/*);*/
/**/
/*create table igloo_hub.light_tricks_palettes*/
/*(*/
/*    id                     uuid         not null primary key,*/
/*    firmware_id            integer      not null,*/
/*    human_name             varchar(255) not null,*/
/*    light_tricks_effect_id uuid         not null*/
/*);*/
/**/
/*create table igloo_hub.light_tricks_actions*/
/*(*/
/*    id                     uuid         not null primary key,*/
/*    logic_state            varchar(255) not null,*/
/*    light_tricks_effect_id uuid         not null references igloo_hub.light_tricks (id),*/
/*    human_name             varchar(255) not null,*/
/*    mqtt_command           varchar(10)  not null*/
/*);*/