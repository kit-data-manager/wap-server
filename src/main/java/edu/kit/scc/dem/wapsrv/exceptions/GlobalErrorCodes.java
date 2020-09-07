package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * Global error code used in the Web Annotation Protocol Server.
 * <p>
 * Here are the ones specified that relate to the protocol specification (4xx), the ones resulting from common internal
 * server problems (5xx) use those defined in {@link org.springframework.http.HttpStatus}
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class GlobalErrorCodes {
   /**
    * The Annotation Client sent a request which the Annotation Server cannot process due to the request not following
    * the appropriate specifications.
    */
   public static final int INVALID_REQUEST = 400;
   /**
    * The Annotation Client is not authorized to perform the requested operation, such as creating or deleting an
    * Annotation, as it did not supply authentication credentials.
    */
   public static final int NOT_AUTHORIZED = 401;
   /**
    * The Annotation Client is not authorized to perform the requested operation, as the authentication credentials
    * supplied did not meet the requirements of a particular access control policy for the Annotation or Annotation
    * Container.
    */
   public static final int INVALID_CREDENTIALS = 403;
   /**
    * The Annotation or Annotation Container requested does not exist.
    */
   public static final int RESOURCE_NOT_EXISTENT = 404;
   /**
    * The requested HTTP method is not allowed for the resource, such as trying to POST to an Annotation Container page,
    * or trying to PATCH an Annotation when that functionality is not supported.
    */
   public static final int METHOD_NOT_ALLOWED = 405;
   /**
    * The requested format for the Annotation or Annotation Container's representation is not available, for example if
    * a client requested RDF/XML and the server does not support that (optional) transformation.
    */
   public static final int FORMAT_NOT_AVAILABLE = 406;
   /**
    * The Annotation Client tried to set or change a value that the server does not allow Clients to modify, such as the
    * containment list of an Annotation Container or server set modification timestamps.
    */
   public static final int MODIFICATION_NOT_ALLOWED = 409;
   /**
    * The Annotation is known to have existed in the past and was deleted.
    */
   public static final int RESOURCE_HAS_BEEN_DELETED = 410;
   /**
    * The Annotation Client supplied an If-Match header that did not match the ETag of the Annotation being modified.
    */
   public static final int ETAGS_DONT_MATCH = 412;
   /**
    * The Annotation Client sent an entity-body that is not able to be processed by the Server, such as non-Annotation
    * or in a context that is unrecognized.
    */
   public static final int INVALID_BODY = 415;

   /**
    * No instances of this class are allowed
    */
   private GlobalErrorCodes() {
      throw new RuntimeException("not used");
   }
}
