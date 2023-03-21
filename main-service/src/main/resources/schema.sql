CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    user_name VARCHAR(40) NOT NULL UNIQUE,
    email VARCHAR(40) NOT NULL UNIQUE,
    CONSTRAINT pk_user PRIMARY KEY (user_id),
    CONSTRAINT UQ_USER_NAME UNIQUE (user_name),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    category_name VARCHAR(40) NOT NULL UNIQUE,
    CONSTRAINT pk_category PRIMARY KEY (category_id),
    CONSTRAINT UQ_CATEGORY_NAME UNIQUE (category_name)
);

CREATE TABLE IF NOT EXISTS events (
    event_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    title VARCHAR(120) NOT NULL,
    annotation VARCHAR(2000) NOT NULL,
    description VARCHAR(7000) NOT NULL,
    category_id BIGINT NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL,
    paid BOOLEAN,
    participant_limit INT,
    request_moderation BOOLEAN,
    created_on TIMESTAMP WITHOUT TIME ZONE,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    initiator_id BIGINT NOT NULL,
    state VARCHAR(16),
    FOREIGN KEY (category_id) REFERENCES categories (category_id) ON DELETE RESTRICT,
    FOREIGN KEY (initiator_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT pk_event PRIMARY KEY (event_id)
);

CREATE TABLE IF NOT EXISTS participation_requests (
    participation_request_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    requester_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(16),
    CONSTRAINT unique_requester_id_and_event_id UNIQUE (requester_id, event_id),
    FOREIGN KEY (requester_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events (event_id),
    CONSTRAINT pk_participation_request PRIMARY KEY (participation_request_id)
);

CREATE TABLE IF NOT EXISTS compilations (
    compilation_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    title VARCHAR(80) NOT NULL,
    pinned BOOLEAN NOT NULL,
    CONSTRAINT pk_compilation PRIMARY KEY (compilation_id)
);

CREATE TABLE IF NOT EXISTS compilations_events (
    compilation_id BIGINT NOT NULL,
--         constraint compilation_events_compilation_fkey references compilations on DELETE CASCADE,
    event_id BIGINT NOT NULL,
    FOREIGN KEY (compilation_id) REFERENCES compilations (compilation_id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events (event_id),
    PRIMARY KEY (compilation_id, event_id)
);