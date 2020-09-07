package edu.kit.scc.dem.wapsrv.model.formats;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;

/**
 * Tests the class SimpleFormatter
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class SimpleFormatterTest {
   private static SimpleFormatter objSimpleFormatter;

   /**
    * Test simple formatter.
    */
   @Test
   final void testSimpleFormatter() {
      SimpleFormatter actual;
      // test for all types
      for (Format paramFormat : Format.values()) {
         actual = null;
         actual = new SimpleFormatter(paramFormat, paramFormat.toString());
         assertNotNull(actual, "Construction did fail for Format: " + paramFormat);
      }
   }

   /**
    * Test get format string.
    */
   @Test
   final void testGetFormatString() {
      String actual;
      // test for all types
      for (Format paramFormat : Format.values()) {
         objSimpleFormatter = new SimpleFormatter(paramFormat, paramFormat.toString());
         actual = objSimpleFormatter.getFormatString();
         assertEquals(actual, paramFormat.toString(), "Expected: " + paramFormat + " but was: " + actual);
      }
   }

   /**
    * Test format.
    */
   @Test
   final void testFormat() {
      FormattableObject paramObj;
      String actual;
      // test for all types
      for (Format paramFormat : Format.values()) {
         objSimpleFormatter = new SimpleFormatter(paramFormat, paramFormat.toString());
         paramObj = new ParamFormattableObject();
         actual = objSimpleFormatter.format(paramObj);
         assertEquals(actual, paramFormat.toString(), "Expected: " + paramFormat + " but was: " + actual);
      }
   }

   /**
    * Test set accept part.
    */
   @Test
   final void testSetAcceptPart() {
      String paramAcceptPart;
      // test for all format types
      for (Format paramFormat : Format.values()) {
         objSimpleFormatter = new SimpleFormatter(paramFormat, paramFormat.toString());
         // test for all FormattableObjects types
         for (Type paramType : FormattableObject.Type.values()) {
            paramAcceptPart = "";
            objSimpleFormatter.setAcceptPart(paramAcceptPart, paramType);
            assertTrue(objSimpleFormatter.isValid(), "Should be always valid.");
         }
      }
   }

   /**
    * Test get content type.
    */
   @Test
   final void testGetContentType() {
      String actual;
      // test for all format types
      for (Format paramFormat : Format.values()) {
         objSimpleFormatter = new SimpleFormatter(paramFormat, paramFormat.toString());
         actual = objSimpleFormatter.getContentType();
         if(!actual.startsWith(paramFormat.toString())){
           fail("Format string was unexpected changed.");
         }
      }
   }

   private static class ParamFormattableObject implements FormattableObject {
      @Override
      public String toString(Format format) {
         return format.toString();
      }

      @Override
      public Type getType() {
         // not needed for testing
         return null;
      }
   }
}
