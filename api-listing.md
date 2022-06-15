# securityservice api-listing

This markdown file contains all api document Order-wise how does flow works of security service 

	baseSecurityUrl:
		localhost:8094

		
Flow for creating required permissions

**/security/permissions/create**

Api to create new permission objects.

	Method: POST
	Params:
		service Json permission object
	Response:
		{} Json of Permission objects

*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/permissions/create' \
--header 'Content-Type: application/json' \
--data-raw '{
        "version": 1,
        "name": "Test Permision",
        "permission": "Team",
        "description": "Team menu"
    }'
```
### We also have api create permission in batch using by array into json objects and json file  ###

**/security/permissions/createPermissionInBatch**

Api to create permission by  array into json objects

	Method: POST
	Params:
		array into Json permission object
	Response:
		array of {} Json of Permission objects

*CURL*
```
curl --location --request POST '{{baseSecurityUrl}}/security/permissions/createPermissionInBatch' \
--header 'Content-Type: application/json' \
--data-raw '{
		"xformation-perfmanager-ui-plugin": [
		{
			"permission": "catalog-topmenu-manangedashboardsbtn",
			"description": "Permissions of '\''Manage Dashboards'\'' Button of '\''xformation-perfmanager-ui-plugin\\src\\domain\\Catalog\\TopMenu.tsx'\''"
		},
		{
			"permission": "catalog-topmenu-catlogbtn",
			"description": "Permissions of '\''Catalog'\'' Button of '\''xformation-perfmanager-ui-plugin\\src\\domain\\Catalog\\TopMenu.tsx'\''"
		}
	],
	"grafana-ui": [
		{
			"permission": "overview",
			"description": ""
		},
		{
			"permission": "activity-log",
			"description": ""
		},
	]
}'

```

### We also have api create permission in batch using by json file of  ``array into json objects``   ###

**/security/permissions/createPermissionsByFile**

	Method: POST
	Params:
		array into  Json permission object
	Response:
		array of {} Json of Permission objects


*CURL*
```
curl --location --request POST ''{{baseSecurityUrl}}/security/permissions/createPermissionsByFile' \
--form 'inputFile=@"/E:/Desktop/permission.txt"' \
--form 'str="hello"'
```

** /security/permissions/listAll **

Api to all permission objects.

	Method: POST | GET
	Response:
		{} Json list of Permission objects

*CURL*
```
	curl --location -g --request GET '{{baseSecurityUrl}}/permissions/listAll'
```

** /security/permissions/{id} **

Api to fetch permission objects by id.

	Method: POST
	Response:
		{} Json of Permission objects

*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/permissions/61'
```
** /security/permissions/delete/{id} **

Api to delete a permission objects by id.

	Method: POST
	Response:
		Success message

*CURL*
```
	curl --location -g --request POST '{{baseSecurityUrl}}/permissions/delete/{id}'
```

** /security/permissions/update **

Api to update a permission by json objects.

	Method: POST
	Params:
		entity Json permission object
	Response:
		{} Json of Permission objects

*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/permissions/update' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": 105,
    "createdBy": "postman",
    "updatedBy": "postman",
    "version": 1,
    "name": "Test Permision",
    "permission": "Team",
    "description": "Team menu"
}'
```

**/security/permissions/delete**

Api to delete a permission by json object.

	Method: POST
	Params:
		entity Json permission object
	Response:
		Success

After the required permission is created we have to create required  roles and role Group

**/security/roles/create**

Api to create new Role objects.

	Method: POST
	Params:
		service Json Role object
	Response:
		{} Json of Role objects

*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/roles/create' \
--header 'Content-Type: application/json' \
--data-raw '{
    "version": 1,
    "name": "Test Role",
    "permission": "Test",
    "description": "Test role"
}'
```

**/security/roles/listAll**

Api to all Role objects.

	Method: POST | GET
	Response:
		{} Json list of Role objects

*CURL*

```
curl --location -g --request GET '{{baseSecurityUrl}}/roles/listAll'
```

**/security/role/{id}**

Api to fetch Role objects by id.

	Method: POST
	Response:
		{} Json of Role objects

*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/roles/104'
```

**/security/roles/delete/{id}**

Api to delete a Role objects by id.

	Method: POST
	Response:
		Success message
*CURL*

```
curl --location -g --request POST '{{baseSecurityUrl}}/roles/delete/103'
```

**/security/roles/update**

Api to update a Role by json objects.

**Note :-** While updating the role we can also add permissions via array of permissions json for example :

```JSON
{
    ....,
    permissions: [
                    {
                        "version": 1,
                        "name": "Test Permision",
                        "permission": "Team",
                        "description": "Team menu"
                    },
                    {
                        "version": 1,
                        "name": "Test Permision",
                        "permission": "Team",
                        "description": "Team menu"
                    }
        ] 
}
``` 
and roles also can set the particular role is a role group by settng ```grp: true```

We can also make relations between roles one to many, Many to One or Many to Many by seting roles perameter in json objects for example :

```JSON
{
    ....,
    "roles": [
        {
            "id": 104
        },
        {
            "id":105
        },
    ]
}

```

	Method: POST
	Params:
		entity Json Role object
	Response:
		{} Json of Role objects
*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/roles/update' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": 104,
    "createdBy": "postman",
    "updatedBy": "postman",
    "name": "Test Role",
    "version": 1,
    "grp": false,
    "description": "Test role",
    "permissions": null,
    "roles": null
}'
```

**/security/roles/delete**

Api to delete a Role by json object.

	Method: POST
	Params:
		entity Json Role object
	Response:
		Success


**/security/users/create**

Api to create new User objects with require permissions and roles.

	Method: POST
	Params:
		service Json User object
	Response:
		{} Json of User objects

*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/users/create' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username": "test",
    "password": "password",
    "active": true,
    "email": "test.test@synectiks.com",
    "roles": [
        {
            "id": 7,
            "createdAt": null,
            "updatedAt": null,
            "createdBy": null,
            "updatedBy": null,
            "name": "Admin_Role_Group",
            "version": 1,
            "grp": true,
            "description": "Admin role group",
            "permissions": [],
            "roles": [
                {
                    "id": 82,
                    "createdAt": null,
                    "updatedAt": null,
                    "createdBy": null,
                    "updatedBy": null,
                    "name": "Team Menu",
                    "version": 1,
                    "grp": false,
                    "description": "Team menu access role",
                    "permissions": [
                        {
                            "id": 1,
                            "createdAt": null,
                            "updatedAt": null,
                            "createdBy": null,
                            "updatedBy": null,
                            "version": 1,
                            "name": "Team",
                            "permission": "Team",
                            "description": "Team menu"
                        }
                    ],
                    "roles": []
                },
                {
                    "id": 6,
                    "createdAt": null,
                    "updatedAt": null,
                    "createdBy": null,
                    "updatedBy": null,
                    "name": "Admin",
                    "version": 1,
                    "grp": false,
                    "description": "Admin",
                    "permissions": [
                        {
                            "id": 1,
                            "createdAt": null,
                            "updatedAt": null,
                            "createdBy": null,
                            "updatedBy": null,
                            "version": 1,
                            "name": "Team",
                            "permission": "Team",
                            "description": "Team menu"
                        },
                        {
                            "id": 8,
                            "createdAt": null,
                            "updatedAt": null,
                            "createdBy": null,
                            "updatedBy": null,
                            "version": 1,
                            "name": "Overview",
                            "permission": "Overview",
                            "description": "Overview menu"
                        }
                    ],
                    "roles": []
                }
            ]
        },
        {
            "id": 83,
            "createdAt": null,
            "updatedAt": null,
            "createdBy": null,
            "updatedBy": null,
            "name": "Team Menu Role",
            "version": 1,
            "grp": true,
            "description": "Team Access Role",
            "permissions": [],
            "roles": [
                {
                    "id": 82,
                    "createdAt": null,
                    "updatedAt": null,
                    "createdBy": null,
                    "updatedBy": null,
                    "name": "Team Menu",
                    "version": 1,
                    "grp": false,
                    "description": "Team menu access role",
                    "permissions": [
                        {
                            "id": 1,
                            "createdAt": null,
                            "updatedAt": null,
                            "createdBy": null,
                            "updatedBy": null,
                            "version": 1,
                            "name": "Team",
                            "permission": "Team",
                            "description": "Team menu"
                        }
                    ],
                    "roles": []
                }
            ]
        }
    ],
    "organization": "Synectiks",
    "owner": null,
    "googleMfaKey": null,
    "isMfaEnable": "NO",
    "mfaQrImageFilePath": null,
    "inviteStatus": null,
    "inviteLink": null,
    "inviteCode": null,
    "inviteSentOn": null,
    "tempPassword": null,
    "mfaQrCode": null,
    "pendingInviteList": null,
    "teamList": null,
    "isAuthenticatedByUserName": false,
    "authenticatedByUserName": false
}'
```
**/security/users/listAll**

Api to all User objects.

	Method: POST | GET
	Response:
		{} Json list of User objects

*CURL*
```
	curl --location -g --request GET '{{baseSecurityUrl}}/users/listAll'
```

### /security/users/{id} ###

Api to fetch User objects by id.

	Method: POST
	Response:
		{} Json of User objects

*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/users/101'
```

**/security/users/delete/{id}**

Api to delete a User objects by id.

	Method: POST
	Response:
		Success message

*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/users/delete/101'
```

**/security/users/delete**

Api to delete a User by json object.

	Method: POST
	Params:
		entity Json User object
	Response:
		Success

*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/users/delete' \
--header 'Content-Type: text/plain' \
--data-raw '{
    
}'
```

**/security/users/update**

Api to update a User by json objects.

	Method: POST
	Params:
		entity Json User object
	Response:
		{} Json of User objects

**First User have to login using login api /security/public/login**

Api to get authenticate user with username and password.

	Method: POST | GET
	Params:
		username*		String
		password*		String
		rememberMe	boolean
		redirectTo	String url to redirect on success
	Response:
		Success	authentication response
		
*CURL*
```cURL
curl --location -g --request GET '{{baseSecurityUrl}} public/login?username=test&password=password'
````
		
** If user is not exist then we have to create new user using this api /security/public/signup **

Api to get authenticate user with LoginRequest object.

	Method: POST
	Params:
		request*	LoginRequest object
	Response:
		Success	authentication response


*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/public/signup' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username":"test",
    "password":"password",
    "rememberMe":false
}'
```

**After the signup user can login using this signin api /security/public/signin**

Api to get authenticate user with User object.

	Method: POST
	Params:
		request*	User object
	Response:
		Success	authentication response

*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/public/singin' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username":"test",
    "password":"password"
}'
```

**/security/public/authenticate**

Api to get authenticate user with UsernamePasswordToken object.

	Method: POST
	Params:
		token*		UsernamePasswordToken object
	Response:
		Success	authentication response

*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/public/authenticate' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username":"test",
    "password":"password"
}'
```

**/security/public/logout**

Api to get authenticate user with UsernamePasswordToken object.

	Method: POST | GET
	Response:
		Success

*CURL*
```
curl --location -g --request GET '{{baseSecurityUrl}}/public/logout'
```


*CURL*
```
curl --location -g --request POST '{{baseSecurityUrl}}/users/update' \
--header 'Content-Type: text/plain' \
--data-raw '{}'
```




### Who do I talk to? ###
	Please mail us on
	info@syenctiks.com
