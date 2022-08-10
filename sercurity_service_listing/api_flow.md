

First We have to create persmission via this curl command

# Request

```
    curl --location --request POST 'http://localhost:8095/security/permissions/create' \
    --header 'Content-Type: application/json' \
    --data-raw '{
            "version": 1,
            "name": "Test Permision",
            "permission": "Team",
            "description": "Team menu"
    }'
```

# Response

```
        {
            "id": 181,
            "createdAt": null,
            "updatedAt": null,
            "createdBy": null,
            "updatedBy": null,
            "version": 1,
            "name": "Test Permision",
            "permission": "Team",
            "description": "Team menu"
        }
```

We can also create multiple permissions using this api 

[click here to see api request and resposne](./create_permission_in_batch.md)


We can also create multiple permissions using this api  using Json File


[click here to see api request and resposne](./create_permission_in_batch.md)


We can update permission vai thi api

# Request

```
            curl --location --request POST 'http://localhost:8095/security/permissions/update' \
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


# Response


```
    {
        "id": 1,
        "createdAt": null,
        "updatedAt": null,
        "createdBy": "postman",
        "updatedBy": "postman",
        "version": 1,
        "name": "Test Permision",
        "permission": "Team",
        "description": "Team menu"
    }   
```

