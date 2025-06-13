---
absorb: true
---

Simple formats are those that do have a Mimetype representation in Accept or Content-Type headers
that needs no additional processing. Examples ist TURTLE (text/turtle). A counter example would be JSON-LD including a profile.
Simple formats can be specified directly in the application.properties using the parameter SimpleFormatters.

Example SimpleFormatters=NTRIPLES\*application/n-triples &#124; RDF_JSON\*application/rdf+json

1. The String is split at the &#124;

- NTRIPLES\*application/n-triples
- RDF_JSON\*application/rdf+json

2. The parts then at the *

- NTRIPLES and application/n-triples
- RDF_JSON and application/rdf+json

The first part then must be identical to the Format name in the enum Formats (it gets determined by Formats.valueOf())
The second part is the String to recognize it in Accept/Content-Type.

- Format.NTRIPPLES and format String = application/n-triples
- Format.RDF_JSON and format String = applictaion/rdf+json

Since there is no way to further process any additional information in the format String this way, this can only be
done with these "simple formats"