CREATE TABLE roles (
    id UUID PRIMARY KEY,

    tenant_id UUID,

    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_roles_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT uq_roles_tenant_name
        UNIQUE (tenant_id, name)
);

CREATE INDEX idx_roles_tenant_id
    ON roles (tenant_id);

CREATE INDEX idx_roles_type
    ON roles (type);