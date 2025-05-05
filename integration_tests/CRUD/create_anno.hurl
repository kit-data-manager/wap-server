# server root is reachable
GET {{host}}/wap/
HTTP 200

#create container
POST {{host}}/wap/
Content-Type: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"
Link: <http://www.w3.org/ns/ldp\#BasicContainer>; rel="type"
Slug: helloWorld
{
  "@context": [ "http://www.w3.org/ns/anno.jsonld", "http://www.w3.org/ns/ldp.jsonld" ],
  "@type": [ "ldp:Container", "ldp:BasicContainer", "AnnotationCollection"],
  "label":"This container will contain nothing"
}
HTTP 201

# Create an Annotation
POST {{host}}/wap/helloWorld/
Content-Type: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"

{
  "@context": "http://www.w3.org/ns/anno.jsonld",
  "type": "Annotation",
  "body": {
    "type": "TextualBody",
    "value": "This is a comment",
    "format": "text/plain"
  },
  "target": "http://example.org/page1"
}
HTTP 201
[Captures]
anno: header "Location"

# Get the Annotation and capture ETag
GET {{anno}}
Accept: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"
HTTP 200
[Captures]
etag: header "ETag"

# Delete the Annotation using ETag
DELETE {{anno}}
If-Match: {{etag}}
HTTP 204

# Get the Container and capture ETag
GET {{host}}/wap/helloWorld/
Accept: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"
HTTP 200
[Captures]
etag: header "ETag"

# Delete the Container using ETag
DELETE {{host}}/wap/helloWorld/
If-Match: {{etag}}
HTTP 204
