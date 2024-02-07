alter table users add column status varchar(255); -- accepted/rejected/locked/suspended/terminated
alter table users add column request_type varchar(255); --online/mail/appkube
alter table users add column comments varchar(5000);
