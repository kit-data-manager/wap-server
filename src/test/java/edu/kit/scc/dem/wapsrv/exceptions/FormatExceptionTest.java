package edu.kit.scc.dem.wapsrv.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests the class FormatException
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class FormatExceptionTest {
   private static final Set<String> PARAMSTRINGLIST = WapExceptionTest.PARAMSTRINGLIST;

   /**
    * Test container not empty exception.
    */
   @Test
   final void testFormatException() {
      Throwable paramThrowable = new Throwable();
      FormatException actual;
      // test for all Strings in paramStringList
      for (String paramString : PARAMSTRINGLIST) {
         // test constructor String
         actual = null;
         actual = new FormatException(paramString);
         assertNotNull(actual, "Constrution did fail for String: " + paramString);
         // test constructor String Throwable
         actual = null;
         actual = new FormatException(paramString, paramThrowable);
         assertNotNull(actual, "Constrution did fail for String: " + paramString);
      }
   }
}
