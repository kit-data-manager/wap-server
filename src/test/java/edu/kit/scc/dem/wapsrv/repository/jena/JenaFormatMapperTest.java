package edu.kit.scc.dem.wapsrv.repository.jena;

import static org.junit.jupiter.api.Assertions.*;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.Test;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * Tests the class JenaFormatMapper
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class JenaFormatMapperTest {
   /**
    * Test map.
    */
   @Test
   final void testMap() {
      Format paramFormat;
      Lang actual;
      Lang expected;
      // test format == null
      paramFormat = null;
      actual = null;
      actual = JenaFormatMapper.map(paramFormat);
      assertNull(actual, "Should be null for format: " + paramFormat);
      // test format JSON-LD
      paramFormat = Format.JSON_LD;
      actual = null;
      actual = JenaFormatMapper.map(paramFormat);
      assertNotNull(actual, "Should be not null for format: " + paramFormat);
      expected = Lang.JSONLD;
      assertEquals(actual, expected, "Expected " + expected + ", but was " + actual);
   }
}
