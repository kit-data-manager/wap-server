package edu.kit.scc.dem.wapsrv.model.rdf.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

/**
 * This class provides the vocabulary for this Project (from http://dem.scc.kit.edu/wapserv/ns#).
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class WapVocab {
   /**
    * deleted - marks an item as deleted in the database
    */
   public static IRI deleted = buildIri("deleted");
   /**
    * etag - under this the etag of an object is stored in the database
    */
   public static IRI etag = buildIri("etag");
   private static RDF rdf;
   private static final String ROOT = "http://dem.scc.kit.edu/wapserv/ns#";

   private static IRI buildIri(String property) {
      if (rdf == null) {
         rdf = new SimpleRDF();
      }
      return rdf.createIRI(ROOT + property);
   }
}
