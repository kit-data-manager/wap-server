/**
 * 
 */
package edu.kit.scc.dem.wapsrv.model.formats;

import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;

/**
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class InvalidFormatterForTests extends SimpleFormatter {
   /**
    * The string identifying the invalid format
    */
   public static final String FORMAT_STRING = "application/invalid+format";

   /**
    * Creates a new invalid format
    */
   public InvalidFormatterForTests() {
      super(Format.RDF_JSON, FORMAT_STRING);
   }

   @Override
   public void setAcceptPart(String acceptPart, Type type) {
      // we should always be invalid
      setValid(false);
   }
}
