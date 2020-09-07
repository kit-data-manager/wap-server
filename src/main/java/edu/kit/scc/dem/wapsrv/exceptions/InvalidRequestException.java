package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * This exception is used whenever a per se valid HTTP Method is used on a resource in an invalid way not already
 * implemented by more specific exceptions
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class InvalidRequestException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = GlobalErrorCodes.INVALID_REQUEST;

   /**
    * Creates a new InvalidRequestException with a given message
    * 
    * @param message
    *                The message
    */
   public InvalidRequestException(String message) {
      super(message, HTTP_STATUS_CODE);
   }
}
