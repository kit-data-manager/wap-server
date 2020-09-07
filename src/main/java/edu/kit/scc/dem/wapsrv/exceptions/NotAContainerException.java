package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * This exception is used when the body of a PUT/POST request does not contain a valid Container
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class NotAContainerException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = GlobalErrorCodes.INVALID_BODY;

   /**
    * Creates a new NotAContainerException with a predefined message
    */
   public NotAContainerException() {
      super("The body does not contain a valid container", HTTP_STATUS_CODE);
   }

   /**
    * Constructs a new NotAContainerException using the given message
    * 
    * @param message
    *                The message
    */
   public NotAContainerException(String message) {
      super(message, HTTP_STATUS_CODE);
   }
}
