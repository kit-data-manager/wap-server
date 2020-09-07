package edu.kit.scc.dem.wapsrv.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpMethod;

/**
 * Contains constants used by the annotation controller.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class AnnotationConstants {
   /**
    * The HTTP status code for successful GET requests to annotations
    */
   public static final int GET_ANNOTATION_SUCCESS_CODE = 200;
   /**
    * The HTTP status code for successful POST requests of annotations
    */
   public static final int POST_ANNOTATION_SUCCESS_CODE = 201;
   /**
    * The HTTP status code for successful PUT requests of annotations
    */
   public static final int PUT_ANNOTATION_SUCCESS_CODE = 200;
   /**
    * The HTTP status code for successful DELETE requests of annotations
    */
   public static final int DELETE_ANNOTATION_SUCCESS_CODE = 204;
   /**
    * Set of allowed HTTP methods on annotations
    */
   public static final Set<HttpMethod> ALLOWED_METHODS = new HashSet<HttpMethod>(Arrays.asList(
         new HttpMethod[] {HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.PUT, HttpMethod.DELETE}));
   /**
    * The allowed methods as a string
    */
   public static final String ALLOWED_METHODS_STRING = "GET,HEAD,OPTIONS,PUT,DELETE";
   /**
    * Values to use in HTTP Vary Header to inform caches that the answer changes with the value of these headers
    */
   public static final List<String> VARY_LIST = Arrays.asList(new String[] {"Accept"});
   /**
    * The HTTP link headers to use for responses
    */
   public static final String[] LINK_HEADER = new String[] {"<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"",
         "<http://www.w3.org/ns/oa#Annotation>; rel=\"type\""};

   /**
    * The constructor
    */
   private AnnotationConstants() {
      throw new RuntimeException("AnnotationConstants should not be instantiated");
   }
}
