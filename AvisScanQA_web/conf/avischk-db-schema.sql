

CREATE TABLE batch
(
    batchid       VARCHAR(255) PRIMARY KEY,
    avisid        VARCHAR(255) NOT NULL,
    roundtrip     INTEGER      NOT NULL,
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,
    delivery_date DATE         NOT NULL,
    problems      TEXT         NOT NULL,
    state         TEXT         NOT NULL
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
