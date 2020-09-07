package edu.kit.scc.dem.wapsrv.model;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.RDF;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * Interface used for creation of internal data models. Also some basic operations are implemented here.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface ModelFactory {
   /**
    * Returns the containerIri of a given annotationIri
    * 
    * @param  annotationIri
    *                       the annotation IRI to extract the container IRI from
    * @return               The container IRI, null if not a valid annotation IRI
    */
   static String getContainerIriFormAnnotationIri(String annotationIri) {
      if (annotationIri == null)
         return null;
      int lastIndex = annotationIri.lastIndexOf("/");
      if (lastIndex == -1)
         return null;
      // +1 to include the "/"
      return annotationIri.substring(0, lastIndex + 1);
   }

   /**
    * Returns the underlying RDF Object
    * 
    * @return The RDF
    */
   RDF getRDF();

   /**
    * Creation an annotation from the given formatted string. The annotation will use the IRI that is implicit in the
    * string.
    * 
    * @param  rawAnnotation
    *                       The serialized string of the annotation
    * @param  format
    *                       The format of the serialized string
    * @return               The created annotation
    * @throws WapException
    *                       if any error occurred
    */
   Annotation createAnnotation(String rawAnnotation, Format format) throws WapException;

   /**
    * Creates an annotation with the given data set as backend
    * 
    * @param  dataset
    *                 The data set
    * @return         The annotation
    */
   Annotation createAnnotation(Dataset dataset);

   /**
    * Create an annotation list from the given parameters. All existent annotations in the serialized string will be
    * parsed and added to the annotation list.<br>
    * The annotations will use the IRIs that are implicit in the string.
    * 
    * @param  rawAnnotation
    *                       The serialized string of the annotations
    * @param  format
    *                       The format of the serialized string
    * @return               The created annotationList
    * @throws WapException
    *                       if any error occurred
    */
   AnnotationList createAnnotationList(String rawAnnotation, Format format) throws WapException;

   /**
    * Creates a container from the given parameters. The container will use the IRI that is implicit in the string.
    * 
    * @param  rawContainer
    *                         The serialized string of the container
    * @param  format
    *                         The format of the serialized string
    * @param  newContainerIri
    *                         The new IRI for Container
    * @return                 The container
    * @throws WapException
    *                         if any error occurred
    */
   Container createContainer(String rawContainer, Format format, String newContainerIri) throws WapException;

   /**
    * Creates a container with the given dataset as backend
    * 
    * @param  dataset
    *                                The dataset with the containers data from the database.
    * @param  preferMinimalContainer
    *                                true, if the minimalContainer was requested
    * @param  preferIrisOnly
    *                                true, if only the IRIS without the body should be returned.
    * @return                        The container created
    */
   Container createContainer(Dataset dataset, boolean preferMinimalContainer, boolean preferIrisOnly);

   /**
    * Creates a container with the given dataset as backend
    * 
    * @param  dataset
    *                 The dataset
    * @return         The container
    */
   Container createContainer(Dataset dataset);

   /**
    * Creates a page with the given data set as backend
    * 
    * @param  dataset
    *                        The data set
    * @param  containerIri
    *                        the IRI of the container requested
    * @param  pageNr
    *                        the pageNumber requested
    * @param  preferIrisOnly
    *                        true, if only the IRIS without the body should be returned.
    * @param  annoTotalCount
    *                        the total Count of the Annotations included
    * @param  modified
    *                        the modified data of the container
    * @param  label
    *                        the label of the container
    * @param  isEmbedded
    *                        true, if the page is created for embedding it into a container.
    * @return                The page
    */
   Page createPage(Dataset dataset, String containerIri, int pageNr, boolean preferIrisOnly, boolean isEmbedded,
         int annoTotalCount, String modified, String label);

   /**
    * Checks whether a given Format is valid for usage in PUT/POST requests
    * 
    * @param  format
    *                The format to check for validity
    * @return        true if format can be used, false otherwise
    */
   boolean isValidInputFormat(Format format);

   /**
    * Converts the format of a given annotation/container from srcFormat to destFormat
    * 
    * @param  rawString
    *                    the serialized representation in the source format
    * @param  srcFormat
    *                    The source format
    * @param  destFormat
    *                    The destination format
    * @return            The converted string
    */
   String convertFormat(String rawString, Format srcFormat, Format destFormat);
}
