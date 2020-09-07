package edu.kit.scc.dem.wapsrv.model.formats;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the class TurtleFormatter
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class TurtleFormatterTest {
   /**
    * Test turtle formatter.
    */
   @Test
   final void testTurtleFormatter() {
      TurtleFormatter actual;
      actual = null;
      actual = new TurtleFormatter();
      assertNotNull(actual, "Construction did fail.");
   }
}
