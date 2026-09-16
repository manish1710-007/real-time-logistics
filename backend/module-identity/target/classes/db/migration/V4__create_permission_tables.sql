CREATE TABLE permissions (
    id UUID PRIMARY KEY,

    code VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,

    CONSTRAINT uq_permissions_code
        UNIQUE(code)
);