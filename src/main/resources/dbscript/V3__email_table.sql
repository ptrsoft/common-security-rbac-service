CREATE TABLE public.email_queue (
	id int8 NOT NULL,
	created_at timestamp NULL,
	created_by varchar(255) NULL,
	updated_at timestamp NULL,
	updated_by varchar(255) NULL,
	status varchar(255) NULL,
	mail_type varchar(255) NULL, -- new user, password change etc..
	mail_from varchar(255) NULL,
	mail_to varchar(255) NULL,
	mail_subject varchar(255) NULL,
	mail_body varchar(5000) NULL,
	mail_template varchar(255) NULL,
	user_name varchar(255) NULL,
	organization_id int8 NULL,
	CONSTRAINT email_queue_pkey PRIMARY KEY (id)
);
ALTER TABLE public.email_queue ADD CONSTRAINT fk_email_queue__organization_id FOREIGN KEY (organization_id) REFERENCES public.organization(id);
