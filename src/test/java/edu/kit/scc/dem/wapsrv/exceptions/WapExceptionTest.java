package edu.kit.scc.dem.wapsrv.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests the class WapException
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class WapExceptionTest {
   /**
    * All other exception tests classes will use this list.
    */
   protected static final Set<String> PARAMSTRINGLIST = createStringList();
   private static final Map<String, Integer> PARAMMAP = createParamMap();

   private static Set<String> createStringList() {
      Set<String> paramStringList = new HashSet<String>();
      paramStringList.add("");
      paramStringList.add("testing");
      return paramStringList;
   }

   private static Map<String, Integer> createParamMap() {
      Map<String, Integer> paramMap = new HashMap<String, Integer>();
      paramMap.put("test string", 200);
      paramMap.put("", 204);
      return paramMap;
   }

   /**
    * Test constructor I
    */
   @Test
   final void testWapExceptionStringInt() {
      WapException actual;
      // test for all entries in paramMap
      for (Map.Entry<String, Integer> entry : PARAMMAP.entrySet()) {
         actual = null;
         actual = new WapExceptionInstance(entry.getKey(), entry.getValue());
         assertNotNull(actual,
               "Construction did fail for String: " + entry.getKey() + ", for Integer: " + entry.getValue());
      }
   }

   /**
    * Test constructor II
    */
   @Test
   final void testWapExceptionStringIntThrowable() {
      WapException actual;
      // test for all entries in paramMap
      for (Map.Entry<String, Integer> entry : PARAMMAP.entrySet()) {
         actual = null;
         actual = new WapExceptionInstance(entry.getKey(), entry.getValue(), new Throwable());
         assertNotNull(actual,
               "Construction did fail for String: " + entry.getKey() + ", for Integer: " + entry.getValue());
      }
   }

   /**
    * Test constructor III
    */
   @Test
   final void testWapExceptionStringIntThrowableBooleanBoolean() {
      WapException actual;
      // test for all entries in paramMap
      for (Map.Entry<String, Integer> entry : PARAMMAP.entrySet()) {
         // test 0 0
         actual = null;
         actual = new WapExceptionInstance(entry.getKey(), entry.getValue(), new Throwable(), false, false);
         assertNotNull(actual, "Construction did fail for String: " + entry.getKey() + ", for Integer: "
               + entry.getValue() + ", for Boolean: 0 0");
         // test 0 1
         actual = null;
         actual = new WapExceptionInstance(entry.getKey(), entry.getValue(), new Throwable(), false, true);
         assertNotNull(actual, "Construction did fail for String: " + entry.getKey() + ", for Integer: "
               + entry.getValue() + ", for Boolean: 0 1");
         // test 1 0
         actual = null;
         actual = new WapExceptionInstance(entry.getKey(), entry.getValue(), new Throwable(), true, false);
         assertNotNull(actual, "Construction did fail for String: " + entry.getKey() + ", for Integer: "
               + entry.getValue() + ", for Boolean: 1 0");
         // test 1 1
         actual = null;
         actual = new WapExceptionInstance(entry.getKey(), entry.getValue(), new Throwable(), true, true);
         assertNotNull(actual, "Construction did fail for String: " + entry.getKey() + ", for Integer: "
               + entry.getValue() + ", for Boolean: 1 1");
      }
   }

   /**
    * Test getting HTTP status code.
    */
   @Test
   final void testGetHttpStatusCode() {
      WapException objWapException;
      int actual;
      // test for all entries in paramMap
      for (Map.Entry<String, Integer> entry : PARAMMAP.entrySet()) {
         objWapException = null;
         objWapException = new WapExceptionInstance(entry.getKey(), entry.getValue());
         actual = objWapException.getHttpStatusCode();
         assertTrue(actual == entry.getValue(), "Status Code expected: " + entry.getValue() + " but was: " + actual);
      }
   }

   private static class WapExceptionInstance extends WapException {
      private static final long serialVersionUID = 1L;

      public WapExceptionInstance(String message, int httpStatusCode) {
         super(message, httpStatusCode);
      }

      public WapExceptionInstance(String message, int httpStatusCode, Throwable cause) {
         super(message, httpStatusCode, cause);
      }

      public WapExceptionInstance(String message, int httpStatusCode, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
         super(message, httpStatusCode, cause, enableSuppression, writableStackTrace);
      }
   }
}
