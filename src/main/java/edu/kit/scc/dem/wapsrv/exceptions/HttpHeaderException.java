package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * This exception is used for all problems concerning HTTP header fields.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class HttpHeaderException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = GlobalErrorCodes.INVALID_REQUEST;

   /**
    * Creates a new HttpHeaderExcepion with the given message
    * 
    * @param message
    *                The exception message
    */
   public HttpHeaderException(String message) {
      super(message, HTTP_STATUS_CODE);
   }
}
