package edu.kit.scc.dem.wapsrv.exceptions;

/**
 * This exception is used whenever a resource that has been deleted should be recreated
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */

public class ResourceDeletedException extends WapException {

   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_STATUS_CODE = GlobalErrorCodes.RESOURCE_HAS_BEEN_DELETED;

   /**
    * Creates a new ResourceDeletedException with a given message
    * 
    * @param message
    *                The message
    */
   public ResourceDeletedException(String message) {
      super(message, HTTP_STATUS_CODE);
   }
}
