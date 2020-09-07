package edu.kit.scc.dem.wapsrv.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the class IllegalPageIriExceptionTest
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class IllegalPageIriExceptionTest {
   /**
    * Test container not empty exception.
    */
   @Test
   final void testIllegalPageIriException() {
      Throwable paramThrowable = new Throwable();
      IllegalPageIriException actual;
      // test default constructor
      actual = null;
      actual = new IllegalPageIriException();
      assertNotNull(actual, "Constrution did fail.");
      // test constructor Throwable
      actual = null;
      actual = new IllegalPageIriException(paramThrowable);
      assertNotNull(actual, "Constrution did fail.");
   }
}
