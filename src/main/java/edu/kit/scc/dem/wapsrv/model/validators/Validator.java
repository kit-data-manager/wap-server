package edu.kit.scc.dem.wapsrv.model.validators;

import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * A validator validates a String of its implemented format against the WADM Schema.<br>
 * An implementation is required if Annotation Validation is activated and annotations should support PUT/POST with the
 * implemented format.
 * <p>
 * Implementing subclasses must use at (at)Component annotation of Spring, otherwise they are not registered.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface Validator {
   /**
    * Validates a given AnnotationString in the given format against the Web Annotation Data Model - Schema.
    * 
    * @param  annotationString
    *                          The annotation as a String
    * @return                  True if a valid annotation, false otherwise
    */
   boolean validateAnnotation(String annotationString);

   /**
    * Validates a given ContainerString in the given format against the Web Annotation Data Model - Schema. It has to be
    * a valid AnnotationCollection.
    * 
    * @param  containerString
    *                         The container as a String
    * @return                 True if a valid AnnotationCollection, false otherwise
    */
   boolean validateContainer(String containerString);

   /**
    * Gets the implemented format
    * 
    * @return The implemented format
    */
   Format getFormat();
}
