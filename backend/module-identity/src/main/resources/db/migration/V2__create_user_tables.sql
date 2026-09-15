CREATE TABLE users (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),

    status VARCHAR(20) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,

    last_login_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_users_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT uq_users_tenant_email
        UNIQUE (tenant_id, email)
);

CREATE INDEX idx_users_tenant_id
    ON users (tenant_id);