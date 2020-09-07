package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * The base exception class use for all Web Annotation Protocol Server specific exceptions.
 * <p>
 * Subclasses must provide a HTTP Status Code value to use for HTTP Responses caused by this exception.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public abstract class WapException extends RuntimeException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status CODE used by this exception
    */
   private final int httpStatusCode;

   /**
    * @param message
    *                       The message used for the exception
    * @param httpStatusCode
    *                       The HTTP Status Code to use
    */
   public WapException(String message, int httpStatusCode) {
      super(message);
      this.httpStatusCode = httpStatusCode;
   }

   /**
    * Constructs a new WapException with the given parameters
    * 
    * @param message
    *                       The message used for the exception
    * @param httpStatusCode
    *                       The HTTP Status Code to use
    * @param cause
    *                       the cause. (A NULL value is permitted, and indicates that the cause is nonexistent or
    *                       unknown.)
    */
   public WapException(String message, int httpStatusCode, Throwable cause) {
      super(message, cause);
      this.httpStatusCode = httpStatusCode;
   }

   /**
    * Constructs a new WapException with the given parameters
    * 
    * @param message
    *                           The message used for the exception
    * @param httpStatusCode
    *                           The HTTP Status Code to use
    * @param cause
    *                           the cause. (A NULL value is permitted, and indicates that the cause is nonexistent or
    *                           unknown.)
    * @param enableSuppression
    *                           whether or not suppression is enabled or disabled
    * @param writableStackTrace
    *                           whether or not the stack trace should be writable
    */
   public WapException(String message, int httpStatusCode, Throwable cause, boolean enableSuppression,
         boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
      this.httpStatusCode = httpStatusCode;
   }

   /**
    * Returns the HTTP status code to use for responses caused by this exception
    * 
    * @return The HTTP response code
    */
   public int getHttpStatusCode() {
      return httpStatusCode;
   }
}
