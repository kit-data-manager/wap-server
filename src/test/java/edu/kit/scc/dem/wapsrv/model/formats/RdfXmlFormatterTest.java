package edu.kit.scc.dem.wapsrv.model.formats;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the class RdfXmlFormatter
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class RdfXmlFormatterTest {
   /**
    * Test RDF XML formatter.
    */
   @Test
   final void testRdfXmlFormatter() {
      RdfXmlFormatter actual;
      actual = null;
      actual = new RdfXmlFormatter();
      assertNotNull(actual, "Construction did fail.");
   }
}
