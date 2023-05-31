create table batch
(
    batchid       varchar(255)      not null,
    avisid        varchar(255)      not null,
    roundtrip     integer           not null,
    start_date    date              not null,
    end_date      date              not null,
    delivery_date date              not null,
    problems      text,
    state         text              not null,
    num_problems  integer default 0 not null,
    username      varchar(255)      not null,
    lastmodified  timestamp         not null,

    CONSTRAINT unique_modstate UNIQUE (batchid, lastmodified)
);


create table newspaperarchive
(
    orig_relpath  varchar(1024) primary key,
    format_type   varchar(10)   not null,
    edition_date  date          not null,
    single_page   boolean       not null,
    page_number   integer,
    avisid        varchar(255)  not null,
    avistitle     varchar(255)  not null,
    shadow_path   text          not null,
    section_title varchar(255),
    edition_title varchar(255),
    delivery_date date          not null,
    handle        bigserial,
    side_label    varchar(255),
    fraktur       boolean,
    problems      text          not null,
    batchid       varchar(255)  not null,
    jpeg_relpath  varchar(1024) not null,
    alto_relpath  varchar(1024)
);



CREATE INDEX avisid_date_index ON newspaperarchive (avisid, edition_date);
CREATE INDEX avisid_format_index ON newspaperarchive (avisid, format_type);

CREATE TABLE notes
(
    id            SERIAL primary key unique not null,
    batchid       VARCHAR(255)              NOT NULL,
    avisid        VARCHAR(255)              NULL,
    edition_date  DATE                      NULL,
    edition_title VARCHAR(255)              NULL,
    section_title VARCHAR(255)              NULL,
    page_number   INTEGER                   NULL,
    username      VARCHAR(255)              NOT NULL,
    notes         TEXT                      NOT NULL,
    created       timestamptz               not null


--     FOREIGN KEY (batchid) REFERENCES batch (batchid)
-- TODO many to many links between avisIDs in notes and newspaperArchive
);
