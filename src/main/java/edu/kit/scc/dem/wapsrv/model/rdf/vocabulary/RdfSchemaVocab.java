package edu.kit.scc.dem.wapsrv.model.rdf.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

/**
 * This class provides the vocabulary for RDF schema (https://www.w3.org/2000/01/rdf-schema#) this class currently only
 * implements the needed "label"
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfSchemaVocab {
   /**
    * label
    */
   public static IRI label = buildIri("label");
   private static RDF rdf;
   private static final String ROOT = "http://www.w3.org/2000/01/rdf-schema#";

   private static IRI buildIri(String property) {
      if (rdf == null) {
         rdf = new SimpleRDF();
      }
      return rdf.createIRI(ROOT + property);
   }
}
