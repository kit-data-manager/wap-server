package edu.kit.scc.dem.wapsrv.model;

import static org.junit.jupiter.api.Assertions.*;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
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
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRdfBackend;
import edu.kit.scc.dem.wapsrv.testscommon.TestDataStore;

/**
 * Tests the interface AnnotationList
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
class AnnotationListTest {
   @Autowired
   private ModelFactory modelFactory;
   @Autowired
   private JsonLdProfileRegistry profileRegistry;

   private AnnotationList createAnnotationList(int size) {
      StringBuilder builder = new StringBuilder();
      builder.append("[\n");
      for (int n = 0; n < size; n++) {
         builder.append(
               expand(TestDataStore.getAnnotation("example" + (n + 1) + ".jsonld")) + (n < size - 1 ? "," : "") + "\n");
      }
      builder.append("]");
      String annoListString = builder.toString();
      return modelFactory.createAnnotationList(annoListString, Format.JSON_LD);
   }

   private String expand(String string) {
      String erg = profileRegistry.expandJsonLd(string);
      // Expansions adds [] around, which is not usable in the test case
      return erg.substring(1, erg.length() - 1);
   }

   /**
    * Test size.
    */
   @Test
   final void testSize() {
      final int size = 5;
      AnnotationList annoList = createAnnotationList(size);
      assertEquals(annoList.size(), size, "Size is not correct");
   }

   /**
    * Test get IRI.
    */
   @Test
   final void testGetIri() {
      AnnotationList annoList = createAnnotationList(1);
      Annotation anno = annoList.getAnnotations().get(0);
      annoList.setContainerIri("http://www.example.org/container1/");
      assertEquals(anno.getIri().toString(), annoList.getIri().toString(), "The IRI equals not the expected string");
      annoList = createAnnotationList(2);
      annoList.setContainerIri("http://www.example.org/container1/");
      assertEquals("<http://www.example.org/container1/>", annoList.getIri().toString(),
            "The IRI equals not the expected string");
   }

   /**
    * Test get IRI string.
    */
   @Test
   final void testGetIriString() {
      AnnotationList annoList = createAnnotationList(1);
      Annotation anno = annoList.getAnnotations().get(0);
      annoList.setContainerIri("http://www.example.org/container1/");
      assertEquals(anno.getIriString(), annoList.getIriString(), "The IRI equals not the expected string");
      annoList = createAnnotationList(2);
      annoList.setContainerIri("http://www.example.org/container1/");
      assertEquals("http://www.example.org/container1/", annoList.getIriString(),
            "The IRI equals not the expected string");
   }

   /**
    * Test get ETAG.
    */
   @Test
   final void testGetEtag() {
      AnnotationList annoList = createAnnotationList(1);
      Annotation anno = annoList.getAnnotations().get(0);
      annoList.setContainerEtag("123");
      assertEquals(anno.getEtag(), annoList.getEtag(), "The ETAG equals not the expected string");
      annoList = createAnnotationList(2);
      annoList.setContainerEtag("123");
      assertEquals("123", annoList.getEtag(), "The ETAG equals not the expected string");
   }

   /**
    * Test get quoted ETAG.
    */
   @Test
   final void testGetEtagQuoted() {
      AnnotationList annoList = createAnnotationList(1);
      Annotation anno = annoList.getAnnotations().get(0);
      annoList.setContainerEtag("123");
      assertEquals(anno.getEtagQuoted(), annoList.getEtagQuoted(), "The ETAG equals not the expected quoted string");
      annoList = createAnnotationList(2);
      annoList.setContainerEtag("123");
      assertEquals("\"123\"", annoList.getEtagQuoted(), "The ETAG equals not the expected quoted string");
   }
}
