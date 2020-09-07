package edu.kit.scc.dem.wapsrv.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests the class NotAContainerException
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class NotAContainerExceptionTest {
   private static final Set<String> PARAMSTRINGLIST = WapExceptionTest.PARAMSTRINGLIST;

   /**
    * Test container not empty exception.
    */
   @Test
   final void testNotAContainerException() {
      NotAContainerException actual;
      // test for default constructor
      actual = null;
      actual = new NotAContainerException();
      assertNotNull(actual, "Constrution did fail for default constructor.");
      // test for all Strings in paramStringList
      for (String paramString : PARAMSTRINGLIST) {
         // test for constructor String
         actual = null;
         actual = new NotAContainerException(paramString);
         assertNotNull(actual, "Constrution did fail for String: " + paramString);
      }
   }
}
