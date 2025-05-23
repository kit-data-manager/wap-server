package edu.kit.scc.dem.wapsrv.model.rdf.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.jena.commonsrdf.JenaRDF;

/**
 * This class provides the vocabulary for annotations of the W3C (from http://www.w3.org/ns/ldp#).
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class LdpVocab {
   /**
    * Basic container
    */
   public static IRI basicContainer = buildIri("BasicContainer");
   /**
    * resource
    */
   public static IRI resource = buildIri("Resource");
   /**
    * rdfSource
    */
   public static IRI rdfSource = buildIri("RDFSource");
   /**
    * Container
    */
   public static IRI container = buildIri("");
   /**
    * DirectContainer
    */
   public static IRI directContainer = buildIri("DirectContainer");
   /**
    * NonRDFSource
    */
   public static IRI nonRDFSource = buildIri("NonRDFSource");
   /**
    * MemberSubject
    */
   public static IRI memberSubject = buildIri("MemberSubject");
   /**
    * PreferContainment
    */
   public static IRI preferContainment = buildIri("PreferContainment");
   /**
    * PreferMembership
    */
   public static IRI preferMembership = buildIri("PreferMembership");
   /**
    * PreferEmptyContainer
    */
   public static IRI preferEmptyContainer = buildIri("PreferEmptyContainer");
   /**
    * preferMinimalContainer
    */
   public static IRI preferMinimalContainer = buildIri("PreferMinimalContainer");
   /**
    * Page
    */
   public static IRI page = buildIri("Page");
   /**
    * PageSortCriterion
    */
   public static IRI pageSortCriterion = buildIri("PageSortCriterion");
   /**
    * contains
    */
   public static IRI contains = buildIri("contains");
   private static RDF rdf;
   private static final String ROOT = "http://www.w3.org/ns/ldp#";

   private static IRI buildIri(String property) {
      if (rdf == null) {
         rdf = new JenaRDF();
      }
      return rdf.createIRI(ROOT + property);
   }
}
