package edu.kit.scc.dem.wapsrv.model.formats;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the interface FormattableObjectTest
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class FormatTest {
   /**
    * Test Enum Format
    */
   @Test
   final void test() {
      String result = "";
      String expected = "JSON_LDTURTLERDF_XMLRDF_JSONNTRIPLESNQUADS";
      for (Format type : Format.values()) {
         result += type.name();
      }
      assertEquals(result, expected, "Enumeration differs from the expected one.");
   }
}
