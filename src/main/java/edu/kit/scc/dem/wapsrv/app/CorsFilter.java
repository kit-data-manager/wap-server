package edu.kit.scc.dem.wapsrv.app;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.apache.jena.fuseki.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A cross origin filter that enhances the original jetty implementation
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class CorsFilter extends CrossOriginFilter {
   /**
    * The maximum age (validity) for preflight requests
    */
   private final int maxAge;
   /**
    * The headers that should be allowed in requests
    */
   private final String allowedHeaders;
   /**
    * The headers that should be accessible in responses
    */
   private final String exposedHeaders;
   /**
    * The logger to use
    */
   private final Logger logger = LoggerFactory.getLogger(this.getClass());
   /**
    * Needed for the original cors filter so he can call a no op chain
    */
   private final FilterChain noopChain = new FilterChain() {
      @Override
      public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
         // Do nothing, we will call the chain ourself later
      }
   };

   /**
    * Creates a new CorsFilter using the given parameters
    * 
    * @param maxAge
    *                       The maximum age (validity) for preflight requests
    * @param allowedHeaders
    *                       The headers that should be allowed in requests
    * @param exposedHeaders
    *                       The headers that should be accessible in responses (will be fixed *)
    */
   public CorsFilter(int maxAge, String allowedHeaders, String exposedHeaders) {
      this.maxAge = maxAge;
      this.allowedHeaders = allowedHeaders;
      this.exposedHeaders = "*";
      if (!"*".equals(exposedHeaders)) {
         LoggerFactory.getLogger(CorsFilter.class).warn("CORS exposed header not *, this is not yet implemented!");
      }
   }

   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
         throws IOException, ServletException {
      // The original cors filter can execute with the original response (it never commits it)
      // but use the noop chain (the real chain commits the request, and it would be called by super class)
      super.doFilter(request, response, noopChain);
      if (response.isCommitted()) {
         logger.warn("Http Response has been committed by the original "
               + "cors filter implementation. This should not happen!");
      }
      final HttpServletResponse httpResponse = (HttpServletResponse) response;
      final HttpServletRequest httpRequest = (HttpServletRequest) request;
      // After the original implementation did it's work, we eventually add some missing headers
      if (httpResponse.getHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER) == null) {
         // No cors request, let the original chain run
         chain.doFilter(request, httpResponse);
         return;
      } else { // We have a cors request, add the missing headers
         // Keeps tracks which headers have already been exposed
         final Set<String> alreadyExposedHeaders = new HashSet<String>();
         if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) { // == preflight request
            // All that is missing is related to preflight requests, therefore we now
            // add the eventually missing allowed methods headers
            if (httpResponse.getHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER) == null) {
               String allow = httpResponse.getHeader("Allow");
               if (allow != null) {
                  // we copy allow if existent
                  httpResponse.setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, allow);
               } else {
                  // or add a wildcard if not. This is a debatable feature we might have to remove
                  httpResponse.setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, "*");
               }
            }
            if (httpResponse.getHeader(ACCESS_CONTROL_MAX_AGE_HEADER) == null) {
               // Add the missing max age header
               httpResponse.setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, maxAge + "");
            }
            if (httpResponse.getHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER) == null) {
               // Add the missing allowed headers header
               httpResponse.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, allowedHeaders);
            }
         }
         // We always expect * as exposed headers, therefore we silently ignore anything else
         // * is allowed according to cors spec, but not implemented yet in the majority of all browsers.
         // We use a workaround here that can be commented out if all relevant browser support it
         if (!"*".equals(exposedHeaders)) {
            LoggerFactory.getLogger(CorsFilter.class).warn("CORS exposed header not *, this is not yet implemented!");
         }
         httpResponse.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER,
               createExposedHeaders(httpResponse, httpRequest, alreadyExposedHeaders));
         // The wrapper is needed to track the added headers and update the exposed headers list accordingly
         // This behavior is necessary since we do not get a chance to manipulate the response after the chain
         // has been executed. It always commits the message. The implementation of the response silently ignores
         // most of the methods changing the response after it has been committed. This led to the problem that
         // we set headers after calling the chain, but they were never transmitted to the client.
         final HttpServletResponse httpResponseWrapper
               = getHttpServletResponseWrapper(httpResponse, alreadyExposedHeaders);
         chain.doFilter(request, httpResponseWrapper);
         // Here the response has already been send. Therefore we cannot change any headers anymore and
         // This complex behavior with the noop chain and the wrapped response was necessary
      }
   }

   private HttpServletResponseWrapper getHttpServletResponseWrapper(final HttpServletResponse origHttpResponse,
         final Set<String> alreadyExposedHeaders) {
      return new HttpServletResponseWrapper(origHttpResponse) {
         @Override
         public void setHeader(String name, String value) {
            appendExposedHeader(name);
            super.setHeader(name, value);
            if ("Allow".equalsIgnoreCase(name)) {
               updatedAllowedMethods();
            }
         }

         @Override
         public void addHeader(String name, String value) {
            appendExposedHeader(name);
            super.addHeader(name, value);
            if ("Allow".equalsIgnoreCase(name)) {
               updatedAllowedMethods();
            }
         }

         @Override
         public void setDateHeader(String name, long date) {
            appendExposedHeader(name);
            super.setDateHeader(name, date);
         }

         @Override
         public void addDateHeader(String name, long date) {
            appendExposedHeader(name);
            super.addDateHeader(name, date);
         }

         @Override
         public void setIntHeader(String name, int value) {
            appendExposedHeader(name);
            super.setIntHeader(name, value);
         }

         @Override
         public void addIntHeader(String name, int value) {
            appendExposedHeader(name);
            super.addIntHeader(name, value);
         }

         /**
          * Update the cors allowed methods to match allow
          */
         private void updatedAllowedMethods() {
            origHttpResponse.setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, origHttpResponse.getHeader("Allow"));
         }

         /**
          * Append the given header to the exposed headers list of the wrapped response
          * 
          * @param name
          */
         private void appendExposedHeader(String name) {
            if (alreadyExposedHeaders.contains(name)) {
               return;
            }
            alreadyExposedHeaders.add(name);
            String header = origHttpResponse.getHeader(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER);
            if (header == null || header.length() == 0) {
               header = name;
            } else {
               header += "," + name;
            }
            origHttpResponse.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, header);
         }
      };
   }

   /**
    * Used as a workaround until all browsers support * for exposed headers
    * 
    * @param  httpResponse
    *                        The http response
    * @param  httpRequest
    *                        The http request
    * @param  exposedHeaders
    *                        The already exposed headers
    * @return                The exposed headers String
    */
   private String createExposedHeaders(HttpServletResponse httpResponse, HttpServletRequest httpRequest,
         Set<String> exposedHeaders) {
      // If the client explicetly requests some headers in a preflight request, we add them to the list
      if (httpRequest.getHeader(ACCESS_CONTROL_REQUEST_HEADERS_HEADER) != null) {
         for (String header : httpRequest.getHeader(ACCESS_CONTROL_REQUEST_HEADERS_HEADER).split(Pattern.quote(","))) {
            if (header.trim().length() > 0) {
               exposedHeaders.add(header.trim());
            }
         }
      }
      // Now we add all that is found in the current response. This should help, but may not be perfect
      // if a preflight request has been issued and no access to the real requests headers exists. The solution
      // then is to explicitly request the headers in question via Access-Control-Request-Headers
      for (String header : httpResponse.getHeaderNames()) {
         if (header.toLowerCase().startsWith("access-control")) {
            continue; // The CORS headers are not needed
         }
         // Some are always acceptable. But since the algorithm to determine them is rather strange,
         // we add all others. This should be no problem, as a "good" client never asks for headers whose
         // answer is already known and headers that are never asked for should be no problem too
         exposedHeaders.add(header);
      }
      return CorsConfiguration.buildCommaSeparatedString(exposedHeaders.toArray(new String[exposedHeaders.size()]));
   }
}
