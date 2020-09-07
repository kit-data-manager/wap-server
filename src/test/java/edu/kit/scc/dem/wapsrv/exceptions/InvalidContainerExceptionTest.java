package edu.kit.scc.dem.wapsrv.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests the class InvalidContainerExceptionTest
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class InvalidContainerExceptionTest {
   private static final Set<String> PARAMSTRINGLIST = WapExceptionTest.PARAMSTRINGLIST;

   /**
    * Test container not empty exception.
    */
   @Test
   final void testInvalidContainerException() {
      InvalidContainerException actual;
      // test for all Strings in paramStringList
      for (String paramString : PARAMSTRINGLIST) {
         actual = null;
         actual = new InvalidContainerException(paramString);
         assertNotNull(actual, "Constrution did fail for String: " + paramString);
      }
   }
}
