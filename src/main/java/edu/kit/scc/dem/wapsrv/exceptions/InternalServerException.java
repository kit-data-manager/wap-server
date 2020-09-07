package edu.kit.scc.dem.wapsrv.exceptions;

import org.springframework.http.HttpStatus;

/**
 * This exception is used for internal server errors that are already specific enough using the message alone
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class InternalServerException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

   /**
    * Creates a new InternalServerExcepion with the given message
    * 
    * @param message
    *                The exception message
    */
   public InternalServerException(String message) {
      super(message, HTTP_STATUS_CODE);
   }
}
