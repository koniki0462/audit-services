-- Runs on PostgreSQL (staging, prod) and on H2 in PostgreSQL mode (dev, tests).

CREATE SEQUENCE audit_seq START WITH 1001;
CREATE SEQUENCE finding_seq START WITH 201;

CREATE TABLE audits (
    id          VARCHAR(20)   PRIMARY KEY,
    title       VARCHAR(255)  NOT NULL,
    department  VARCHAR(255)  NOT NULL,
    auditor     VARCHAR(255)  NOT NULL,
    status      VARCHAR(20)   NOT NULL CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'IN_REVIEW', 'COMPLETED')),
    start_date  DATE          NOT NULL,
    end_date    DATE          NOT NULL,
    progress    INTEGER       NOT NULL CHECK (progress BETWEEN 0 AND 100),
    description VARCHAR(2000),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT audits_dates_chk CHECK (end_date >= start_date)
);

CREATE TABLE findings (
    id          VARCHAR(20)   PRIMARY KEY,
    audit_id    VARCHAR(20)   NOT NULL REFERENCES audits (id) ON DELETE CASCADE,
    title       VARCHAR(255)  NOT NULL,
    severity    VARCHAR(20)   NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    owner       VARCHAR(255)  NOT NULL,
    due_date    DATE          NOT NULL,
    resolved    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_audits_status ON audits (status);
CREATE INDEX idx_findings_audit_id ON findings (audit_id);
CREATE INDEX idx_findings_open ON findings (resolved, severity);
