package edu.kit.scc.dem.wapsrv.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests the class IllegalHttpParameterException
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class IllegalHttpParameterExceptionTest {
   private static final Set<String> PARAMSTRINGLIST = WapExceptionTest.PARAMSTRINGLIST;

   /**
    * Test container not empty exception.
    */
   @Test
   final void testIllegalHttpParameterException() {
      Throwable paramThrowable = new Throwable();
      IllegalHttpParameterException actual;
      // test for all Strings in paramStringList
      for (String paramString : PARAMSTRINGLIST) {
         // test for constructor String
         actual = null;
         actual = new IllegalHttpParameterException(paramString);
         assertNotNull(actual, "Constrution did fail for String: " + paramString);
         // test for constructor String Throwable
         actual = null;
         actual = new IllegalHttpParameterException(paramString, paramThrowable);
         assertNotNull(actual, "Constrution did fail for String: " + paramString);
         // test for constructor String Throwable boolean boolean
         actual = null;
         actual = new IllegalHttpParameterException(paramString, paramThrowable, false, false);
         assertNotNull(actual, "Constrution did fail for String: " + paramString);
      }
   }
}
