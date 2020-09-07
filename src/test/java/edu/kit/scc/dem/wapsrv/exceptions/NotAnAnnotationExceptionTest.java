package edu.kit.scc.dem.wapsrv.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests the class NotAnAnnotationException
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class NotAnAnnotationExceptionTest {
   private static final Set<String> PARAMSTRINGLIST = WapExceptionTest.PARAMSTRINGLIST;

   /**
    * Test container not empty exception.
    */
   @Test
   final void testNotAnAnnotationException() {
      NotAnAnnotationException actual;
      // test for default constructor
      actual = null;
      actual = new NotAnAnnotationException();
      assertNotNull(actual, "Constrution did fail for default constructor.");
      // test for all Strings in paramStringList
      for (String paramString : PARAMSTRINGLIST) {
         actual = null;
         actual = new NotAnAnnotationException(paramString);
         assertNotNull(actual, "Constrution did fail for String: " + paramString);
      }
   }
}
