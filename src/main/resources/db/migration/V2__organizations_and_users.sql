-- Multi-tenancy: organizations, users and their memberships. Audits and findings belong to one organization.
-- Runs on PostgreSQL (staging, prod) and on H2 in PostgreSQL mode (dev, tests).

CREATE TABLE organizations (
    id          VARCHAR(36)   PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT        NOT NULL DEFAULT 0
);

CREATE TABLE users (
    id            VARCHAR(36)   PRIMARY KEY,
    email         VARCHAR(255)  NOT NULL,
    name          VARCHAR(255)  NOT NULL,
    password_hash VARCHAR(100)  NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version       BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT users_email_uq UNIQUE (email)
);

CREATE TABLE memberships (
    id              VARCHAR(36)  PRIMARY KEY,
    user_id         VARCHAR(36)  NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    organization_id VARCHAR(36)  NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT memberships_user_org_uq UNIQUE (user_id, organization_id)
);

CREATE INDEX idx_memberships_organization ON memberships (organization_id);

-- Rows created before multi-tenancy move into a default organization, created only if there are any.
-- Add members to it through the API (POST /organization/members) once someone has signed up.
INSERT INTO organizations (id, name)
SELECT '00000000-0000-0000-0000-000000000001', 'Default organization'
WHERE EXISTS (SELECT 1 FROM audits);

ALTER TABLE audits ADD COLUMN organization_id VARCHAR(36);
UPDATE audits SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE audits ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE audits ADD CONSTRAINT audits_organization_fk FOREIGN KEY (organization_id) REFERENCES organizations (id);
ALTER TABLE audits ADD CONSTRAINT audits_id_organization_uq UNIQUE (id, organization_id);

ALTER TABLE findings ADD COLUMN organization_id VARCHAR(36);
UPDATE findings SET organization_id = (SELECT a.organization_id FROM audits a WHERE a.id = findings.audit_id);
ALTER TABLE findings ALTER COLUMN organization_id SET NOT NULL;
-- A finding always belongs to the same organization as its audit.
ALTER TABLE findings ADD CONSTRAINT findings_audit_organization_fk
    FOREIGN KEY (audit_id, organization_id) REFERENCES audits (id, organization_id) ON DELETE CASCADE;

CREATE INDEX idx_audits_organization ON audits (organization_id, status);
CREATE INDEX idx_findings_organization ON findings (organization_id, resolved, severity);
