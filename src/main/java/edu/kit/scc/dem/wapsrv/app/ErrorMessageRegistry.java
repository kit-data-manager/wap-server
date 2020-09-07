package edu.kit.scc.dem.wapsrv.app;

import org.springframework.http.HttpHeaders;

/**
 * This class is the central registry for all error messages that should have a static message. This is needed
 * especially for REST tests to verify that not only an exception of the expected type is thrown, but also for the
 * expected reason.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class ErrorMessageRegistry {
   /**
    * Error message for getting head options of a container with parameters except an IRI.
    */
   public static final String CONTAINER_GET_HEAD_OPTIONS_NO_PARAMETERS_ALLOWED_BUT_IRIS
         = "No parameters allowed in container GET|HEAD|OPTIONS requests beside iris=[0|1]";
   /**
    * Error message for posting a container deletion with parameters.
    */
   public static final String CONTAINER_POST_DELETE_NO_PARAMETERS_ALLOWED
         = "No parameters allowed in container POST|DELETE requests";
   /**
    * Error message for invalid preference header for a container.
    */
   public static final String CONTAINER_INVALID_PREFERENCES = "Invalid Prefer header, see WAP - ContainerPreferences";
   /**
    * Error message for invalid combination of preferences for a container.
    */
   public static final String CONTAINER_UNALLOWED_PREFERENCE_COMBINATION
         = "Embedded IRIs and Embedded Descriptions cannot be used together, " + " see WAP - ContainerPreferences";
   /**
    * Error message for a empty container slug.
    */
   public static final String CONTAINER_SLUG_IS_SET_MANDATORY
         = "Container name cannot be empty, slug is set to be mandatory";
   /**
    * Error message for non basic ldp containers.
    */
   public static final String CONTAINER_ONLY_BASIC_CONTAINER_ALLOWED = "Only ldp basic containers allowed for POST";
   /**
    * Error message for missing link header while posting a container.
    */
   public static final String CONTAINER_LINK_NEEDED_IN_POST = "Link header ist needed for POST of containers";
   /**
    * Error message for missing content type at POST request.
    */
   public static final String ALL_CONTENT_TYPE_NEEDED_IN_POST
         = HttpHeaders.CONTENT_TYPE + " header necessary in POST requests";
   /**
    * Error message for missing content type at PUT request.
    */
   public static final String ALL_CONTENT_TYPE_NEEDED_IN_PUT
         = HttpHeaders.CONTENT_TYPE + " header necessary in PUT requests";
   /**
    * Error message for invalid IRI being not a URI.
    */
   public static final String INTERNAL_IRI_NOT_A_URI = "Internal Server error, IRI not an URI";
   /**
    * Error message for missing IF MATCH header in DELETE request.
    */
   public static final String ALL_ETAG_NEEDED_FOR_DELETE
         = HttpHeaders.IF_MATCH + " header necessary in DELETE requests";
   /**
    * Error message for missing IF MATCH header in PUT request.
    */
   public static final String ALL_ETAG_NEEDED_FOR_PUT = HttpHeaders.IF_MATCH + " header necessary in PUT requests";
   /**
    * Error message for missing internal content type.
    */
   public static final String INTERNAL_CONTENT_TYPE_NULL_WHERE_IT_SHOULD_NOT_BE
         = HttpHeaders.CONTENT_TYPE + " is null in WapController.getContentTypeParser(String contentType)";
   /**
    * Error message for unknown input format.
    */
   public static final String ALL_UNKNOWN_INPUT_FORMAT = "Unkown input format";
   /**
    * Error message for invalid input format header.
    */
   public static final String ALL_INVALID_INPUT_FORMAT_HEADER = "Error parsing input format information";
   /**
    * Error message for invalid format of PUT and POST requests.
    */
   public static final String ALL_UNALLOWED_INPUT_FORMAT = "Format not allowed in PUT/POST requests";
   /**
    * Error message for invalid ETAG because of missing quotation mark.
    */
   public static final String ALL_INVALID_ETAG_FORMAT = "Invalid Etag, it has to start and end with \" (e.G. \"123s\")";
   /**
    * Error message for invalid PAGE request because of wrong parameters.
    */
   public static final String PAGE_INVALID_GIVEN_PARAMETERS = "Only iris and page parameters allowed in page requests";
   /**
    * Error message annotation request with not allowed parameters.
    */
   public static final String ANNOTATION_NO_PARAMETERS_ALLOWED = "No parameters allowed in annotation requests";
   /**
    * Error message for not supported HTTP method request.
    */
   public static final String ALL_UNALLOWED_METHOD = "This server does not support the requested HTTP method";
   /**
    * Error message for error while processing WAP request.
    */
   public static final String INTERAL_WAP_ERROR_PROCESSING_NOT_COMPLETE
         = "WAW request error processing " + "not complete in catch-all controller";
   /**
    * Error message for not not possible request at catch-all block while GET request from the WebApp.
    */
   public static final String WEBAPP_GET_IN_CATCHALL
         = "GET Requests to the webapp should never " + "land in catch-all controller";
   /**
    * Error message for not allowed request to the WebApp endpoint.
    */
   public static final String WEBAPP_ONLY_GET_ALLOWED = "Only GET requests are allowed to the webapp endpoint";
   /**
    * Error message for not allowed request to the JavaDoc endpoint.
    */
   public static final String JAVADOC_GET_IN_CATCHALL
         = "GET Requests to the javadoc should never " + "land in catch-all controller";
   /**
    * Error message for not not possible request at catch-all block while GET request from the JavaDoc endpoint.
    */
   public static final String JAVADOC_ONLY_GET_ALLOWED = "Only GET requests are allowed to the javadoc endpoint";
   /**
    * Error message for not allowed POST to an annotation.
    */
   public static final String ANNOTATION_POST_TO_NOT_ALLOWED = "POST to an annotion IRI is not allowed";
   /**
    * Error message for not allowed PUT to an annotation.
    */
   public static final String ANNOTATION_PUT_TO_NOT_ALLOWED = "PUT to an annotion IRI is not allowed";
   /**
    * Error message for not allowed parameters in a PUT request.
    */
   public static final String ALL_NO_PARAMETERS_IN_PUT = "No parameters allowed in PUT requests";
   /**
    * Error message for not allowed parameters in a POST request.
    */
   public static final String ALL_NO_PARAMETERS_IN_POST = "No parameters allowed in POST requests";
   /**
    * Error message for the not implemented function of PUTing containers.
    */
   public static final String CONTAINER_PUT_NOT_IMPLEMENTED = "PUT of containers not implemented";
   /**
    * Error message for invalid container POST request.
    */
   public static final String CONTAINER_POST_INVALID = "Unknown intention: POST container ==> use Link header";
   /**
    * Error message for invalid annotation POST request.
    */
   public static final String ANNOTATION_POST_INVALID = CONTAINER_POST_INVALID;
   /**
    * Error message for not allowed request to post annotation to root container.
    */
   public static final String ANNOTATION_NO_POST_TO_ROOT_CONTAINER = "Annotations cannot be posted to root container";
   /**
    * Error message for not existing page.
    */
   public static final String PAGE_NOT_EXISTENT = "The page with the given number does not exist";
   /**
    * Error message for missing IRI in PAGE request.
    */
   public static final String PAGE_WITH_PAGE_BUT_IRIS_MISSING = "Page request with page parameter but missing iris";
   /**
    * Error message for not allowed slug header.
    */
   public static final String CONTAINER_NO_EMPTY_SLUG_ALLOWED
         = "Slug header was empty. " + "To use auto name creation, do not specifiy the header at all";
   /**
    * Error message for request mapping to a not expected location.
    */
   public static final String INTERAL_FOLDER_SERVER_UNEXPECTED_MAPPING = "Requests mapped to a not expected location";
   /**
    * Error message for not existing file.
    */
   public static final String FOLDER_SERVER_NOT_EXISTENT_FILE = "The requested resource does not exist";
   /**
    * Error message for not readable file.
    */
   public static final String FOLDER_SERVER_FILE_NOT_READBLE = "The requested resource cannot be read";
   /**
    * Error message for internal error because of impossible redirection.
    */
   public static final String INTERAL_FOLDER_SERVER_REDIRECT_ERROR = "Internal Server error, cannot redirect to";
   /**
    * Error message for invalid requested resource because not being a file.
    */
   public static final String FOLDER_SERVER_NOT_A_FILE = "The requested resource is not a file";
   /**
    * Error message when put to an annotation langs in the catchall controller
    */
   public static final String PUT_ANNOTATION_SHOULD_NOT_LAND_HERE
         = "Put annotation should not land in catchall controller";
}
