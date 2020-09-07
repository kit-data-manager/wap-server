package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * This exception is used if during post/post requests an invalid format is used. That means the format string provided
 * in accept or content-type headers is not known/implemented in the application.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class FormatNotAvailableException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = GlobalErrorCodes.FORMAT_NOT_AVAILABLE;

   /**
    * Constructs a new FormatNotAvailableException instance
    * 
    * @param message
    *                The message used for the exception
    */
   public FormatNotAvailableException(String message) {
      super(message, HTTP_STATUS_CODE);
   }
}
