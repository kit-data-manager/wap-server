package edu.kit.scc.dem.wapsrv.exceptions;

import org.springframework.http.HttpStatus;

/**
 * This exception is used whenever an error occurred in the repository
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RepositoryException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

   /**
    * Creates a new RepositoryException with a given message
    * 
    * @param message
    *                The message
    */
   public RepositoryException(String message) {
      super(message, HTTP_STATUS_CODE);
   }
}
