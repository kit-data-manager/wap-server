package edu.kit.scc.dem.wapsrv.model;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.simple.SimpleRDF;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfUtilities;

/**
 * The container interface defines all methods special to containers. These address root containers, labels and other
 * container-only metadata, page information and the number of contained annotations.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface Container extends WapObject {
   /**
    * Gets the human readable label of the container
    * 
    * @return The label
    */
   String getLabel();

   /**
    * Creates a default label value (=name of the container) if none exists
    */
   void createDefaultLabel();

   /**
    * Checks if PreferMinimalContainer preference is set
    * 
    * @return True if PreferMinimalContainer
    */
   boolean isMinimalContainer();

   /**
    * Generates the IRI for the rdf:seq of a container holding the annotations.
    *
    * @param  iri
    *             the IRI of the container
    * @return     the IRI of the rdf:seq for Annotations
    */
   static BlankNodeOrIRI toAnnotationSeqIri(BlankNodeOrIRI iri) {
      SimpleRDF rdf = new SimpleRDF();
      return rdf.createIRI(toAnnotationSeqIriString(RdfUtilities.nStringToString(iri.ntriplesString())));
   }

   /**
    * Generates the IRI String for the rdf:seq of a container holding the annotations.
    *
    * @param  containerIriString
    *                            the IRI of the container
    * @return                    the IRI String of the rdf:seq for Annotations
    */
   static String toAnnotationSeqIriString(String containerIriString) {
      return containerIriString + "#annotations";
   }

   /**
    * Generates the IRI for the rdf:seq of a container holding the subcontainers.
    *
    * @param  iri
    *             the IRI of the container
    * @return     the IRI of the rdf:seq for Subcontainers
    */
   static BlankNodeOrIRI toContainerSeqIri(BlankNodeOrIRI iri) {
      SimpleRDF rdf = new SimpleRDF();
      return rdf.createIRI(toContainerSeqIriString(RdfUtilities.nStringToString(iri.ntriplesString())));
   }

   /**
    * Generates the IRI String for the rdf:seq of a container holding the subcontainers.
    *
    * @param  containerIriString
    *                            the IRI of the container
    * @return                    the IRI String of the rdf:seq for Subcontainers
    */
   static String toContainerSeqIriString(String containerIriString) {
      return containerIriString + "#containers";
   }
}
