package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * This exception is used when ETags do not match in the request and the database
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class EtagDoesntMatchException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = GlobalErrorCodes.ETAGS_DONT_MATCH;

   /**
    * Creates a new EtagDoesntMatchException with a given message
    * 
    * @param message
    *                The message
    */
   public EtagDoesntMatchException(String message) {
      super(message, HTTP_STATUS_CODE);
   }
}
