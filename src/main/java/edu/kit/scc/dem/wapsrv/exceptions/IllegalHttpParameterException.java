package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * This exception is used whenever HTTP requests contained illegal parameters in their query string.
 * <p>
 * This may be wrong ones, unknown ones, missing ones and parameters at all where none are expected.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class IllegalHttpParameterException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = GlobalErrorCodes.INVALID_REQUEST;

   /**
    * Constructs a new IllegalHttpParameterException instance
    * 
    * @param message
    *                The message used for the exception
    */
   public IllegalHttpParameterException(String message) {
      super(message, HTTP_STATUS_CODE);
   }

   /**
    * Constructs a new IllegalHttpParameterException instance
    * 
    * @param message
    *                The message used for the exception
    * @param cause
    *                The cause. (A NULL value is permitted, and indicates that the cause is nonexistent or unknown.)
    */
   public IllegalHttpParameterException(String message, Throwable cause) {
      super(message, HTTP_STATUS_CODE, cause);
   }

   /**
    * Constructs a new IllegalHttpParameterException instance
    * 
    * @param message
    *                           The message used for the exception
    * @param cause
    *                           The cause. (A NULL value is permitted, and indicates that the cause is nonexistent or
    *                           unknown.)
    * @param enableSuppression
    *                           whether or not suppression is enabled or disabled
    * @param writableStackTrace
    *                           whether or not the stack trace should be writable
    */
   public IllegalHttpParameterException(String message, Throwable cause, boolean enableSuppression,
         boolean writableStackTrace) {
      super(message, HTTP_STATUS_CODE, cause, enableSuppression, writableStackTrace);
   }
}
