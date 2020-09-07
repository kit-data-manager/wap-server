package edu.kit.scc.dem.wapsrv.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpMethod;

/**
 * Contains constants used by the page controller
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class PageConstants {
   /**
    * The HTTP status code for successful GET requests to pages
    */
   public static final int GET_PAGE_SUCCESS_CODE = 200;
   /**
    * Set of allowed HTTP methods on pages
    */
   public static final Set<HttpMethod> ALLOWED_METHODS = new HashSet<HttpMethod>(
         Arrays.asList(new HttpMethod[] {HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS}));
   /**
    * The allowed methods as a String
    */
   public static final String ALLOWED_METHODS_STRING = "GET,HEAD,OPTIONS";
   // Error Codes for pages
   /**
    * The HTTP status code for bad requests of pages
    */
   public static final int BAD_REQUEST_CODE = 400;
   /**
    * Values to use in HTTP vary header to inform caches that the answer changes with the value of these headers
    */
   public static final List<String> VARY_LIST = Arrays.asList(new String[] {"Accept"});

   /**
    * The constructor
    */
   private PageConstants() {
      throw new RuntimeException("PageCodes should not be instantiated");
   }
}
