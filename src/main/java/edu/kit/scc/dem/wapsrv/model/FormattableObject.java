package edu.kit.scc.dem.wapsrv.model;

import edu.kit.scc.dem.wapsrv.exceptions.FormatNotAvailableException;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * Everything that need to have a String serialization for usage in HTTP response bodies has to implement this
 * interface.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface FormattableObject {
   /**
    * The different types of formattable objects that may exist
    */
   public static enum Type {
   /**
    * Type for annotations
    */
   ANNOTATION,
   /**
    * Type for pages
    */
   PAGE,
   /**
    * Type for containers
    */
   CONTAINER
   }

   /**
    * Returns a String serialization in the given format
    * 
    * @param  format
    *                                     The format
    * @return                             A String serialization
    * @throws FormatNotAvailableException
    *                                     if format not supported
    */
   String toString(Format format) throws FormatNotAvailableException;

   /**
    * Gets the type of the formattable object
    * 
    * @return The type of formattable object
    */
   Type getType();
}
