package edu.kit.scc.dem.wapsrv.model.rdf.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.jena.commonsrdf.JenaRDF;

/**
 * This class provides the vocabulary for dc-terms (http://dublincore.org/documents/dcmi-terms/).
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class DcTermsVocab {
   /**
    * abstract (abstract alone is a reserved term in java, hence the "_")
    */
   public static IRI abstractVoc = buildIri("http://purl.org/dc/terms/abstract");
   /**
    * abstractaccessRights
    */
   public static IRI abstractaccessRights = buildIri("http://purl.org/dc/terms/abstractaccessRights");
   /**
    * accrualMethod
    */
   public static IRI accrualMethod = buildIri("http://purl.org/dc/terms/accrualMethod");
   /**
    * accrualPeriodicity
    */
   public static IRI accrualPeriodicity = buildIri("http://purl.org/dc/terms/accrualPeriodicity");
   /**
    * accrualPolicy
    */
   public static IRI accrualPolicy = buildIri("http://purl.org/dc/terms/accrualPolicy");
   /**
    * alternative
    */
   public static IRI alternative = buildIri("http://purl.org/dc/terms/alternative");
   /**
    * audience
    */
   public static IRI audience = buildIri("http://purl.org/dc/terms/audience");
   /**
    * available
    */
   public static IRI available = buildIri("http://purl.org/dc/terms/available");
   /**
    * bibliographicCitation
    */
   public static IRI bibliographicCitation = buildIri("http://purl.org/dc/terms/bibliographicCitation");
   /**
    * conformsTo
    */
   public static IRI conformsTo = buildIri("http://purl.org/dc/terms/conformsTo");
   /**
    * contributor
    */
   public static IRI contributor = buildIri("http://purl.org/dc/terms/contributor");
   /**
    * coverage
    */
   public static IRI coverage = buildIri("http://purl.org/dc/terms/coverage");
   /**
    * created
    */
   public static IRI created = buildIri("http://purl.org/dc/terms/created");
   /**
    * creator
    */
   public static IRI creator = buildIri("http://purl.org/dc/terms/creator");
   /**
    * date
    */
   public static IRI date = buildIri("http://purl.org/dc/terms/date");
   /**
    * dateAccepted
    */
   public static IRI dateAccepted = buildIri("http://purl.org/dc/terms/dateAccepted");
   /**
    * dateCopyrighted
    */
   public static IRI dateCopyrighted = buildIri("http://purl.org/dc/terms/dateCopyrighted");
   /**
    * dateSubmitted
    */
   public static IRI dateSubmitted = buildIri("http://purl.org/dc/terms/dateSubmitted");
   /**
    * description
    */
   public static IRI description = buildIri("http://purl.org/dc/terms/description");
   /**
    * educationLevel
    */
   public static IRI educationLevel = buildIri("http://purl.org/dc/terms/educationLevel");
   /**
    * extent
    */
   public static IRI extent = buildIri("http://purl.org/dc/terms/extent");
   /**
    * format
    */
   public static IRI format = buildIri("http://purl.org/dc/terms/format");
   /**
    * hasFormat
    */
   public static IRI hasFormat = buildIri("http://purl.org/dc/terms/hasFormat");
   /**
    * hasPart
    */
   public static IRI hasPart = buildIri("http://purl.org/dc/terms/hasPart");
   /**
    * hasVersion
    */
   public static IRI hasVersion = buildIri("http://purl.org/dc/terms/hasVersion");
   /**
    * identifier
    */
   public static IRI identifier = buildIri("http://purl.org/dc/terms/identifier");
   /**
    * instructionalMethod
    */
   public static IRI instructionalMethod = buildIri("http://purl.org/dc/terms/instructionalMethod");
   /**
    * isFormatOf
    */
   public static IRI isFormatOf = buildIri("http://purl.org/dc/terms/isFormatOf");
   /**
    * isPartOf
    */
   public static IRI isPartOf = buildIri("http://purl.org/dc/terms/isPartOf");
   /**
    * isReferencedBy
    */
   public static IRI isReferencedBy = buildIri("http://purl.org/dc/terms/isReferencedBy");
   /**
    * isReplacedBy
    */
   public static IRI isReplacedBy = buildIri("http://purl.org/dc/terms/isReplacedBy");
   /**
    * isRequiredBy
    */
   public static IRI isRequiredBy = buildIri("http://purl.org/dc/terms/isRequiredBy");
   /**
    * issued
    */
   public static IRI issued = buildIri("http://purl.org/dc/terms/issued");
   /**
    * isVersionOf
    */
   public static IRI isVersionOf = buildIri("http://purl.org/dc/terms/isVersionOf");
   /**
    * language
    */
   public static IRI language = buildIri("http://purl.org/dc/terms/language");
   /**
    * license
    */
   public static IRI license = buildIri("http://purl.org/dc/terms/license");
   /**
    * mediator
    */
   public static IRI mediator = buildIri("http://purl.org/dc/terms/mediator");
   /**
    * medium
    */
   public static IRI medium = buildIri("http://purl.org/dc/terms/medium");
   /**
    * modified
    */
   public static IRI modified = buildIri("http://purl.org/dc/terms/modified");
   /**
    * provenance
    */
   public static IRI provenance = buildIri("http://purl.org/dc/terms/provenance");
   /**
    * publisher
    */
   public static IRI publisher = buildIri("http://purl.org/dc/terms/publisher");
   /**
    * references
    */
   public static IRI references = buildIri("http://purl.org/dc/terms/references");
   /**
    * relation
    */
   public static IRI relation = buildIri("http://purl.org/dc/terms/relation");
   /**
    * replaces
    */
   public static IRI replaces = buildIri("http://purl.org/dc/terms/replaces");
   /**
    * requires
    */
   public static IRI requires = buildIri("http://purl.org/dc/terms/requires");
   /**
    * rights
    */
   public static IRI rights = buildIri("http://purl.org/dc/terms/rights");
   /**
    * rightsHolder
    */
   public static IRI rightsHolder = buildIri("http://purl.org/dc/terms/rightsHolder");
   /**
    * source
    */
   public static IRI source = buildIri("http://purl.org/dc/terms/source");
   /**
    * spatial
    */
   public static IRI spatial = buildIri("http://purl.org/dc/terms/spatial");
   /**
    * subject
    */
   public static IRI subject = buildIri("http://purl.org/dc/terms/subject");
   /**
    * tableOfContents
    */
   public static IRI tableOfContents = buildIri("http://purl.org/dc/terms/tableOfContents");
   /**
    * temporal
    */
   public static IRI temporal = buildIri("http://purl.org/dc/terms/temporal");
   /**
    * title
    */
   public static IRI title = buildIri("http://purl.org/dc/terms/title");
   /**
    * type
    */
   public static IRI type = buildIri("http://purl.org/dc/terms/type");
   /**
    * valid
    */
   public static IRI valid = buildIri("http://purl.org/dc/terms/valid");
   private static RDF rdf;

   private static IRI buildIri(String property) {
      if (rdf == null) {
         rdf = new JenaRDF();
      }
      return rdf.createIRI(property);
   }
}
