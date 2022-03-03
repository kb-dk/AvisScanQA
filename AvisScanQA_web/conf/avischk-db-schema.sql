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


CREATE TABLE newspaperarchive
(
    orig_relpath  VARCHAR(1024) PRIMARY KEY,
    format_type   VARCHAR(10)  NOT NULL,
    edition_date  DATE         NOT NULL,
    single_page   BOOLEAN      NOT NULL,
    page_number   INTEGER,
    avisid        VARCHAR(255) NOT NULL,
    avistitle     VARCHAR(255) NOT NULL,
    shadow_path   TEXT         NOT NULL,
    section_title VARCHAR(255),
    edition_title VARCHAR(255),
    delivery_date DATE         NOT NULL,
    handle        BIGSERIAL,
    side_label    VARCHAR(255),
    fraktur       BOOLEAN,
    problems      TEXT         NOT NULL,
    batchid       VARCHAR(255) NOT NULL,
    FOREIGN KEY (batchid) REFERENCES batch (batchid)
);



CREATE INDEX avisid_date_index ON newspaperarchive (avisid, edition_date);
CREATE INDEX avisid_format_index ON newspaperarchive (avisid, format_type);

CREATE TABLE notes
(
    batchid       VARCHAR(255) NOT NULL,
    avisid        VARCHAR(255) NULL,
    edition_date  DATE         NULL,
    edition_title VARCHAR(255) NULL,
    section_title VARCHAR(255) NULL,
    page_number   INTEGER      NULL,
    notes         TEXT         NOT NULL,

    CONSTRAINT notes_batchid_avisid_edition_date_edition_title_section_tit_key
        UNIQUE (batchid, avisid, edition_date, edition_title, section_title, page_number)

--     FOREIGN KEY (batchid) REFERENCES batch (batchid)
-- TODO many to many links between avisIDs in notes and newspaperArchive
);

CREATE INDEX notes_batch_index ON notes (batchid);
CREATE INDEX notes_batch_avis_index ON notes (batchid, avisid);
CREATE INDEX notes_batch_avis_date_index ON notes (batchid, avisid, edition_date);
CREATE INDEX notes_batch_avis_date_edition_index ON notes (batchid, avisid, edition_date, edition_title);
CREATE INDEX notes_batch_avis_date_edition_section_index ON notes (batchid, avisid, edition_date, edition_title, section_title);
CREATE INDEX notes_batch_avis_date_edition_section_page_index ON notes (batchid, avisid, edition_date, edition_title,
                                                                        section_title, page_number);

