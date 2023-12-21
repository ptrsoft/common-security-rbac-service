-- public."document" definition

-- Drop table

-- DROP TABLE public."document";

CREATE TABLE public."document" (
	id int8 NOT NULL,
	created_by varchar(255) NULL,
	created_on timestamp NULL,
	file_ext varchar(255) NULL,
	file_name varchar(255) NULL,
	file_size int8 NULL,
	identifier varchar(255) NULL,
	local_file_path varchar(255) NULL,
	source_id varchar(255) NULL,
	storage_location varchar(255) NULL,
	updated_by varchar(255) NULL,
	updated_on timestamp NULL,
	CONSTRAINT document_pkey PRIMARY KEY (id)
);


-- public.flyway_schema_history definition

-- Drop table

-- DROP TABLE public.flyway_schema_history;

CREATE TABLE public.flyway_schema_history (
	installed_rank int4 NOT NULL,
	"version" varchar(50) NULL,
	description varchar(200) NOT NULL,
	"type" varchar(20) NOT NULL,
	script varchar(1000) NOT NULL,
	checksum int4 NULL,
	installed_by varchar(100) NOT NULL,
	installed_on timestamp NOT NULL DEFAULT now(),
	execution_time int4 NOT NULL,
	success bool NOT NULL,
	CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank)
);
CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


-- public.organization definition

-- Drop table

-- DROP TABLE public.organization;

CREATE TABLE public.organization (
	id int8 NOT NULL,
	created_at timestamp NULL,
	created_by varchar(255) NULL,
	updated_at timestamp NULL,
	updated_by varchar(255) NULL,
	address varchar(255) NULL,
	cmdb_org_id int8 NULL,
	date_of_establishment timestamp NULL,
	description varchar(5000) NULL,
	email varchar(255) NULL,
	fax varchar(255) NULL,
	"name" varchar(255) NULL,
	phone varchar(255) NULL,
	status varchar(255) NULL,
	CONSTRAINT organization_pkey PRIMARY KEY (id)
);


-- public."permission" definition

-- Drop table

-- DROP TABLE public."permission";

CREATE TABLE public."permission" (
	id int8 NOT NULL,
	created_at timestamp NULL,
	created_by varchar(255) NULL,
	updated_at timestamp NULL,
	updated_by varchar(255) NULL,
	description varchar(255) NULL,
	"name" varchar(255) NULL,
	status varchar(255) NULL,
	"version" int8 NULL,
	CONSTRAINT permission_pkey PRIMARY KEY (id)
);


-- public.permission_category definition

-- Drop table

-- DROP TABLE public.permission_category;

CREATE TABLE public.permission_category (
	id int8 NOT NULL,
	created_at timestamp NULL,
	created_by varchar(255) NULL,
	updated_at timestamp NULL,
	updated_by varchar(255) NULL,
	description varchar(255) NULL,
	"name" varchar(255) NULL,
	status varchar(255) NULL,
	"version" int8 NULL,
	CONSTRAINT permission_category_pkey PRIMARY KEY (id)
);


-- public."policy" definition

-- Drop table

-- DROP TABLE public."policy";

CREATE TABLE public."policy" (
	id int8 NOT NULL,
	created_at timestamp NULL,
	created_by varchar(255) NULL,
	updated_at timestamp NULL,
	updated_by varchar(255) NULL,
	description varchar(255) NULL,
	"name" varchar(255) NULL,
	status varchar(255) NULL,
	"version" int8 NULL,
	CONSTRAINT policy_pkey PRIMARY KEY (id)
);


-- public.policy_assigned_permissions definition

-- Drop table

-- DROP TABLE public.policy_assigned_permissions;

CREATE TABLE public.policy_assigned_permissions (
	id int8 NOT NULL,
	created_at timestamp NULL,
	created_by varchar(255) NULL,
	updated_at timestamp NULL,
	updated_by varchar(255) NULL,
	permission_category_id int8 NULL,
	permission_category_name varchar(255) NULL,
	permission_id int8 NULL,
	permission_name varchar(255) NULL,
	policy_id int8 NULL,
	policy_name varchar(255) NULL,
	CONSTRAINT policy_assigned_permissions_pkey PRIMARY KEY (id)
);


-- public."result" definition

-- Drop table

-- DROP TABLE public."result";

CREATE TABLE public."result" (
	id int8 NOT NULL,
	created_at timestamp NULL,
	created_by varchar(255) NULL,
	updated_at timestamp NULL,
	updated_by varchar(255) NULL,
	policy_id int8 NOT NULL,
	rule_id int8 NOT NULL,
	scroll_id varchar(255) NULL,
	terminated_early bool NOT NULL,
	time_out bool NOT NULL,
	took_in_millis int8 NOT NULL,
	total_hits int8 NOT NULL,
	CONSTRAINT result_pkey PRIMARY KEY (id)
);


-- public.roles definition

-- Drop table

-- DROP TABLE public.roles;

CREATE TABLE public.roles (
	id int8 NOT NULL,
	created_at timestamp NULL,
	created_by varchar(255) NULL,
	updated_at timestamp NULL,
	updated_by varchar(255) NULL,
	description varchar(255) NULL,
	grp bool NOT NULL,
	is_default bool NOT NULL DEFAULT false,
	"name" varchar(255) NULL,
	"version" int8 NULL,
	CONSTRAINT roles_pkey PRIMARY KEY (id)
);


-- public.user_session definition

-- Drop table

-- DROP TABLE public.user_session;

CREATE TABLE public.user_session (
	id int8 NOT NULL,
	created_at timestamp NULL,
	created_by varchar(255) NULL,
	updated_at timestamp NULL,
	updated_by varchar(255) NULL,
	expiration_time timestamp NULL,
	session_id varchar(255) NULL,
	username varchar(255) NULL,
	CONSTRAINT user_session_pkey PRIMARY KEY (id)
);


-- public.organizational_unit definition

-- Drop table

-- DROP TABLE public.organizational_unit;

CREATE TABLE public.organizational_unit (
	id int8 NOT NULL,
	created_at timestamp NULL,
	created_by varchar(255) NULL,
	updated_at timestamp NULL,
	updated_by varchar(255) NULL,
	description varchar(5000) NULL,
	"name" varchar(255) NULL,
	status varchar(255) NULL,
	organization_id int8 NULL,
	CONSTRAINT organizational_unit_pkey PRIMARY KEY (id),
	CONSTRAINT fkjp8up3ysmx52e26hat4ddfvwc FOREIGN KEY (organization_id) REFERENCES public.organization(id)
);


-- public.permission_category_permissions definition

-- Drop table

-- DROP TABLE public.permission_category_permissions;

CREATE TABLE public.permission_category_permissions (
	permission_category_id int8 NOT NULL,
	permissions_id int8 NOT NULL,
	CONSTRAINT fk6yy1xp4swjfqrocky6pbwvepn FOREIGN KEY (permission_category_id) REFERENCES public.permission_category(id),
	CONSTRAINT fkfwqcah846enn37rtxssckh6b3 FOREIGN KEY (permissions_id) REFERENCES public."permission"(id)
);


-- public.policy_permissions definition

-- Drop table

-- DROP TABLE public.policy_permissions;

CREATE TABLE public.policy_permissions (
	policy_id int8 NOT NULL,
	permissions_id int8 NOT NULL,
	CONSTRAINT fkn3ubhdfsb86h66cyocgfoabgk FOREIGN KEY (policy_id) REFERENCES public."policy"(id),
	CONSTRAINT fkthth1u869ladvoywpfiosw50y FOREIGN KEY (permissions_id) REFERENCES public.policy_assigned_permissions(id)
);


-- public.policy_rule_result_hits definition

-- Drop table

-- DROP TABLE public.policy_rule_result_hits;

CREATE TABLE public.policy_rule_result_hits (
	policy_rule_result_id int8 NOT NULL,
	hits varchar(255) NULL,
	CONSTRAINT fktgfw2q551t5fwvtav8uataand FOREIGN KEY (policy_rule_result_id) REFERENCES public."result"(id)
);


-- public.roles_policies definition

-- Drop table

-- DROP TABLE public.roles_policies;

CREATE TABLE public.roles_policies (
	role_id int8 NOT NULL,
	policies_id int8 NOT NULL,
	CONSTRAINT fk1uny2l7d06h1pt72a2nwkanrj FOREIGN KEY (role_id) REFERENCES public.roles(id),
	CONSTRAINT fkmfsrcwnq74agqwf23tnvhd59h FOREIGN KEY (policies_id) REFERENCES public."policy"(id)
);


-- public.roles_roles definition

-- Drop table

-- DROP TABLE public.roles_roles;

CREATE TABLE public.roles_roles (
	role_id int8 NOT NULL,
	roles_id int8 NOT NULL,
	CONSTRAINT roles_roles_pkey PRIMARY KEY (role_id, roles_id),
	CONSTRAINT fkh0er2qrwpns8hej8uk95j4f6h FOREIGN KEY (role_id) REFERENCES public.roles(id),
	CONSTRAINT fkqao0vjvqum05dlyy56wc7r5mi FOREIGN KEY (roles_id) REFERENCES public.roles(id)
);


-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
	id int8 NOT NULL,
	created_at timestamp NULL,
	created_by varchar(255) NULL,
	updated_at timestamp NULL,
	updated_by varchar(255) NULL,
	active bool NOT NULL,
	email varchar(255) NULL,
	enc_password varchar(255) NULL,
	google_mfa_key varchar(255) NULL,
	invite_code varchar(255) NULL,
	invite_link varchar(1000) NULL,
	invite_sent_on timestamp NULL,
	invite_status varchar(255) NULL,
	is_mfa_enable varchar(255) NULL,
	last_login_at timestamp NULL,
	login_count int4 NULL,
	mfa_qr_image_file_path varchar(255) NULL,
	"password" varchar(255) NULL,
	temp_password varchar(255) NULL,
	"type" varchar(255) NULL,
	username varchar(255) NULL,
	organization_id int8 NULL,
	owner_id int8 NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id),
	CONSTRAINT fk9q8fdenwsqjwrjfivd5ovv5k3 FOREIGN KEY (organization_id) REFERENCES public.organization(id),
	CONSTRAINT fkntyuh06i5y3y6ir598luxy3k9 FOREIGN KEY (owner_id) REFERENCES public.users(id)
);


-- public.users_roles definition

-- Drop table

-- DROP TABLE public.users_roles;

CREATE TABLE public.users_roles (
	user_id int8 NOT NULL,
	roles_id int8 NOT NULL,
	CONSTRAINT fk2o0jvgh89lemvvo17cbqvdxaa FOREIGN KEY (user_id) REFERENCES public.users(id),
	CONSTRAINT fka62j07k5mhgifpp955h37ponj FOREIGN KEY (roles_id) REFERENCES public.roles(id)
);
