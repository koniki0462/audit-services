-- Who may manage what. An application admin runs the whole service: creates organizations and manages the role
-- catalogue. An organization admin manages one organization's details and members. Everyone else is a member,
-- who can work with the organization's audits and findings.
-- Make the first application admin by hand: UPDATE users SET is_app_admin = TRUE WHERE email = '...';
ALTER TABLE users ADD COLUMN is_app_admin BOOLEAN DEFAULT FALSE NOT NULL;

-- Profile details, set when an organization admin creates the account. Accounts from sign-up only have name.
ALTER TABLE users ADD COLUMN first_name VARCHAR(100);
ALTER TABLE users ADD COLUMN last_name VARCHAR(100);
ALTER TABLE users ADD COLUMN gender VARCHAR(20);
ALTER TABLE users ADD COLUMN phone_number VARCHAR(30);
ALTER TABLE users ADD COLUMN address VARCHAR(1000);
ALTER TABLE users ADD CONSTRAINT users_gender_chk CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'UNDISCLOSED'));

ALTER TABLE memberships ADD COLUMN role VARCHAR(20) DEFAULT 'MEMBER' NOT NULL;
ALTER TABLE memberships ADD CONSTRAINT memberships_role_chk CHECK (role IN ('ORG_ADMIN', 'MEMBER'));
-- Until now every member could manage their organization, so existing members keep that.
UPDATE memberships SET role = 'ORG_ADMIN';
