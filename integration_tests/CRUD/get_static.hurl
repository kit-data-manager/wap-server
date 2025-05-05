# server root is reachable
GET {{host}}
HTTP 200

GET {{host}}/webapp
[Options]
location-trusted: true
HTTP 200

GET {{host}}/javadoc
[Options]
location-trusted: true
HTTP 200
