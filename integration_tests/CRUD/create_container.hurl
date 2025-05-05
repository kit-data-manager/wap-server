# server root is reachable
GET {{host}}/wap/
HTTP 200

#create container
POST {{host}}/wap/
Content-Type: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"
Link: <http://www.w3.org/ns/ldp\#BasicContainer>; rel="type"
Slug: annocontainer
{
  "@context": [ "http://www.w3.org/ns/anno.jsonld", "http://www.w3.org/ns/ldp.jsonld" ],
  "@type": [ "ldp:Container", "ldp:BasicContainer", "AnnotationCollection"],
  "label":"This container will contain nothing"
}
HTTP 201

# Get the Container and capture ETag
GET {{host}}/wap/annocontainer/
Accept: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"
HTTP 200
[Captures]
etag: header "ETag"

# Delete the Container using ETag
DELETE {{host}}/wap/annocontainer/
If-Match: {{etag}}
HTTP 204
