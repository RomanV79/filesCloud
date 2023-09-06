CREATE TABLE IF NOT EXISTS roles (
    id         INTEGER PRIMARY KEY  AUTO_INCREMENT,
    name       VARCHAR(128)         NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS users_roles(
    id          INTEGER PRIMARY KEY AUTO_INCREMENT,
    users_id    INTEGER             NOT NULL,
    roles_id    INTEGER             NOT NULL,
    FOREIGN KEY (users_id) REFERENCES users (id),
    FOREIGN KEY (roles_id) REFERENCES roles(id)
)