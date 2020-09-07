package edu.kit.scc.dem.wapsrv.model.formats;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;

/**
 * Tests the class AbstractFormatter
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class AbstractFormatterTest {
   /**
    * Test abstract formatter.
    */
   @Test
   final void testAbstractFormatter() {
      AbstractFormatter actual;
      // test for JSON-LD
      actual = null;
      actual = new ImplAbstractFormatter(Format.JSON_LD);
      assertNotNull(actual, "Construction did fail.");
   }

   /**
    * Test get format.
    */
   @Test
   final void testGetFormat() {
      AbstractFormatter objAbstractFormatter;
      Format paramFormat;
      Format actual;
      // setup for JSON-LD
      paramFormat = Format.JSON_LD;
      objAbstractFormatter = null;
      objAbstractFormatter = new ImplAbstractFormatter(paramFormat);
      assertNotNull(objAbstractFormatter, "Could not create AbstractFormatter for format: " + paramFormat);
      // test default
      actual = null;
      actual = objAbstractFormatter.getFormat();
      assertNotNull(actual, "Could not get format.");
      assertEquals(paramFormat, actual);
   }

   /**
    * Test set valid.
    */
   @Test
   final void testSetValid() {
      AbstractFormatter objAbstractFormatter;
      Format paramFormat;
      boolean actual;
      // setup for JSON-LD
      paramFormat = Format.JSON_LD;
      objAbstractFormatter = null;
      objAbstractFormatter = new ImplAbstractFormatter(paramFormat);
      assertNotNull(objAbstractFormatter, "Could not create AbstractFormatter for format: " + paramFormat);
      // test set valid true
      objAbstractFormatter.setValid(true);
      actual = objAbstractFormatter.isValid();
      assertTrue(actual, "Should be true after set to true.");
      // test set valid false
      objAbstractFormatter.setValid(false);
      actual = objAbstractFormatter.isValid();
      assertFalse(actual, "Should be false after set to false.");
   }

   /**
    * Test if is valid.
    */
   @Test
   final void testIsValid() {
      // already tested in testSetValid
   }

   private class ImplAbstractFormatter extends AbstractFormatter {
      protected ImplAbstractFormatter(Format format) {
         super(format);
      }

      @Override
      public String getFormatString() {
         // from interface Formatter - not needed for testing
         return null;
      }

      @Override
      public String format(FormattableObject obj) {
         // from interface Formatter - not needed for testing
         return null;
      }

      @Override
      public void setAcceptPart(String acceptPart, Type type) {
         // from interface Formatter - not needed for testing
      }

      @Override
      public String getContentType() {
         // from interface Formatter - not needed for testing
         return null;
      }
   }
}
