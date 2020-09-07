package edu.kit.scc.dem.wapsrv.model.rdf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDF;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.NotAnAnnotationException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.AnnotationList;
import edu.kit.scc.dem.wapsrv.model.ModelFactory;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdFormatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AnnoVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRdfBackend;

/**
 * Tests the class RdfModelFactory.
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
@ActiveProfiles("test")
class RdfModelFactoryTest {
   /** The model factory. */
   @Autowired
   private ModelFactory modelFactory;
   @Autowired
   private JsonLdProfileRegistry profileRegistry;
   /** The raw annotation. */
   private String rawAnnotation = "{\r\n" + "  \"@context\": \"http://www.w3.org/ns/anno.jsonld\",\r\n"
         + "  \"id\": \"http://example.org/anno1\",\r\n" + "  \"type\": \"Annotation\",\r\n"
         + "  \"body\": \"http://example.org/post1\",\r\n" + "  \"target\": \"http://example.com/page1\"\r\n" + "}";

   private String expand(String string) {
      String erg = profileRegistry.expandJsonLd(string);
      // Expansions adds [] around, which is not usable in the test case
      return erg.substring(1, erg.length() - 1);
   }

   /**
    * Test constructor.
    */
   @Test
   void testConstruct() {
      ModelFactory mf = new RdfModelFactory();
      assertNotNull(mf, "simple construct should not be null");
   }

   /**
    * Test create annotation list.
    */
   @Test
   final void testCreateAnnotationList() {
      assertThrows(NotAnAnnotationException.class, () -> {
         modelFactory.createAnnotationList("{}", Format.JSON_LD);
      });
      AnnotationList a = modelFactory.createAnnotationList(expand(rawAnnotation), Format.JSON_LD);
      assertThat(a.size(), is(1));
   }

   /**
    * Test get RDF.
    */
   @Test
   final void testGetRDF() {
      assertNotNull(modelFactory.getRDF(), "a vaild RDF has to be returned");
   }

   /**
    * Test create annotation dataset.
    */
   @Test
   final void testCreateAnnotationDataset() {
      RDF rdf = modelFactory.getRDF();
      Dataset ds = rdf.createDataset();
      Graph graph = ds.getGraph();
      BlankNodeOrIRI node = rdf.createBlankNode();
      graph.add(node, RdfVocab.type, AnnoVocab.annotation);
      graph.add(node, AnnoVocab.body, rdf.createIRI("http://example.org/post1"));
      graph.add(node, AnnoVocab.target, rdf.createIRI("http://example.com/page1"));
      Annotation a = modelFactory.createAnnotation(ds);
      assertNotNull(a, "the returned Annotation is not allowed to be null");
   }

   /**
    * Test create annotation string format.
    */
   @Test
   final void testCreateAnnotationStringFormat() {
      assertThrows(NotAnAnnotationException.class, () -> {
         modelFactory.createAnnotation("{}", Format.JSON_LD);
      });
      Annotation a = modelFactory.createAnnotation(expand(rawAnnotation), Format.JSON_LD);
      assertNotNull(a, "the returned Annotation is not allowed to be null");
   }

   /**
    * Test is valid input format.
    */
   @Test
   final void testIsValidInputFormat() {
      assertTrue(modelFactory.isValidInputFormat(Format.JSON_LD), "expected true");
   }

   /**
    * Test create page.
    */
   @Test
   final void testCreatePage() {
      // DOTEST write the test for this method
   }

   /**
    * Test create container dataset boolean boolean.
    */
   @Test
   final void testCreateContainerDatasetBooleanBoolean() {
      // DOTEST write the test for this method
   }

   /**
    * Test create container string format.
    */
   @Test
   final void testCreateContainerStringFormat() {
      // DOTEST write the test for this method
   }

   /**
    * Test convert format.
    */
   @Test
   final void testConvertFormat() {
      // DOTEST write the test for this method
   }

   /**
    * Test create container dataset.
    */
   @Test
   final void testCreateContainerDataset() {
      // DOTEST write the test for this method
   }
}
