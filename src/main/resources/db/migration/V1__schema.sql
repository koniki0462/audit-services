-- The whole schema. Add changes as new V<n>__description.sql files; never edit this one once it has run anywhere.
-- Runs on PostgreSQL (dev, staging, prod) and on H2 in PostgreSQL mode (tests).

-- Server-assigned ids: "AUD-" + audit_seq and "F-" + finding_seq, shared by all organizations.
CREATE SEQUENCE audit_seq START WITH 1001;
CREATE SEQUENCE finding_seq START WITH 201;

-- Tenants. Every audit and finding belongs to exactly one organization.
CREATE TABLE organizations (
    organization_id       VARCHAR(36)   PRIMARY KEY,
    legal_name            VARCHAR(255)  NOT NULL,
    trade_name            VARCHAR(255),
    entity_type           VARCHAR(30),
    industry_code         VARCHAR(20),   -- NAICS, SIC or NACE
    business_purpose      TEXT,
    date_of_incorporation DATE,
    contact_email         VARCHAR(255),
    contact_number        VARCHAR(30),
    address               VARCHAR(1000),
    fiscal_year_end_month INTEGER,       -- 1-12
    status                VARCHAR(20)   DEFAULT 'ACTIVE' NOT NULL,
    is_active             BOOLEAN       DEFAULT TRUE NOT NULL,
    created_at            TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at            TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version               BIGINT        DEFAULT 0 NOT NULL,
    CONSTRAINT organizations_legal_name_uq UNIQUE (legal_name),
    CONSTRAINT organizations_entity_type_chk CHECK (entity_type IN
        ('SOLE_PROPRIETORSHIP', 'PARTNERSHIP', 'LLC', 'CORPORATION', 'NON_PROFIT', 'GOVERNMENT', 'OTHER')),
    CONSTRAINT organizations_fiscal_month_chk CHECK (fiscal_year_end_month BETWEEN 1 AND 12),
    CONSTRAINT organizations_status_chk CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'DISSOLVED'))
);

-- People who can sign in. Emails are stored lower-case.
CREATE TABLE users (
    id            VARCHAR(36)   PRIMARY KEY,
    email         VARCHAR(255)  NOT NULL,
    name          VARCHAR(255)  NOT NULL,
    password_hash VARCHAR(100)  NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version       BIGINT        DEFAULT 0 NOT NULL,
    CONSTRAINT users_email_uq UNIQUE (email)
);

CREATE TABLE memberships (
    id              VARCHAR(36)  PRIMARY KEY,
    user_id         VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36)  NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT memberships_user_org_uq UNIQUE (user_id, organization_id),
    CONSTRAINT memberships_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT memberships_organization_fk FOREIGN KEY (organization_id)
        REFERENCES organizations (organization_id) ON DELETE CASCADE
);

CREATE TABLE audits (
    id              VARCHAR(20)   PRIMARY KEY,
    organization_id VARCHAR(36)   NOT NULL,
    title           VARCHAR(255)  NOT NULL,
    department      VARCHAR(255)  NOT NULL,
    auditor         VARCHAR(255)  NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    start_date      DATE          NOT NULL,
    end_date        DATE          NOT NULL,
    progress        INTEGER       NOT NULL,
    description     VARCHAR(2000),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version         BIGINT        DEFAULT 0 NOT NULL,
    CONSTRAINT audits_organization_fk FOREIGN KEY (organization_id) REFERENCES organizations (organization_id),
    -- Lets findings reference (audit, organization) together.
    CONSTRAINT audits_id_organization_uq UNIQUE (id, organization_id),
    CONSTRAINT audits_status_chk CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'IN_REVIEW', 'COMPLETED')),
    CONSTRAINT audits_progress_chk CHECK (progress BETWEEN 0 AND 100),
    CONSTRAINT audits_dates_chk CHECK (end_date >= start_date)
);

CREATE TABLE findings (
    id              VARCHAR(20)   PRIMARY KEY,
    organization_id VARCHAR(36)   NOT NULL,
    audit_id        VARCHAR(20)   NOT NULL,
    title           VARCHAR(255)  NOT NULL,
    severity        VARCHAR(20)   NOT NULL,
    owner           VARCHAR(255)  NOT NULL,
    due_date        DATE          NOT NULL,
    resolved        BOOLEAN       DEFAULT FALSE NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version         BIGINT        DEFAULT 0 NOT NULL,
    -- A finding always belongs to the same organization as its audit, and goes when the audit is deleted.
    CONSTRAINT findings_audit_organization_fk FOREIGN KEY (audit_id, organization_id)
        REFERENCES audits (id, organization_id) ON DELETE CASCADE,
    CONSTRAINT findings_severity_chk CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

-- Every query filters by organization first.
CREATE INDEX idx_memberships_organization ON memberships (organization_id);
CREATE INDEX idx_audits_organization ON audits (organization_id, status);
CREATE INDEX idx_findings_audit ON findings (organization_id, audit_id);
CREATE INDEX idx_findings_organization ON findings (organization_id, resolved, severity);
