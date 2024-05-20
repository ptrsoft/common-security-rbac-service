INSERT INTO public.config
(id, "key", value, status, created_at, updated_at, updated_by, created_by, organization_id)
VALUES((SELECT nextval('psql_seq')), 'CMDB_ORGANIZATION_URL', 'http://213.210.36.2:6057/api/organization', 'ACTIVE', current_timestamp, current_timestamp, 'System', 'System', (select id from organization where name = 'PTRSOFT'));
INSERT INTO public.config
(id, "key", value, status, created_at, updated_at, updated_by, created_by, organization_id)
VALUES((SELECT nextval('psql_seq')), 'GLOBAL_SESSION_TIMEOUT', '15', 'ACTIVE', current_timestamp, current_timestamp, 'System', 'System', (select id from organization where name = 'PTRSOFT'));
