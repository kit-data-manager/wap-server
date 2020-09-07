package edu.kit.scc.dem.wapsrv.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests the class InvalidRequestException
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class InvalidRequestExceptionTest {
   private static final Set<String> PARAMSTRINGLIST = WapExceptionTest.PARAMSTRINGLIST;

   /**
    * Test container not empty exception.
    */
   @Test
   final void testInvalidRequestException() {
      InvalidRequestException actual;
      // test for all Strings in paramStringList
      for (String paramString : PARAMSTRINGLIST) {
         actual = null;
         actual = new InvalidRequestException(paramString);
         assertNotNull(actual, "Constrution did fail for String: " + paramString);
      }
   }
}
