package edu.kit.scc.dem.wapsrv.model;

import java.util.List;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfUtilities;

/**
 * AnnotationList is a simple list of annotations without the additional metadata of pages.
 * <p>
 * It is used as the incoming format in postAnnotation requests. The POST of a single annotation is implemented as
 * posting a list of a single annotation, posting many is obviously a list.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface AnnotationList extends FormattableObject, Iterable<Annotation> {
   /**
    * Returns the IRI of the container these annotations should be posted to.
    * 
    * @return The container IRI
    */
   IRI getContainerIri();

   /**
    * Sets the containerIri of the container these annotation list is posted to.
    * 
    * @param containerIri
    *                     The container IRI to set
    */
   void setContainerIri(String containerIri);

   /**
    * Sets the containerIri of the container these annotation list is posted to.
    * 
    * @param containerIri
    *                     The container IRI to set
    */
   void setContainerIri(IRI containerIri);

   /**
    * Returns the ETag of the container these annotations should be posted to.
    * 
    * @return The container ETag
    */
   String getContainerEtag();

   /**
    * Sets the containerEtag of the container these annotation list is posted to
    * 
    * @param containerEtag
    *                      .
    */
   void setContainerEtag(String containerEtag);

   /**
    * Adds an annotation to this list
    * 
    * @param anno
    *             The annotation to add
    */
   void addAnnotation(Annotation anno);

   /**
    * Returns a List of the annotations in this page.
    * 
    * @return List of annotations. Is null if PreferMinimalIri is set
    */
   List<Annotation> getAnnotations();

   /**
    * Returns the size of the annotation list
    * 
    * @return The number of annotations in the list
    */
   default int size() {
      return getAnnotations().size();
   }

   /**
    * Returns the IRI of the annotation list.<br>
    * This is either the IRI of the the first annotation if the list contains only one, or the IRI of the container if
    * more a contained.
    * 
    * @return The IRI of the annotation list
    */
   default BlankNodeOrIRI getIri() {
      if (getAnnotations().size() == 1) {
         return getAnnotations().get(0).getIri();
      } else {
         // When multiple annotations exist, we return the container IRI
         return getContainerIri();
      }
   }

   /**
    * Returns the IRI of the WapObject.
    * 
    * @return The IRI
    */
   default String getIriString() {
      return RdfUtilities.nStringToString(getIri().ntriplesString());
   }

   /**
    * Returns the ETag of the annotation list.<br>
    * This is either the ETag of the the first annotation if the list contains only one, or the ETag of the container if
    * more a contained.
    * 
    * @return The ETag of the annotation list
    */
   default String getEtag() {
      if (getAnnotations().size() == 1) {
         return getAnnotations().get(0).getEtag();
      } else {
         // When multiple annotations exist, we return the container ETag
         return getContainerEtag();
      }
   }

   /**
    * Gets the etag in quotes.
    *
    * @return the String of the etag surrounded with quotes
    */
   default String getEtagQuoted() {
      return "\"" + getEtag() + "\"";
   }
}
