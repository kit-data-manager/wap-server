package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * This exception is used when the IRI of a page has an invalid form
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class IllegalPageIriException extends WapException {
   /**
    * The common error message
    */
   public static final String ERROR_MESSAGE
         = "Page iris must be of this form : CONTAINER_IRI?iris=[0|1]&page=[int>=0], the page parameter is optional";
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = GlobalErrorCodes.INVALID_REQUEST;

   /**
    * Creates a new IllegalPageIriException with a predefined message
    */
   public IllegalPageIriException() {
      super(ERROR_MESSAGE, HTTP_STATUS_CODE);
   }

   /**
    * Creates a new IllegalPageIriException with a predefined message
    * 
    * @param cause
    *              The cause
    */
   public IllegalPageIriException(Throwable cause) {
      super(ERROR_MESSAGE, HTTP_STATUS_CODE, cause);
   }
}
