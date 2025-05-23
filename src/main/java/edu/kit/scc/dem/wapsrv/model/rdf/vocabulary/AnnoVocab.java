package edu.kit.scc.dem.wapsrv.model.rdf.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.jena.commonsrdf.JenaRDF;

/**
 * This class provides the vocabulary for annotations of the W3C (from http://www.w3.org/ns/oa#).
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class AnnoVocab {
   /**
    * annotation
    */
   public static IRI annotation = buildIri("Annotation");
   /**
    * textualBody
    */
   public static IRI textualBody = buildIri("TextualBody");
   /**
    * resourceSelection
    */
   public static IRI resourceSelection = buildIri("ResourceSelection");
   /**
    * specificResource
    */
   public static IRI specificResource = buildIri("SpecificResource");
   /**
    * fragmentSelector
    */
   public static IRI fragmentSelector = buildIri("FragmentSelector");
   /**
    * cssSelector
    */
   public static IRI cssSelector = buildIri("CssSelector");
   /**
    * xPathSelector
    */
   public static IRI xPathSelector = buildIri("XPathSelector");
   /**
    * textQuoteSelector
    */
   public static IRI textQuoteSelector = buildIri("TextQuoteSelector");
   /**
    * textPositionSelector
    */
   public static IRI textPositionSelector = buildIri("TextPositionSelector");
   /**
    * dataPositionSelector
    */
   public static IRI dataPositionSelector = buildIri("DataPositionSelector");
   /**
    * svgSelector
    */
   public static IRI svgSelector = buildIri("SvgSelector");
   /**
    * rangeSelector
    */
   public static IRI rangeSelector = buildIri("RangeSelector");
   /**
    * timeState
    */
   public static IRI timeState = buildIri("TimeState");
   /**
    * httpRequestState
    */
   public static IRI httpRequestState = buildIri("HttpRequestState");
   /**
    * cssStylesheet
    */
   public static IRI cssStylesheet = buildIri("CssStyle");
   /**
    * choice
    */
   public static IRI choice = buildIri("Choice");
   /**
    * Motivation
    */
   public static IRI motivation = buildIri("Motivation");
   /**
    * bookmarking
    */
   public static IRI bookmarking = buildIri("bookmarking");
   /**
    * classifying
    */
   public static IRI classifying = buildIri("classifying");
   /**
    * commenting
    */
   public static IRI commenting = buildIri("commenting");
   /**
    * describing
    */
   public static IRI describing = buildIri("describing");
   /**
    * editing
    */
   public static IRI editing = buildIri("editing");
   /**
    * highlighting
    */
   public static IRI highlighting = buildIri("highlighting");
   /**
    * identifying
    */
   public static IRI identifying = buildIri("identifying");
   /**
    * linking
    */
   public static IRI linking = buildIri("linking");
   /**
    * moderating
    */
   public static IRI moderating = buildIri("moderating");
   /**
    * questioning
    */
   public static IRI questioning = buildIri("questioning");
   /**
    * replying
    */
   public static IRI replying = buildIri("replying");
   /**
    * reviewing
    */
   public static IRI reviewing = buildIri("reviewing");
   /**
    * reviewing
    */
   public static IRI tagging = buildIri("tagging");
   /**
    * auto
    */
   public static IRI auto = buildIri("autoDirection");
   /**
    * ltr
    */
   public static IRI ltr = buildIri("ltrDirection");
   /**
    * rtl
    */
   public static IRI rtl = buildIri("rtlDirection");
   /**
    * body
    */
   public static IRI body = buildIri("hasBody");
   /**
    * target
    */
   public static IRI target = buildIri("hasTarget");
   /**
    * source
    */
   public static IRI source = buildIri("hasSource");
   /**
    * selector
    */
   public static IRI selector = buildIri("hasSelector");
   /**
    * state
    */
   public static IRI state = buildIri("hasState");
   /**
    * scope
    */
   public static IRI scope = buildIri("hasScope");
   /**
    * refinedBy
    */
   public static IRI refinedBy = buildIri("refinedBy");
   /**
    * startSelector
    */
   public static IRI startSelector = buildIri("hasStartSelector");
   /**
    * endSelector
    */
   public static IRI endSelector = buildIri("hasEndSelector");
   /**
    * renderedVia
    */
   public static IRI renderedVia = buildIri("renderedVia");
   /**
    * via
    */
   public static IRI via = buildIri("via");
   /**
    * canonical
    */
   public static IRI canonical = buildIri("canonical");
   /**
    * stylesheet
    */
   public static IRI stylesheet = buildIri("styledBy");
   /**
    * cached
    */
   public static IRI cached = buildIri("cachedSource");
   /**
    * motivation
    */
   public static IRI motivatedBy = buildIri("motivatedBy");
   /**
    * purpose
    */
   public static IRI purpose = buildIri("hasPurpose");
   /**
    * textDirection
    */
   public static IRI textDirection = buildIri("textDirection");
   /**
    * bodyValue
    */
   public static IRI bodyValue = buildIri("bodyValue");
   /**
    * processingLanguage
    */
   public static IRI processingLanguage = buildIri("processingLanguage");
   /**
    * exact
    */
   public static IRI exact = buildIri("exact");
   /**
    * prefix
    */
   public static IRI prefix = buildIri("prefix");
   /**
    * suffix
    */
   public static IRI suffix = buildIri("suffix");
   /**
    * styleClass
    */
   public static IRI styleClass = buildIri("styleClass");
   /**
    * sourceDate
    */
   public static IRI sourceDate = buildIri("sourceDate");
   /**
    * sourceDateStart
    */
   public static IRI sourceDateStart = buildIri("sourceDateStart");
   /**
    * sourceDateEnd
    */
   public static IRI sourceDateEnd = buildIri("sourceDateEnd");
   /**
    * start
    */
   public static IRI start = buildIri("start");
   /**
    * end
    */
   public static IRI end = buildIri("end");
   private static RDF rdf;
   private static final String ROOT = "http://www.w3.org/ns/oa#";

   private static IRI buildIri(String property) {
      if (rdf == null) {
         rdf = new JenaRDF();
      }
      return rdf.createIRI(ROOT + property);
   }
}
