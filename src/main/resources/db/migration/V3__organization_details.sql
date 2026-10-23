-- Organization core details. Renames id/name to organization_id/legal_name; foreign keys from memberships,
-- audits and findings follow the renamed column. Existing organizations become ACTIVE.
-- Runs on PostgreSQL (dev, staging, prod) and on H2 in PostgreSQL mode (tests).

ALTER TABLE organizations RENAME COLUMN id TO organization_id;
ALTER TABLE organizations RENAME COLUMN name TO legal_name;
ALTER TABLE organizations ADD CONSTRAINT organizations_legal_name_uq UNIQUE (legal_name);

-- Doing-business-as / operating name.
ALTER TABLE organizations ADD COLUMN trade_name VARCHAR(255);
ALTER TABLE organizations ADD COLUMN entity_type VARCHAR(30);
ALTER TABLE organizations ADD CONSTRAINT organizations_entity_type_chk CHECK (entity_type IN
    ('SOLE_PROPRIETORSHIP', 'PARTNERSHIP', 'LLC', 'CORPORATION', 'NON_PROFIT', 'GOVERNMENT', 'OTHER'));
-- NAICS, SIC or NACE code.
ALTER TABLE organizations ADD COLUMN industry_code VARCHAR(20);
ALTER TABLE organizations ADD COLUMN business_purpose TEXT;
ALTER TABLE organizations ADD COLUMN date_of_incorporation DATE;
ALTER TABLE organizations ADD COLUMN contact_email VARCHAR(255);
ALTER TABLE organizations ADD COLUMN contact_number VARCHAR(30);
ALTER TABLE organizations ADD COLUMN address VARCHAR(1000);
-- Month (1-12) in which the financial year ends.
ALTER TABLE organizations ADD COLUMN fiscal_year_end_month INTEGER;
ALTER TABLE organizations ADD CONSTRAINT organizations_fiscal_month_chk CHECK (fiscal_year_end_month BETWEEN 1 AND 12);
ALTER TABLE organizations ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL;
ALTER TABLE organizations ADD CONSTRAINT organizations_status_chk CHECK (status IN
    ('PENDING', 'ACTIVE', 'SUSPENDED', 'DISSOLVED'));
ALTER TABLE organizations ADD COLUMN is_active BOOLEAN DEFAULT TRUE NOT NULL;
