insert into role_assignments
(id
,role_code
,pers_id_grantee
,pers_id_registerer)
values
(nextval('ROLE_ASSIGNMENT_ID_SEQ')
,'ADMIN'
,(select id from persons where user_id = 'system')
,(select id from persons where user_id = 'system'));
