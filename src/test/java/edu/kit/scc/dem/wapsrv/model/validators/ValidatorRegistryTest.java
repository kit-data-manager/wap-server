package edu.kit.scc.dem.wapsrv.model.validators;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * Tests the class ValidatorRegistry
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ValidatorRegistry.class, WapServerConfig.class, JsonLdValidator.class})
@ActiveProfiles("test")
class ValidatorRegistryTest {
   @Autowired
   private ValidatorRegistry objValidatorRegistry;

   /**
    * Test get instance.
    */
   @Test
   final void testGetInstance() {
      ValidatorRegistry actual;
      actual = null;
      actual = ValidatorRegistry.getInstance();
      assertNotNull(actual, "Instance should not be null.");
   }

   /**
    * Test get supported formats.
    */
   @Test
   final void testGetSupportedFormats() {
      Set<Format> actual;
      actual = null;
      actual = objValidatorRegistry.getSupportedFormats();
      assertNotNull(actual, "Could not get supported formats.");
      assertTrue(actual.toArray().length > 0, "Supported formats should not be empty.");
   }

   /**
    * Test get validator.
    */
   @Test
   final void testGetValidator() {
      Format paramFormat;
      Validator actual;
      // test for format JSON-LD
      paramFormat = Format.JSON_LD;
      actual = null;
      actual = objValidatorRegistry.getValidator(paramFormat);
      assertNotNull(actual, "Could not get validator for format: " + paramFormat);
      // test for not supported format TURTLE
      paramFormat = Format.TURTLE;
      actual = null;
      actual = objValidatorRegistry.getValidator(paramFormat);
      assertNull(actual, "Should ne null for not supported format: " + paramFormat);
      // DOTEST write the test for this method
   }
}
