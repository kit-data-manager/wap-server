package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * This exception is used when a requested resource does not exist
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class NotExistentException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = GlobalErrorCodes.RESOURCE_NOT_EXISTENT;

   /**
    * Creates a new NotExistentException with a given message
    * 
    * @param message
    *                The message
    */
   public NotExistentException(String message) {
      super(message, HTTP_STATUS_CODE);
   }
}
