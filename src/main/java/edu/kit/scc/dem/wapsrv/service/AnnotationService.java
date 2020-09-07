package edu.kit.scc.dem.wapsrv.service;

import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.AnnotationList;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * The annotation interface to act as the bridge between the storage layer in the repositories and the REST request
 * layer in the controllers.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface AnnotationService extends WapService {
   /**
    * Adds a annotation.
    *
    * @param  a
    *           the Annotation to be added
    * @return   the annotation has been added. Could be the same Object as passed as Parameter if it can be guaranteed
    *           the it will be identical to the one could be read from the database
    */
   Annotation addAnnotation(Annotation a);

   /**
    * Updates the Annotation denoted by the given IRI in the given format with the provided String representation. This
    * is only performed if ETags match.
    * 
    * @param  iri
    *                       The IRI of the annotation
    * @param  etag
    *                       The ETag associated with the annotation state known to the client
    * @param  rawAnnotation
    *                       A String representation of the Annotation
    * @param  format
    *                       The data format used
    * @return               The Annotation created by a successful update
    * @throws WapException
    *                       In case any errors occurred .
    */
   Annotation putAnnotation(String iri, String etag, String rawAnnotation, Format format) throws WapException;

   /**
    * Posts new Annotations to the container denoted by the given containerIri in the given format. The Annotations are
    * provided as a String representation.
    * <p>
    * If given a single Annotation, a List containing a single Annotation is returned. If multiple Annotations exist in
    * the rawAnnotation, each one is added to the list.
    * 
    * @param  containerIri
    *                       The IRI of the container
    * @param  rawAnnotation
    *                       A String representation of the Annotation(s)
    * @param  format
    *                       The data format used
    * @return               The Annotation(s) created by a successful post
    * @throws WapException
    *                       In case any errors occurred
    */
   AnnotationList postAnnotation(String containerIri, String rawAnnotation, Format format) throws WapException;

   /**
    * Deletes the {@link Annotation} denoted by the given IRI if ETags match
    * 
    * @param  iri
    *                      The IRI of the annotation to delete
    * @param  etag
    *                      The ETag associated with the annotation state known to the client
    * @throws WapException
    *                      In case any errors occurred
    */
   void deleteAnnotation(String iri, String etag) throws WapException;
}
