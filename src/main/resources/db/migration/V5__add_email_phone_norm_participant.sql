alter table participant add column email_norm varchar(320) null;
alter table participant add column phone_norm varchar(30) null;

create index idx_participant_email_norm on participant(email_norm);
create index idx_participant_phone_norm on participant(phone_norm);