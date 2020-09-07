package edu.kit.scc.dem.wapsrv.model.formats;

import org.springframework.stereotype.Component;

/**
 * Formatter implementing N-QUADS
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Component
public final class NquadsFormatter extends SimpleFormatter {
   /**
    * The string identifying RDF
    */
   public static final String NQUADS_STRING = "application/n-quads"; // see https://www.w3.org/TR/n-quads/#sec-mediaReg

   /**
    * The constructor
    */
   public NquadsFormatter() {
      super(Format.NQUADS, NQUADS_STRING);
   }
}
