package edu.kit.scc.dem.wapsrv.exceptions;

import org.springframework.http.HttpStatus;

/**
 * This exception is used for various types of exceptions related to format processing.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class FormatException extends WapException {
   /**
    * The serial version UID for java serialization
    */
   private static final long serialVersionUID = 1L;
   /**
    * The HTTP Status Code used for this type of Exception
    */
   private static final int HTTP_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

   /**
    * Constructs a new FormatException instance
    * 
    * @param message
    *                The message used for the exception
    */
   public FormatException(String message) {
      super(message, HTTP_CODE);
   }

   /**
    * Constructs a new FormatException instance
    * 
    * @param message
    *                The message used for the exception
    * @param cause
    *                the cause. (A NULL value is permitted, and indicates that the cause is nonexistent or unknown.)
    */
   public FormatException(String message, Throwable cause) {
      super(message, HTTP_CODE, cause);
   }
}
