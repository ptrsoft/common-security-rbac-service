
update policy_assigned_permissions set permission_category_name = (select pc.name from permission_category pc
where policy_assigned_permissions.permission_category_id = pc.id)

update policy_assigned_permissions set permission_name  = (select p.name from "permission"  p
where policy_assigned_permissions.permission_id  = p.id)

update policy_assigned_permissions set policy_name = (select p.name from "policy"  p
where policy_assigned_permissions.policy_id  = p.id)
