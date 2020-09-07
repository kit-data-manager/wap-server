package edu.kit.scc.dem.wapsrv.model.formats;

import org.springframework.stereotype.Component;

/**
 * Formatter implementing RDF/XML
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Component
public final class RdfXmlFormatter extends SimpleFormatter {
   /**
    * The string identifying RDF/XML
    */
   public static final String RDF_XML_STRING = "application/rdf+xml";

   /**
    * The constructor
    */
   public RdfXmlFormatter() {
      super(Format.RDF_XML, RDF_XML_STRING);
   }
}
