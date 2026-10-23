-- Sample data for the dev profile only, copied from the front end's src/lib/data.ts.

INSERT INTO audits (id, title, department, auditor, status, start_date, end_date, progress, description) VALUES
('AUD-1001', 'Quarterly Financial Controls', 'Finance', 'Priya Sharma', 'IN_PROGRESS', DATE '2026-10-01', DATE '2026-10-31', 65,
 'Review of journal entry approvals, reconciliations and segregation of duties.'),
('AUD-1002', 'ISO 27001 Access Review', 'IT Security', 'Arjun Rao', 'IN_REVIEW', DATE '2026-09-15', DATE '2026-10-20', 90,
 'User access recertification for production systems and privileged accounts.'),
('AUD-1003', 'Vendor Onboarding Compliance', 'Procurement', 'Meera Iyer', 'PLANNED', DATE '2026-11-05', DATE '2026-11-28', 0,
 'Due diligence, contract terms and KYC checks for newly onboarded vendors.'),
('AUD-1004', 'Payroll Process Audit', 'HR', 'Rahul Verma', 'COMPLETED', DATE '2026-08-01', DATE '2026-08-30', 100,
 'Payroll calculation accuracy, statutory deductions and master data changes.'),
('AUD-1005', 'Warehouse Inventory Count', 'Operations', 'Kavya Nair', 'IN_PROGRESS', DATE '2026-10-10', DATE '2026-11-10', 40,
 'Physical stock verification against ERP records across three warehouses.'),
('AUD-1006', 'Data Privacy (DPDP) Readiness', 'Legal', 'Arjun Rao', 'COMPLETED', DATE '2026-07-01', DATE '2026-07-31', 100,
 'Consent management, data retention and breach notification procedures.');

INSERT INTO findings (id, audit_id, title, severity, owner, due_date, resolved) VALUES
('F-201', 'AUD-1001', 'Manual journals posted without second approval', 'HIGH', 'Finance Controller', DATE '2026-11-15', FALSE),
('F-202', 'AUD-1001', 'Bank reconciliation delayed by 12 days', 'MEDIUM', 'Treasury', DATE '2026-11-05', FALSE),
('F-203', 'AUD-1002', 'Terminated users with active VPN access', 'CRITICAL', 'IT Ops', DATE '2026-10-25', FALSE),
('F-204', 'AUD-1002', 'Shared admin account on database server', 'HIGH', 'DBA Team', DATE '2026-11-01', TRUE),
('F-205', 'AUD-1004', 'Missing approval for salary revisions', 'MEDIUM', 'HR Ops', DATE '2026-09-15', TRUE),
('F-206', 'AUD-1005', 'Stock variance above 2% threshold', 'HIGH', 'Warehouse Manager', DATE '2026-11-20', FALSE),
('F-207', 'AUD-1006', 'Retention policy not applied to backups', 'LOW', 'Legal', DATE '2026-08-30', TRUE);

ALTER SEQUENCE audit_seq RESTART WITH 1007;
ALTER SEQUENCE finding_seq RESTART WITH 208;
