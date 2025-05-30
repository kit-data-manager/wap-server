# server root is reachable
GET {{host}}/wap/
HTTP 200

#create container
POST {{host}}/wap/
Content-Type: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"
Link: <http://www.w3.org/ns/ldp\#BasicContainer>; rel="type"
Slug: crud_performance
{
  "@context": [ "http://www.w3.org/ns/anno.jsonld", "http://www.w3.org/ns/ldp.jsonld" ],
  "@type": [ "ldp:Container", "ldp:BasicContainer", "AnnotationCollection"],
  "label":"This container will contain many annotations"
}

# Create an Annotation
POST {{host}}/wap/crud_performance/
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

GET {{host}}/wap/crud_performance/
HTTP 200
