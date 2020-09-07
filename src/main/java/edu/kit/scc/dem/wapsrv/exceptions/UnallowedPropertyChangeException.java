package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * This exception is used when properties that are forbidden to change are tried to be changed
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class UnallowedPropertyChangeException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = GlobalErrorCodes.MODIFICATION_NOT_ALLOWED;

   /**
    * Creates a new UnallowedPropertyChangeException with a given message
    * 
    * @param message
    *                The message
    */
   public UnallowedPropertyChangeException(String message) {
      super(message, HTTP_STATUS_CODE);
   }
}
