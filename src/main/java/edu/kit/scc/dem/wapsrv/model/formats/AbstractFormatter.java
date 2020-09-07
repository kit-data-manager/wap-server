package edu.kit.scc.dem.wapsrv.model.formats;

/**
 * Base class for all Formatters.
 * <p>
 * To use auto-registration, all implementing subclasses need the Spring (at)Component annotation.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public abstract class AbstractFormatter implements Formatter {
   /**
    * The implemented format
    */
   private final Format format;
   /**
    * Is the formatter valid or not
    */
   private boolean valid = false;

   /**
    * Generates an abstract formatter for Format format
    * 
    * @param format
    *               The format to implement .
    */
   protected AbstractFormatter(Format format) {
      this.format = format;
   }

   @Override
   public Format getFormat() {
      return format;
   }

   /**
    * Sets the formatter valid or not
    * 
    * @param valid
    *              true=valid, false=invalid
    */
   protected void setValid(boolean valid) {
      this.valid = valid;
   }

   @Override
   public boolean isValid() {
      return valid;
   }
}
