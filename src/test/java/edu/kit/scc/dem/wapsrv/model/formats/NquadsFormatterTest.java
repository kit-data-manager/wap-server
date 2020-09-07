package edu.kit.scc.dem.wapsrv.model.formats;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the class NquadsFormatter
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class NquadsFormatterTest {
   /**
    * Test nquads formatter.
    */
   @Test
   final void testNquadsFormatter() {
      NquadsFormatter actual;
      actual = null;
      actual = new NquadsFormatter();
      assertNotNull(actual, "Construction did fail.");
   }
}
