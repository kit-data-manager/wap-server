package edu.kit.scc.dem.wapsrv.model.validators;

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
 * Tests the class JsonLdValidator
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
class JsonLdValidatorTest {
   private static JsonLdValidator jsonLdValidator = initObjJsonLdValidator();
   @Autowired
   private JsonLdProfileRegistry profileRegistry;

   private static JsonLdValidator initObjJsonLdValidator() {
      return new JsonLdValidator(WapServerConfig.getInstance());
   }

   private String expand(String string) {
      String erg = profileRegistry.expandJsonLd(string);
      // Expansions adds [] around, which is not usable in the test case
      return erg.substring(1, erg.length() - 1);
   }

   /**
    * Test LSON ld validator.
    */
   @Test
   final void testJsonLdValidator() {
      JsonLdValidator actual;
      actual = initObjJsonLdValidator();
      assertNotNull(actual, "Construction did fail.");
   }

   /**
    * Test validate annotation.
    */
   @Test
   final void testValidateAnnotation() {
      String paramAnnotationString;
      boolean actual;
      paramAnnotationString = TestDataStore.getAnnotation("example70_realAnnoWithPicture.jsonld");
      actual = jsonLdValidator.validateAnnotation(paramAnnotationString);
      assertTrue(actual, "Test Annotation should be valid.");
   }

   /**
    * Test validate container.
    */
   @Test
   final void testValidateContainer() {
      String paramcontainerString;
      boolean actual;
      paramcontainerString = expand(TestDataStore.getContainer("example1.jsonld"));
      actual = jsonLdValidator.validateContainer(paramcontainerString);
      assertTrue(actual, "Test Container should be valid.");
   }

   /**
    * Test get format.
    */
   @Test
   final void testGetFormat() {
      Format actual;
      actual = jsonLdValidator.getFormat();
      assertEquals(actual, Format.JSON_LD, "Format expected: " + Format.JSON_LD + ", but was " + actual);
   }
}
