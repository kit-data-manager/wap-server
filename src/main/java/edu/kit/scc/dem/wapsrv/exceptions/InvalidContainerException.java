package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * This exception is used if the client tries to create a container that is not a LDP basic container, has an invalid
 * name or similar container specific reasons
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class InvalidContainerException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = GlobalErrorCodes.INVALID_REQUEST;

   /**
    * Creates a new InvalidContainerTypeException with a given message
    * 
    * @param message
    *                The message
    */
   public InvalidContainerException(String message) {
      super(message, HTTP_STATUS_CODE);
   }
}
