package edu.kit.scc.dem.wapsrv.model.rdf.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.jena.commonsrdf.JenaRDF;

/**
 * This class provides the vocabulary for RDF syntax (http://www.w3.org/1999/02/22-rdf-syntax-ns#).
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfVocab {
   /**
    * html
    */
   public static IRI html = buildIri("HTML");
   /**
    * langString
    */
   public static IRI langString = buildIri("langString");
   /**
    * plainLiteral
    */
   public static IRI plainLiteral = buildIri("PlainLiteral");
   /**
    * type
    */
   public static IRI type = buildIri("type");
   /**
    * property
    */
   public static IRI property = buildIri("Property");
   /**
    * statement
    */
   public static IRI statement = buildIri("Statement");
   /**
    * subject
    */
   public static IRI subject = buildIri("subject");
   /**
    * predicate
    */
   public static IRI predicate = buildIri("predicate");
   /**
    * object
    */
   public static IRI object = buildIri("object");
   /**
    * bag
    */
   public static IRI bag = buildIri("Bag");
   /**
    * seq
    */
   public static IRI seq = buildIri("Seq");
   /**
    * alt
    */
   public static IRI alt = buildIri("Alt");
   /**
    * value
    */
   public static IRI value = buildIri("value");
   /**
    * list
    */
   public static IRI list = buildIri("List");
   /**
    * nil
    */
   public static IRI nil = buildIri("nil");
   /**
    * first
    */
   public static IRI first = buildIri("first");
   /**
    * rest
    */
   public static IRI rest = buildIri("rest");
   /**
    * xmlLiteral
    */
   public static IRI xmlLiteral = buildIri("XMLLiteral");
   private static RDF rdf;
   private static final String ROOT = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

   private static IRI buildIri(String property) {
      if (rdf == null) {
         rdf = new JenaRDF();
      }
      return rdf.createIRI(ROOT + property);
   }
}
