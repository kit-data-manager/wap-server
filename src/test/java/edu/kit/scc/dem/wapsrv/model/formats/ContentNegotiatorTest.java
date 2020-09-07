package edu.kit.scc.dem.wapsrv.model.formats;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * Tests the class ContentNegotiator
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WapServerConfig.class, JsonLdProfileRegistry.class, FormatRegistry.class,
      JsonLdFormatter.class, TurtleFormatter.class})
@ActiveProfiles("test")
class ContentNegotiatorTest {
   private static ContentNegotiator objContentNegotiator;
   private static final String PARAM_ACCEPT_REAL = "application/ld+json; "
         + "profile=\"http://www.w3.org/ns/ldp.jsonld http://www.w3.org/ns/anno.jsonld\", text/turtle";
   @Autowired
   private JsonLdProfileRegistry profileRegistry;
   @Autowired
   private FormatRegistry formatRegistry;

   /**
    * Test content negotiator.
    */
   @SuppressWarnings("unchecked")
   @Test
   final void testContentNegotiator() {
      // REFLECTION
      String paramAccept;
      Type paramType;
      String actualAccept;
      List<Formatter> actualFormatters;
      boolean actualClientSelected;
      // test null for all types
      paramAccept = null;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            actualAccept = (String) FieldUtils.readDeclaredField(objContentNegotiator, "accept", true);
            actualFormatters = (List<Formatter>) FieldUtils.readDeclaredField(objContentNegotiator, "formatters", true);
            actualClientSelected = (boolean) FieldUtils.readDeclaredField(objContentNegotiator, "clientSelected", true);
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         assertNull(actualAccept, "Constructor did not set accept to null for type: " + paramType.toString());
         assertFalse(actualClientSelected,
               "Constructor did not set clientSelected to false for type: " + paramType.toString());
         assertTrue(actualFormatters.toArray().length > 0,
               "Constructor did not add a formatter for type: " + paramType.toString());
      }
      // test real accept header for all types
      paramAccept = PARAM_ACCEPT_REAL;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            actualAccept = (String) FieldUtils.readDeclaredField(objContentNegotiator, "accept", true);
            actualFormatters = (List<Formatter>) FieldUtils.readDeclaredField(objContentNegotiator, "formatters", true);
            actualClientSelected = (boolean) FieldUtils.readDeclaredField(objContentNegotiator, "clientSelected", true);
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         assertEquals(actualAccept, paramAccept,
               "Constructor did change accept value for type: " + paramType.toString());
         assertTrue(actualClientSelected,
               "Constructor did not set clientSelected to true for type: " + paramType.toString());
         assertTrue(actualFormatters.toArray().length > 0,
               "Constructor did not add a formatter for type: " + paramType.toString());
      }
   }

   /**
    * Test if is client selected.
    */
   @Test
   final void testIsClientSelected() {
      // REFLECTION
      String paramAccept;
      Type paramType;
      boolean actual;
      boolean expected;
      // test null for all types
      paramAccept = null;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            expected = (boolean) FieldUtils.readDeclaredField(objContentNegotiator, "clientSelected", true);
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         actual = objContentNegotiator.isClientSelected();
         assertEquals(actual, expected,
               "For Type: " + paramType.toString() + " expected: " + expected + " but was: " + actual);
      }
      // test real accept header for all types
      paramAccept = PARAM_ACCEPT_REAL;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            expected = (boolean) FieldUtils.readDeclaredField(objContentNegotiator, "clientSelected", true);
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         actual = objContentNegotiator.isClientSelected();
         assertEquals(actual, expected,
               "For Type: " + paramType.toString() + " expected: " + expected + " but was: " + actual);
      }
   }

   /**
    * Test get accept.
    */
   @Test
   final void testGetAccept() {
      // REFLECTION
      String paramAccept;
      Type paramType;
      String actual;
      String expected;
      // test null for all types
      paramAccept = null;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            expected = (String) FieldUtils.readDeclaredField(objContentNegotiator, "accept", true);
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         actual = objContentNegotiator.getAccept();
         assertEquals(actual, expected,
               "For Type: " + paramType.toString() + " expected: " + expected + " but was: " + actual);
      }
      // test real accept header for all types
      paramAccept = PARAM_ACCEPT_REAL;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            expected = (String) FieldUtils.readDeclaredField(objContentNegotiator, "accept", true);
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         actual = objContentNegotiator.getAccept();
         assertEquals(actual, expected,
               "For Type: " + paramType.toString() + " expected: " + expected + " but was: " + actual);
      }
   }

   /**
    * Test get formatters.
    */
   @SuppressWarnings("unchecked")
   @Test
   final void testGetFormatters() {
      // REFLECTION
      String paramAccept;
      Type paramType;
      List<Formatter> actual;
      List<Formatter> expected;
      // test null for all types
      paramAccept = null;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            expected = (List<Formatter>) FieldUtils.readDeclaredField(objContentNegotiator, "formatters", true);
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         actual = objContentNegotiator.getFormatters();
         assertEquals(actual, expected, "For Type: " + paramType.toString() + " expected: " + expected.toString()
               + " but was: " + actual.toString());
      }
      // test real accept header for all types
      paramAccept = PARAM_ACCEPT_REAL;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            expected = (List<Formatter>) FieldUtils.readDeclaredField(objContentNegotiator, "formatters", true);
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         actual = objContentNegotiator.getFormatters();
         assertEquals(actual, expected, "For Type: " + paramType.toString() + " expected: " + expected.toString()
               + " but was: " + actual.toString());
      }
   }

   /**
    * Test get formatter.
    */
   @SuppressWarnings("unchecked")
   @Test
   final void testGetFormatter() {
      // REFLECTION
      String paramAccept;
      Type paramType;
      Formatter actual;
      Formatter expected;
      List<Formatter> expectedList;
      // test null for all types
      paramAccept = null;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            expectedList = (List<Formatter>) FieldUtils.readDeclaredField(objContentNegotiator, "formatters", true);
            expected = (Formatter) expectedList.toArray()[0];
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         actual = objContentNegotiator.getFormatter();
         assertEquals(actual, expected, "For Type: " + paramType.toString() + " expected: " + expected.toString()
               + " but was: " + actual.toString());
      }
      // test real accept header for all types
      paramAccept = PARAM_ACCEPT_REAL;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            expectedList = (List<Formatter>) FieldUtils.readDeclaredField(objContentNegotiator, "formatters", true);
            expected = (Formatter) expectedList.toArray()[0];
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         actual = objContentNegotiator.getFormatter();
         assertEquals(actual, expected, "For Type: " + paramType.toString() + " expected: " + expected.toString()
               + " but was: " + actual.toString());
      }
   }

   /**
    * Test get Q value.
    */
   @Test
   final void testGetQValue() {
      // Q Value extraction share the code between all formatters, because it is not done by individual formatters
      // if it works for one, it works for all. the parameter type is only needed to select default profiles if needed
      // check the first is selected with q=1
      final String acceptJson = "application/ld+json; "
            + "profile=\"http://www.w3.org/ns/ldp.jsonld http://www.w3.org/ns/anno.jsonld\", text/turtle";
      ContentNegotiator contentNegotiator
            = new ContentNegotiator(acceptJson, Type.ANNOTATION, profileRegistry, formatRegistry);
      assertEquals(Format.JSON_LD, contentNegotiator.getFormatter().getFormat());
      assertEquals(1.0, contentNegotiator.getQValue(contentNegotiator.getFormatter()));
      // again, but now with turtle
      final String acceptTurtle = "text/turtle, application/ld+json; "
            + "profile=\"http://www.w3.org/ns/ldp.jsonld http://www.w3.org/ns/anno.jsonld\"";
      contentNegotiator = new ContentNegotiator(acceptTurtle, Type.ANNOTATION, profileRegistry, formatRegistry);
      assertEquals(Format.TURTLE, contentNegotiator.getFormatter().getFormat());
      assertEquals(1.0, contentNegotiator.getQValue(contentNegotiator.getFormatter()));
      // And now select the second one by marking the first with q=0.5 (1.0 is default)
      final String acceptTurtleQ = "application/ld+json; "
            + "profile=\"http://www.w3.org/ns/ldp.jsonld http://www.w3.org/ns/anno.jsonld\"; q=0.5, text/turtle";
      contentNegotiator = new ContentNegotiator(acceptTurtleQ, Type.ANNOTATION, profileRegistry, formatRegistry);
      assertEquals(Format.TURTLE, contentNegotiator.getFormatters().get(0).getFormat());
      assertEquals(Format.JSON_LD, contentNegotiator.getFormatters().get(1).getFormat());
      assertEquals(1.0, contentNegotiator.getQValue(contentNegotiator.getFormatters().get(0)));
      assertEquals(0.5, contentNegotiator.getQValue(contentNegotiator.getFormatters().get(1)));
   }

   /**
    * Test to string.
    */
   @SuppressWarnings("unchecked")
   @Test
   final void testToString() {
      // REFLECTION
      String paramAccept;
      Type paramType;
      List<Formatter> paramFormatterList;
      boolean paramClientSelected;
      String expectedAccept;
      String expectedFormatters;
      String expectedClientSelected;
      String actual;
      boolean containsAccept;
      boolean containsFormatters;
      boolean containsClientSelected;
      // test null for all types
      paramAccept = null;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            expectedAccept = "" + (String) FieldUtils.readDeclaredField(objContentNegotiator, "accept", true);
            paramFormatterList
                  = (List<Formatter>) FieldUtils.readDeclaredField(objContentNegotiator, "formatters", true);
            paramClientSelected = (boolean) FieldUtils.readDeclaredField(objContentNegotiator, "clientSelected", true);
            expectedFormatters = "" + paramFormatterList;
            expectedClientSelected = "" + paramClientSelected;
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         actual = objContentNegotiator.toString();
         containsAccept = actual.contains(expectedAccept);
         containsFormatters = actual.contains(expectedFormatters);
         containsClientSelected = actual.contains(expectedClientSelected);
         assertTrue(containsAccept, "For Type: " + paramType.toString() + " for Field: accept ");
         assertTrue(containsFormatters, "For Type: " + paramType.toString() + " for Field: formatters ");
         assertTrue(containsClientSelected, "For Type: " + paramType.toString() + " for Field: clientSelected ");
      }
      // test real accept header for all types
      paramAccept = PARAM_ACCEPT_REAL;
      for (Type item : FormattableObject.Type.values()) {
         paramType = item;
         objContentNegotiator = new ContentNegotiator(paramAccept, paramType, profileRegistry, formatRegistry);
         try {
            expectedAccept = "" + (String) FieldUtils.readDeclaredField(objContentNegotiator, "accept", true);
            paramFormatterList
                  = (List<Formatter>) FieldUtils.readDeclaredField(objContentNegotiator, "formatters", true);
            paramClientSelected = (boolean) FieldUtils.readDeclaredField(objContentNegotiator, "clientSelected", true);
            expectedFormatters = "" + paramFormatterList;
            expectedClientSelected = "" + paramClientSelected;
         } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return;
         }
         actual = objContentNegotiator.toString();
         containsAccept = actual.contains(expectedAccept);
         containsFormatters = actual.contains(expectedFormatters);
         containsClientSelected = actual.contains(expectedClientSelected);
         assertTrue(containsAccept, "For Type: " + paramType.toString() + " for Field: accept ");
         assertTrue(containsFormatters, "For Type: " + paramType.toString() + " for Field: formatters ");
         assertTrue(containsClientSelected, "For Type: " + paramType.toString() + " for Field: clientSelected ");
      }
   }
}
