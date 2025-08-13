SELECT u.id, u.email, r.nom AS role
FROM auth_service.users u
JOIN auth_service.user_roles ur ON ur.user_id = u.id
JOIN auth_service.roles r ON r.id = ur.role_id
WHERE u.email = 'sidiki1@gmail.com';
