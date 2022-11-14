create table batch
(
    batchid       varchar(255)             not null,
    avisid        varchar(255)             not null,
    roundtrip     integer                  not null,
    start_date    date                     not null,
    end_date      date                     not null,
    delivery_date date                     not null,
    problems      text,
    state         text                     not null,
    num_problems  integer default 0        not null,
    username      varchar(255)             not null,
    lastmodified  timestamp with time zone not null,
    constraint unique_modstate
        unique (batchid, lastmodified)
);

alter table batch
    owner to avisscqa;

