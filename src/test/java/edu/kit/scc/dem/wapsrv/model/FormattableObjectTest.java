package edu.kit.scc.dem.wapsrv.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;

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
class FormattableObjectTest {
   /**
    * Test Enum FormattableObject
    */
   @Test
   final void testEnum() {
      String result = "";
      String expected = "ANNOTATIONPAGECONTAINER";
      for (Type type : FormattableObject.Type.values()) {
         result += type.name();
      }
      assertEquals(result, expected, "Enumeration differs from the expected one.");
   }
}
