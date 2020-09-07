package edu.kit.scc.dem.wapsrv.model.formats;

import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;

/**
 * Formatters are used to implement a new format in this application. They provide the following features:<br>
 * <ul>
 * <li>Parse incoming HTTP Accept Headers additional Format info</li>
 * <li>Format String representations of the format applying the additional info</li>
 * <li>Get HTTP ContentType Header values to use in HTTP Responses</li>
 * </ul>
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface Formatter {
   /**
    * Return the Format implemented by this Formatter
    * 
    * @return The format
    */
   Format getFormat();

   /**
    * Return the Format-String used to recognize this Format in HTTP Accept Headers
    * <p>
    * This includes only the part denoting the media type, but not additional information provided.
    * 
    * @return The formatString
    */
   String getFormatString();

   /**
    * Formats a given FormattableObject with this formatter
    * 
    * @param  obj
    *             The Object for format
    * @return     The String representation of the given FormattableObject
    */
   String format(FormattableObject obj);

   /**
    * Returns whether this formatter is valid. This means that setAcceptPart(String, Type) was called with a valid HTTP
    * Accept Header part. Without calling this method before, the formatter is always invalid.
    * 
    * @return Whether this formatter has been correctly initialized by setAcceptPart(String, Type)
    */
   boolean isValid();

   /**
    * Updates this formatter with additional information provided by the HTTP Accept header.
    * <p>
    * This method must always be called, eventually with a null value, if the format does not need additional info, or
    * this formatter cannot be used / is inValid
    * 
    * @param acceptPart
    *                   The part of the HTTP Accept Header with additional information
    * @param type
    *                   The type of Object this formatter will be used with. May be null.
    */
   void setAcceptPart(String acceptPart, Type type);

   /**
    * Return a String representation of this formatter that can be used in the HTTP Responses as ContentType Header
    * 
    * @return String representation for ContentType HTTP Header
    */
   String getContentType();
}
