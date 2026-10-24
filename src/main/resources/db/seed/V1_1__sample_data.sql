-- Sample data for the dev profile only (Flyway loads db/seed only there). Acme's audits and findings are the
-- same records as the front end's src/test/fixtures.ts. Every password is "password123".
--   demo@auditapp.dev    member of Acme Corp and Globex Ltd
--   owner@globex.dev     member of Globex Ltd only

INSERT INTO organizations (organization_id, legal_name, trade_name, entity_type, industry_code, business_purpose,
                           date_of_incorporation, contact_email, contact_number, address, fiscal_year_end_month) VALUES
('00000000-0000-0000-0000-000000000001', 'Acme Corp', 'Acme', 'CORPORATION', '332510',
 'Manufacturer of hardware, tools and consumer products.', DATE '2009-04-01', 'compliance@acme.test',
 '+91 80 4000 1000', '12 MG Road, Bengaluru, Karnataka 560001, India', 3),
('00000000-0000-0000-0000-000000000002', 'Globex Ltd', 'Globex', 'LLC', '541611',
 'Management consulting and corporate advisory services.', DATE '2015-01-15', 'audit@globex.test',
 '+91 22 5000 2000', '4 Nariman Point, Mumbai, Maharashtra 400021, India', 12);

INSERT INTO users (id, email, name, password_hash) VALUES
('00000000-0000-0000-0000-0000000000a1', 'demo@auditapp.dev', 'Demo User',
 '$2a$10$aEDkOzSpR/MorBCiDnDQAO5T6wsVhrEQHttJFQ54J42LfonitLYi6'),
('00000000-0000-0000-0000-0000000000a2', 'owner@globex.dev', 'Globex Owner',
 '$2a$10$aEDkOzSpR/MorBCiDnDQAO5T6wsVhrEQHttJFQ54J42LfonitLYi6');

INSERT INTO memberships (id, user_id, organization_id) VALUES
('00000000-0000-0000-0000-0000000000b1', '00000000-0000-0000-0000-0000000000a1', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-0000000000b2', '00000000-0000-0000-0000-0000000000a1', '00000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-0000000000b3', '00000000-0000-0000-0000-0000000000a2', '00000000-0000-0000-0000-000000000002');

INSERT INTO audits (id, organization_id, title, department, auditor, status, start_date, end_date, progress, description) VALUES
-- Acme Corp
('AUD-1001', '00000000-0000-0000-0000-000000000001', 'Quarterly Financial Controls', 'Finance', 'Priya Sharma',
 'IN_PROGRESS', DATE '2026-10-01', DATE '2026-10-31', 65,
 'Review of journal entry approvals, reconciliations and segregation of duties.'),
('AUD-1002', '00000000-0000-0000-0000-000000000001', 'ISO 27001 Access Review', 'IT Security', 'Arjun Rao',
 'IN_REVIEW', DATE '2026-09-15', DATE '2026-10-20', 90,
 'User access recertification for production systems and privileged accounts.'),
('AUD-1003', '00000000-0000-0000-0000-000000000001', 'Vendor Onboarding Compliance', 'Procurement', 'Meera Iyer',
 'PLANNED', DATE '2026-11-05', DATE '2026-11-28', 0,
 'Due diligence, contract terms and KYC checks for newly onboarded vendors.'),
('AUD-1004', '00000000-0000-0000-0000-000000000001', 'Payroll Process Audit', 'HR', 'Rahul Verma',
 'COMPLETED', DATE '2026-08-01', DATE '2026-08-30', 100,
 'Payroll calculation accuracy, statutory deductions and master data changes.'),
('AUD-1005', '00000000-0000-0000-0000-000000000001', 'Warehouse Inventory Count', 'Operations', 'Kavya Nair',
 'IN_PROGRESS', DATE '2026-10-10', DATE '2026-11-10', 40,
 'Physical stock verification against ERP records across three warehouses.'),
('AUD-1006', '00000000-0000-0000-0000-000000000001', 'Data Privacy (DPDP) Readiness', 'Legal', 'Arjun Rao',
 'COMPLETED', DATE '2026-07-01', DATE '2026-07-31', 100,
 'Consent management, data retention and breach notification procedures.'),
-- Globex Ltd
('AUD-1007', '00000000-0000-0000-0000-000000000002', 'SOX Revenue Recognition', 'Finance', 'Hank Scorpio',
 'IN_PROGRESS', DATE '2026-10-05', DATE '2026-11-05', 30,
 'Contract review and revenue cut-off testing for Q3.'),
('AUD-1008', '00000000-0000-0000-0000-000000000002', 'Plant Safety Inspection', 'Operations', 'Frank Grimes',
 'PLANNED', DATE '2026-11-10', DATE '2026-11-20', 0,
 'Fire safety, PPE compliance and incident logs at the main plant.');

INSERT INTO findings (id, organization_id, audit_id, title, severity, owner, due_date, resolved) VALUES
-- Acme Corp
('F-201', '00000000-0000-0000-0000-000000000001', 'AUD-1001', 'Manual journals posted without second approval', 'HIGH', 'Finance Controller', DATE '2026-11-15', FALSE),
('F-202', '00000000-0000-0000-0000-000000000001', 'AUD-1001', 'Bank reconciliation delayed by 12 days', 'MEDIUM', 'Treasury', DATE '2026-11-05', FALSE),
('F-203', '00000000-0000-0000-0000-000000000001', 'AUD-1002', 'Terminated users with active VPN access', 'CRITICAL', 'IT Ops', DATE '2026-10-25', FALSE),
('F-204', '00000000-0000-0000-0000-000000000001', 'AUD-1002', 'Shared admin account on database server', 'HIGH', 'DBA Team', DATE '2026-11-01', TRUE),
('F-205', '00000000-0000-0000-0000-000000000001', 'AUD-1004', 'Missing approval for salary revisions', 'MEDIUM', 'HR Ops', DATE '2026-09-15', TRUE),
('F-206', '00000000-0000-0000-0000-000000000001', 'AUD-1005', 'Stock variance above 2% threshold', 'HIGH', 'Warehouse Manager', DATE '2026-11-20', FALSE),
('F-207', '00000000-0000-0000-0000-000000000001', 'AUD-1006', 'Retention policy not applied to backups', 'LOW', 'Legal', DATE '2026-08-30', TRUE),
-- Globex Ltd
('F-208', '00000000-0000-0000-0000-000000000002', 'AUD-1007', 'Revenue booked before delivery on two contracts', 'HIGH', 'Revenue Accounting', DATE '2026-11-12', FALSE),
('F-209', '00000000-0000-0000-0000-000000000002', 'AUD-1007', 'Missing signed order forms', 'LOW', 'Sales Ops', DATE '2026-11-30', FALSE);

-- New records continue after the sample ids.
ALTER SEQUENCE audit_seq RESTART WITH 1009;
ALTER SEQUENCE finding_seq RESTART WITH 210;
