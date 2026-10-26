-- Dev sample roles (after V3 made every existing member an organization admin):
--   demo@auditapp.dev    application admin; organization admin of Acme Corp, member of Globex Ltd
--   owner@globex.dev     organization admin of Globex Ltd
UPDATE users SET is_app_admin = TRUE, first_name = 'Demo', last_name = 'User'
WHERE id = '00000000-0000-0000-0000-0000000000a1';
UPDATE users SET first_name = 'Globex', last_name = 'Owner'
WHERE id = '00000000-0000-0000-0000-0000000000a2';
UPDATE memberships SET role = 'MEMBER' WHERE id = '00000000-0000-0000-0000-0000000000b2';
