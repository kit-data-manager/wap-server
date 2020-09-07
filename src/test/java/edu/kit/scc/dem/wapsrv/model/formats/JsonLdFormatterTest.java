package edu.kit.scc.dem.wapsrv.model.formats;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the class JsonLdFormatter
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class JsonLdFormatterTest {
   private static JsonLdFormatter objJsonLdFormatter = new JsonLdFormatter();

   /**
    * Test JSON-LD formatter.
    */
   @Test
   final void testJsonLdFormatter() {
      JsonLdFormatter actual;
      actual = null;
      actual = new JsonLdFormatter();
      assertNotNull(actual, "Construction did fail.");
   }

   /**
    * Test set profile registry.
    */
   @Test
   final void testSetProfileRegistry() {
      JsonLdProfileRegistry paramJsonLdProfileRegistry;
      paramJsonLdProfileRegistry = new JsonLdProfileRegistry();
      assertNotNull(paramJsonLdProfileRegistry, "Could not create new JsonLdProfileRegistry.");
      objJsonLdFormatter.setProfileRegistry(paramJsonLdProfileRegistry);
   }

   /**
    * Test format.
    */
   @Test
   final void testFormat() {
      // DOTEST write the test for this method
   }

   /**
    * Test set accept part.
    */
   @Test
   final void testSetAcceptPart() {
      // DOTEST write the test for this method
   }

   /**
    * Test get content type.
    */
   @Test
   final void testGetContentType() {
      // DOTEST write the test for this method
   }

   /**
    * Test get format string.
    */
   @Test
   final void testGetFormatString() {
      // DOTEST write the test for this method
   }
}
