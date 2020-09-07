package edu.kit.scc.dem.wapsrv.model;

/**
 * The annotation interface contains all methods special to annotations. As most of the needed functionality is already
 * defined in {@link WapObject} there are only a few addressing the container IRI and the question if a target exists.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface Annotation extends WapObject {
   /**
    * Gets the container IRI
    * 
    * @return IRI as String
    */
   String getContainerIri();

   /**
    * Returns whether this annotation has a target
    * 
    * @return true if target existent, false otherwise
    */
   boolean hasTarget();
}
