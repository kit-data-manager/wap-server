package edu.kit.scc.dem.wapsrv.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the ErrorMessageRegistry
 *
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class UnallowedPropertyChangeExceptionTest {
   /**
    * Test unallowed property change exception.
    */
   @Test
   final void testUnallowedPropertyChangeException() {
      assertEquals("testmsg", new UnallowedPropertyChangeException("testmsg").getMessage());
   }
}
