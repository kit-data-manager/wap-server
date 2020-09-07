package edu.kit.scc.dem.wapsrv.controller;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests the class WapPathMatcher
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class WapPathMatcherTest {
   private static WapPathMatcher objWapPathMatcher = new WapPathMatcher();
   private static List<String> validPatternList = createPatternList(true);
   private static List<String> invalidPatternList = createPatternList(false);

   private static List<String> createPatternList(boolean valid) {
      List<String> paramPatternList = new ArrayList<String>();
      if (valid) {
         paramPatternList.add(WapPathMatcher.ANNOTATION_PATTERN);
         paramPatternList.add(WapPathMatcher.CONTAINER_AND_PAGE_PATTERN);
      } else {
         paramPatternList.add("");
         paramPatternList.add("invalidPatternXYZ");
      }
      return paramPatternList;
   }

   /**
    * Test is pattern.
    */
   @Test
   final void testIsPattern() {
      String paramPath;
      boolean actual;
      // test for all valid pattern in list
      for (String validPattern : validPatternList) {
         paramPath = validPattern;
         actual = objWapPathMatcher.isPattern(paramPath);
         assertTrue(actual, "Path: " + paramPath + ", should be valid pattern.");
      }
      // test for all invalid pattern in list
      for (String invalidPattern : invalidPatternList) {
         paramPath = invalidPattern;
         actual = objWapPathMatcher.isPattern(paramPath);
         assertFalse(actual, "Path: " + paramPath + ", should not be valid pattern.");
      }
   }

   /**
    * Test match.
    */
   @Test
   final void testMatch() {
      String paramPattern;
      String paramPath;
      boolean actual;
      // test for all valid pattern in list
      for (String validPattern : validPatternList) {
         paramPattern = validPattern;
         paramPath = "/wap/" + validPattern;
         actual = objWapPathMatcher.match(paramPattern, paramPath);
         assertTrue(actual, "Path: " + paramPath + ", should be valid pattern.");
      }
      // test for all invalid pattern in list
      for (String invalidPattern : invalidPatternList) {
         paramPattern = invalidPattern;
         paramPath = "/wap/" + invalidPattern;
         actual = objWapPathMatcher.match(paramPattern, paramPath);
         assertFalse(actual, "Path: " + paramPath + ", should not be valid pattern.");
      }
   }

   /**
    * Test match start.
    */
   @Test
   final void testMatchStart() {
      String paramPattern;
      String paramPath;
      boolean actual;
      // test for all valid pattern in list
      for (String validPattern : validPatternList) {
         paramPattern = validPattern;
         paramPath = "/wap/";
         actual = objWapPathMatcher.matchStart(paramPattern, paramPath);
         assertTrue(actual, "Path: " + paramPath + ", should be valid pattern.");
      }
      // test for all invalid pattern in list
      for (String invalidPattern : invalidPatternList) {
         paramPattern = invalidPattern;
         paramPath = "/wap/";
         actual = objWapPathMatcher.matchStart(paramPattern, paramPath);
         assertFalse(actual, "Path: " + paramPath + ", should not be valid pattern.");
      }
   }

   /**
    * Test extract path within pattern.
    */
   @Test
   final void testExtractPathWithinPattern() {
      String paramPattern;
      String paramPath;
      String actual;
      // test for all valid pattern in list
      for (String validPattern : validPatternList) {
         paramPattern = validPattern;
         paramPath = "/wap/";
         actual = null;
         actual = objWapPathMatcher.extractPathWithinPattern(paramPattern, paramPath);
         assertNotNull(actual, "Could not extract path within pattern: " + paramPattern);
      }
   }

   /**
    * Test extract URI template variables.
    */
   @Test
   final void testExtractUriTemplateVariables() {
      String paramPattern;
      String paramPath;
      Map<String, String> actual;
      // test for all valid pattern in list
      for (String validPattern : validPatternList) {
         paramPattern = validPattern;
         paramPath = "/wap/";
         actual = objWapPathMatcher.extractUriTemplateVariables(paramPattern, paramPath);
         assertNotNull(actual, "Could not extract URI template variables from pattern: " + paramPattern);
      }
   }

   /**
    * Test get pattern comparator.
    */
   @Test
   final void testGetPatternComparator() {
      Comparator<String> actual;
      actual = null;
      actual = objWapPathMatcher.getPatternComparator("/wap/");
      assertNotNull(actual, "Could not get pattern comparator.");
   }

   /**
    * Test combine.
    */
   @Test
   final void testCombine() {
      String actual;
      String expected;
      actual = null;
      actual = objWapPathMatcher.combine("/test1/", "/test2/");
      expected = "/test1/test2/";
      assertEquals(actual, expected, "Expected: " + expected + ", but was: " + actual);
   }

   /**
    * Test add ignored pattern.
    */
   @Test
   final void testAddIgnoredPattern() {
      String paramPath;
      paramPath = "/ignoreTest/";
      WapPathMatcher.addIgnoredPattern(paramPath);
      // assert no error
   }
}
