package edu.kit.scc.dem.wapsrv.controller;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * Helper class for Controller Tests
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class ControllerTestHelper {
    private static final Logger logger = LoggerFactory.getLogger(ControllerTestHelper.class);

   private ControllerTestHelper() {
      throw new RuntimeException("No instance allowed");
   }

   /**
    * Checks whether the given methods are equal to the responses allow header
    * 
    * @param response
    *                 The response to test
    * @param methods
    *                 The methods to find in the allow header
    */
   public static void checkAllowHeader(ResponseEntity<?> response, HttpMethod... methods) {
      assertNotNull(response);
      assertNotNull(methods);
      Set<HttpMethod> methodsRef = response.getHeaders().getAllow();
      assertNotNull(methodsRef);
      assertEquals(methodsRef.size(), methods.length);
      for (HttpMethod method : methods) {
         if (!methodsRef.contains(method)) {
            fail("Http method " + method + " not in response allow header");
         }
      }
      response.getHeaders();
   }

   /**
    * Checks whether the given header fields are equal to the responses vary header
    * 
    * @param response
    *                     The response to test
    * @param headerFields
    *                     The header fields to find in the vary header
    */
   public static void checkVaryHeader(ResponseEntity<?> response, String... headerFields) {
      assertNotNull(response);
      assertNotNull(headerFields);
      List<String> varyRef = response.getHeaders().getVary();
      assertNotNull(varyRef);
      assertEquals(varyRef.size(), headerFields.length);
      for (String vary : headerFields) {
         if (!varyRef.contains(vary)) {
            fail("Vary " + vary + " not in response vary header");
         }
      }
      response.getHeaders();
   }

   /**
    * Checks if the link headers are as expected
    * 
    * @param response
    *                 The response to check
    * @param linksRef
    *                 The link headers to find
    */
   public static void checkLinkHeader(ResponseEntity<?> response, String[] linksRef) {
      assertNotNull(response);
      assertNotNull(linksRef);
      List<String> links = response.getHeaders().get(HttpHeaders.LINK);
      assertNotNull(links);
      assertEquals(links.size(), linksRef.length);
      for (String link : linksRef) {
         if (!links.contains(link)) {
            fail("Link not found : " + link);
         }
      }
   }

   /**
    * Creates a parameter map from a given parameter string
    * 
    * @param  rawString
    *                   The string as used in urls, null to generate empty map
    * @return           The map generated from the string
    */
   public static Map<String, String[]> createParamsMap(String rawString) {
      Map<String, String[]> map = new Hashtable<String, String[]>();
      if (rawString == null)
         return map;
      String[] parts = rawString.split(Pattern.quote("&"));
      for (String part : parts) {
         String[] subParts = part.split(Pattern.quote("="));
         String key = subParts[0];
         String value = subParts[1];
         if (map.containsKey(key)) {
            String[] values = map.get(key);
            String[] valuesNew = new String[values.length + 1];
            for (int n = 0; n < values.length; n++) {
               valuesNew[n] = values[n];
            }
            valuesNew[values.length] = value;
         } else {
            map.put(key, new String[] {value});
         }
      }
      return map;
   }

   /**
    * Checks an exception of a expected type is thrown and that it has the expected message
    * 
    * @param expectedType
    *                     The expected exception
    * @param message
    *                     The expected message
    * @param executable
    *                     The code that should throw the exception
    */
   public static void checkException(Class<? extends Exception> expectedType, String message, Executable executable) {
      Exception actualException = Assertions.assertThrows(expectedType, executable);
      // If this code is reached, the exception has been thrown, now check message
      assertEquals(message, actualException.getMessage(), "Exception has unexpected message");
   }
}
