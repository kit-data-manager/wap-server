package edu.kit.scc.dem.wapsrv.controller;

import static org.junit.jupiter.api.Assertions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

/**
 * Tests the class BasicController
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class BasicControllerTest {
   /**
    * Test extract IRI.
    */
   @Test
   final void testExtractIri() {
      final String iri = "http://www.example.org/wap/container1";
      HttpServletRequest request = new HttpServletRequestAdapter() {
         @Override
         public StringBuffer getRequestURL() {
            return new StringBuffer(iri);
         }
      };
      String extractedIri = BasicController.extractIri(request);
      assertEquals(iri, extractedIri, "Iris do not match");
   }

   /**
    * Test get query string.
    */
   @Test
   final void testGetQueryString() {
      final String queryString = "iris=0&page=1";
      HttpServletRequest request = new HttpServletRequestAdapter() {
         @Override
         public String getQueryString() {
            return queryString;
         };
      };
      String extractedQueryString = BasicController.getQueryString(request);
      assertEquals(queryString, extractedQueryString, "Query String does not match");
   }

   /**
    * Test extract URL withoutQuery
    */
   @Test
   final void testExtractUrlWithoutQuery() {
      // Test with no query string is not needed, is equals to testExtractIri
      // Test with query String
      final String iri = "http://www.example.org/wap/container1";
      HttpServletRequest request = new HttpServletRequestAdapter() {
         @Override
         public StringBuffer getRequestURL() {
            return new StringBuffer(iri);
         }
      };
      String extractedUrl = BasicController.extractUrl(request);
      assertEquals(iri, extractedUrl, "URL does not match");
   }

   /**
    * Test extract URL with query
    */
   @Test
   final void testExtractUrlWithQuery() {
      // Test with no query string is not needed, is equals to testExtractIri
      // Test with query String
      final String iri = "http://www.example.org/wap/container1";
      final String queryString = "iris=0&page=1";
      HttpServletRequest request = new HttpServletRequestAdapter() {
         @Override
         public StringBuffer getRequestURL() {
            return new StringBuffer(iri);
         }

         @Override
         public String getQueryString() {
            return queryString;
         };
      };
      String extractedUrl = BasicController.extractUrl(request);
      assertEquals(iri + "?" + queryString, extractedUrl, "URL does not match");
   }

   /**
    * Test is options request.
    */
   @Test
   final void testIsOptionsRequest() {
      String paramHttpMethod;
      boolean actual;
      // test for all HttpMethod
      for (HttpMethod item : HttpMethod.values()) {
         paramHttpMethod = item.toString();
         actual = BasicController.isOptionsRequest(paramHttpMethod);
         if (item == HttpMethod.OPTIONS) {
            assertTrue(actual, "Is options request should be true for HTTP OPTIONS method.");
         } else {
            assertFalse(actual, "Is options request should be false for HTTP method: " + paramHttpMethod);
         }
      }
   }
}
