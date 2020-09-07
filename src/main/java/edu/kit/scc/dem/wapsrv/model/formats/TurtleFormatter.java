package edu.kit.scc.dem.wapsrv.model.formats;

import org.springframework.stereotype.Component;

/**
 * Formatter implementing TURTLE
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Component
public final class TurtleFormatter extends SimpleFormatter {
   /**
    * The string identifying TURTLE
    */
   public static final String TURTLE_STRING = "text/turtle";

   /**
    * The constructor
    */
   public TurtleFormatter() {
      super(Format.TURTLE, TURTLE_STRING);
   }
}
