package edu.kit.scc.dem.wapsrv.model;

import static org.junit.jupiter.api.Assertions.*;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.apache.commons.rdf.api.IRI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdFormatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfModelFactory;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AnnoVocab;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRdfBackend;
import edu.kit.scc.dem.wapsrv.testscommon.TestDataStore;

/**
 * Tests the interface Container
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {JenaRdfBackend.class, RdfModelFactory.class, WapServerConfig.class, FormatRegistry.class,
      JsonLdProfileRegistry.class, JsonLdFormatter.class})
@ExtendWith(HoverflyExtension.class)
@HoverflySimulate(source = @HoverflySimulate.Source(value = "w3c_simulation.json", type = HoverflySimulate.SourceType.DEFAULT_PATH))
@ActiveProfiles("test")
class WapObjectTest {
   @Autowired
   private ModelFactory modelFactory;
   @Autowired
   private JsonLdProfileRegistry profileRegistry;

   private String expand(String string) {
      String erg = profileRegistry.expandJsonLd(string);
      // Expansions adds [] around, which is not usable in the test case
      return erg.substring(1, erg.length() - 1);
   }

   /**
    * Test get IRI string.
    */
   @Test
   final void testGetIriString() {
      String iri = "http://www.example.org/container1/";
      Annotation anno;
      anno = modelFactory.createAnnotation(
            expand(TestDataStore.getAnnotation(1 + (int) (System.currentTimeMillis() % 6))), Format.JSON_LD);
      anno.setIri(iri);
      anno.getIriString();
      assertEquals(iri, anno.getIriString(), "IRI is different from expected IRI");
   }

   /**
    * Test is property with multiple values equal.
    */
   @Test
   final void testIsPropertyWithMultipleValuesEqual() {
      IRI bodyIri = AnnoVocab.body;
      String anno1String = expand("{" + "\"@context\": \"http://www.w3.org/ns/anno.jsonld\","
            + "\"id\": \"http://example.org/anno1\"," + "\"type\": \"Annotation\","
            + "\"body\": [\"http://example.org/post1\", \"http://example.org/post2\"],"
            + "\"target\": \"http://example.com/page1\"" + "}");
      String anno2String = expand("{" + "\"@context\": \"http://www.w3.org/ns/anno.jsonld\","
            + "\"id\": \"http://example.org/anno1\"," + "\"type\": \"Annotation\","
            + "\"body\": [\"http://example.org/post2\", \"http://example.org/post1\"],"
            + "\"target\": \"http://example.com/page1\"" + "}");
      String anno3String = expand("{" + "\"@context\": \"http://www.w3.org/ns/anno.jsonld\","
            + "\"id\": \"http://example.org/anno1\"," + "\"type\": \"Annotation\","
            + "\"body\": [\"http://example.org/post3\", \"http://example.org/post2\"],"
            + "\"target\": \"http://example.com/page1\"" + "}");
      String anno4String = expand("{" + "\"@context\": \"http://www.w3.org/ns/anno.jsonld\","
            + "\"id\": \"http://example.org/anno1\"," + "\"type\": \"Annotation\","
            + "\"body\": [\"http://example.org/post1\", \"http://example.org/post2\", \"http://example.org/post3\"],"
            + "\"target\": \"http://example.com/page1\"" + "}");
      Annotation anno1;
      Annotation anno2;
      Annotation anno3;
      Annotation anno4;
      anno1 = modelFactory.createAnnotation(anno1String, Format.JSON_LD);
      anno2 = modelFactory.createAnnotation(anno2String, Format.JSON_LD);
      anno3 = modelFactory.createAnnotation(anno3String, Format.JSON_LD);
      anno4 = modelFactory.createAnnotation(anno4String, Format.JSON_LD);
      Boolean result1 = anno1.isPropertyWithMultipleValuesEqual(anno1, bodyIri);
      assertTrue(result1, "Expected to be true");
      Boolean result2 = anno1.isPropertyWithMultipleValuesEqual(anno2, bodyIri);
      assertTrue(result2, "Expected to be true");
      Boolean result3 = anno1.isPropertyWithMultipleValuesEqual(anno3, bodyIri);
      assertFalse(result3, "Expected to be false");
      Boolean result4 = anno1.isPropertyWithMultipleValuesEqual(anno4, bodyIri);
      assertFalse(result4, "Expected to be false");
   }

   /**
    * Test is property equal.
    */
   @Test
   final void testIsPropertyEqual() {
      IRI bodyIri = AnnoVocab.body;
      String anno1String = expand("{" + "\"@context\": \"http://www.w3.org/ns/anno.jsonld\","
            + "\"id\": \"http://example.org/anno1\"," + "\"type\": \"Annotation\","
            + "\"body\": \"http://example.org/post1\"," + "\"target\": \"http://example.com/page1\"" + "}");
      String anno2String = expand("{" + "\"@context\": \"http://www.w3.org/ns/anno.jsonld\","
            + "\"id\": \"http://example.org/anno1\"," + "\"type\": \"Annotation\","
            + "\"body\": \"http://example.org/post2\"," + "\"target\": \"http://example.com/page1\"" + "}");
      String anno3String = expand(
            "{" + "\"@context\": \"http://www.w3.org/ns/anno.jsonld\"," + "\"id\": \"http://example.org/anno1\","
                  + "\"type\": \"Annotation\"," + "\"target\": \"http://example.com/page1\"" + "}");
      String anno4String = expand(
            "{" + "\"@context\": \"http://www.w3.org/ns/anno.jsonld\"," + "\"id\": \"http://example.org/anno1\","
                  + "\"type\": \"Annotation\"," + "\"target\": \"http://example.com/page1\"" + "}");
      Annotation anno1;
      Annotation anno2;
      Annotation anno3;
      Annotation anno4;
      anno1 = modelFactory.createAnnotation(anno1String, Format.JSON_LD);
      anno2 = modelFactory.createAnnotation(anno2String, Format.JSON_LD);
      anno3 = modelFactory.createAnnotation(anno3String, Format.JSON_LD);
      anno4 = modelFactory.createAnnotation(anno4String, Format.JSON_LD);
      Boolean result1 = anno1.isPropertyEqual(anno1, bodyIri);
      assertTrue(result1, "Expected to be true");
      Boolean result2 = anno1.isPropertyEqual(anno2, bodyIri);
      assertFalse(result2, "Expected to be false");
      Boolean result3 = anno3.isPropertyEqual(anno4, bodyIri);
      assertTrue(result3, "Expected to be true");
      Boolean result4 = anno3.isPropertyEqual(anno1, bodyIri);
      assertFalse(result4, "Expected to be false");
   }

   /**
    * Test has property.
    */
   @Test
   final void testHasProperty() {
      IRI bodyIri = AnnoVocab.body;
      IRI scopeIri = AnnoVocab.scope;
      Annotation anno;
      anno = modelFactory.createAnnotation(expand(TestDataStore.getAnnotation("example1.jsonld")), Format.JSON_LD);
      assertTrue(anno.hasProperty(bodyIri), "Expected to be true");
      anno = modelFactory.createAnnotation(expand(TestDataStore.getAnnotation("example1.jsonld")), Format.JSON_LD);
      assertFalse(anno.hasProperty(scopeIri), "Expected to be false");
   }

   /**
    * Test get parent container IRI string.
    */
   @Test
   final void testGetParentContainerIriString() {
      String iri1 = "http://www.example.org/container1/";
      String expected1 = "http://www.example.org/";
      WapObject.getParentContainerIriString(iri1);
      assertEquals(expected1, WapObject.getParentContainerIriString(iri1), "IRI string not as expected");
      String iri2 = "http://www.example.org/container1/anno1";
      String expected2 = "http://www.example.org/container1/";
      WapObject.getParentContainerIriString(iri2);
      assertEquals(expected2, WapObject.getParentContainerIriString(iri2), "IRI string not as expected");
   }
}
