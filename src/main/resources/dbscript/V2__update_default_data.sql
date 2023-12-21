
update policy_assigned_permissions set permission_category_name = pc.name  from policy_assigned_permissions pap,permission_category pc
where pap.permission_category_id = pc.id ;

update policy_assigned_permissions set permission_name = p.name from policy_assigned_permissions pap, permission p
where pap.permission_id = p.id;

update policy_assigned_permissions set policy_name = p.name from policy_assigned_permissions pap, policy p
where pap.policy_id = p.id
