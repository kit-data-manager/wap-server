package edu.kit.scc.dem.wapsrv.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpMethod;

/**
 * Contains constants used by the container controllers
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class ContainerConstants {
   /**
    * The HTTP status code for successful GET requests to containers
    */
   public static final int GET_CONTAINER_SUCCESS_CODE = 200;
   /**
    * The HTTP status code for successful POST requests of containers
    */
   public static final int POST_CONTAINER_SUCCESS_CODE = 201;
   /**
    * The HTTP status code for successful DELETE requests of containers
    */
   public static final int DELETE_CONTAINER_SUCCESS_CODE = 204;
   /**
    * Set of allowed HTTP methods on containers
    */
   public static final Set<HttpMethod> ALLOWED_METHODS = new HashSet<HttpMethod>(Arrays.asList(
         new HttpMethod[] {HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.POST, HttpMethod.DELETE}));
   /**
    * The allowed methods as a string
    */
   public static final String ALLOWED_METHODS_STRING = "GET,HEAD,OPTIONS,POST,DELETE";
   /**
    * The allowed methods as a string for the root container
    */
   public static final String ROOT_ALLOWED_METHODS_STRING = "GET,HEAD,OPTIONS,POST";
   /**
    * Set of allowed HTTP methods on the root container
    */
   public static final Set<HttpMethod> ROOT_ALLOWED_METHODS = new HashSet<HttpMethod>(
         Arrays.asList(new HttpMethod[] {HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.POST}));
   /**
    * Values to use in HTTP vary header to inform caches that the answer changes with the value of these headers
    */
   public static final List<String> VARY_LIST = Arrays.asList(new String[] {"Accept"});
   /**
    * The link type string
    */
   public static final String LINK_TYPE = "<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"";
   /**
    * The protocol string
    */
   public static final String LINK_ANNOTATION_PROTOCOL
         = "<http://www.w3.org/TR/annotation-protocol/>; " + "rel=\"http://www.w3.org/ns/ldp#constrainedBy\"";

   /**
    * The constructor
    */
   private ContainerConstants() {
      throw new RuntimeException("ContainerCodes should not be instantiated");
   }
}
