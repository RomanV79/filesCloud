CREATE TABLE IF NOT EXISTS users (
    id          INTEGER PRIMARY KEY,
    login       VARCHAR(128)    NOT NULL UNIQUE,
    password    VARCHAR(128)    NOT NULL
    )