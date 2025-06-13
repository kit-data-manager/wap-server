package edu.kit.scc.dem.wapsrv.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;

/**
 * Base class for all Controllers where common behavior is implemented.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public abstract class BasicController {
   /**
    * Returns the IRI of the requested URL.
    * <p>
    * The IRI does not include the query string. Further distinction between pages and containers needs to be separately
    * performed.
    * 
    * @param  request
    *                 The request
    * @return         The IRI as a string
    */
   public static String extractIri(HttpServletRequest request) {
      String url = request.getRequestURL().toString();
      return url;
   }

   /**
    * Returns the query string used in a request. This does not contain the ?
    * 
    * @param  request
    *                 The request
    * @return         The query string, may be null
    */
   public static String getQueryString(HttpServletRequest request) {
      // This is very simple after changing the request class.
      return request.getQueryString();
   }

   /**
    * Extracts the URL requested by the client. This includes the query string if it exists.
    * 
    * @param  request
    *                 The request
    * @return         The complete URL requested
    */
   public static String extractUrl(HttpServletRequest request) {
      String queryString = request.getQueryString();
      String iri = request.getRequestURL() + (queryString != null ? "?" + queryString : "");
      return iri;
   }

   /**
    * Determine whether a given HTTP method as a String corresponds to the HTTP OPTIONS method
    * 
    * @param  httpMethod
    *                    The string representation of the HTTP method
    * @return            true if OPTIONS, false otherwise
    */
   public static boolean isOptionsRequest(String httpMethod) {
      return HttpMethod.valueOf(httpMethod) == HttpMethod.OPTIONS;
   }
}
