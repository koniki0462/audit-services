-- Sample organizations and users for the dev profile only. Every password is "password123".
--   demo@auditapp.dev    member of Acme Corp (the V1_1 sample data) and Globex Ltd
--   owner@globex.dev     member of Globex Ltd only

UPDATE organizations SET name = 'Acme Corp' WHERE id = '00000000-0000-0000-0000-000000000001';

INSERT INTO organizations (id, name) VALUES
('00000000-0000-0000-0000-000000000002', 'Globex Ltd');

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
('AUD-1007', '00000000-0000-0000-0000-000000000002', 'SOX Revenue Recognition', 'Finance', 'Hank Scorpio', 'IN_PROGRESS',
 DATE '2026-10-05', DATE '2026-11-05', 30, 'Contract review and revenue cut-off testing for Q3.'),
('AUD-1008', '00000000-0000-0000-0000-000000000002', 'Plant Safety Inspection', 'Operations', 'Frank Grimes', 'PLANNED',
 DATE '2026-11-10', DATE '2026-11-20', 0, 'Fire safety, PPE compliance and incident logs at the main plant.');

INSERT INTO findings (id, organization_id, audit_id, title, severity, owner, due_date, resolved) VALUES
('F-208', '00000000-0000-0000-0000-000000000002', 'AUD-1007', 'Revenue booked before delivery on two contracts', 'HIGH',
 'Revenue Accounting', DATE '2026-11-12', FALSE),
('F-209', '00000000-0000-0000-0000-000000000002', 'AUD-1007', 'Missing signed order forms', 'LOW',
 'Sales Ops', DATE '2026-11-30', FALSE);

ALTER SEQUENCE audit_seq RESTART WITH 1009;
ALTER SEQUENCE finding_seq RESTART WITH 210;
