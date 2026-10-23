-- Sample organization details for the dev profile only.

UPDATE organizations SET
    trade_name = 'Acme',
    entity_type = 'CORPORATION',
    industry_code = '332510',
    business_purpose = 'Manufacturer of hardware, tools and consumer products.',
    date_of_incorporation = DATE '2009-04-01',
    contact_email = 'compliance@acme.test',
    contact_number = '+91 80 4000 1000',
    address = '12 MG Road, Bengaluru, Karnataka 560001, India',
    fiscal_year_end_month = 3
WHERE organization_id = '00000000-0000-0000-0000-000000000001';

UPDATE organizations SET
    trade_name = 'Globex',
    entity_type = 'LLC',
    industry_code = '541611',
    business_purpose = 'Management consulting and corporate advisory services.',
    date_of_incorporation = DATE '2015-01-15',
    contact_email = 'audit@globex.test',
    contact_number = '+91 22 5000 2000',
    address = '4 Nariman Point, Mumbai, Maharashtra 400021, India',
    fiscal_year_end_month = 12
WHERE organization_id = '00000000-0000-0000-0000-000000000002';
