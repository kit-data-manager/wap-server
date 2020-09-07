package edu.kit.scc.dem.wapsrv.service;

import java.util.Set;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.Page;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * The container interface to act as the bridge between the storage layer in the repositories and the REST request layer
 * in the controllers.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface ContainerService extends WapService {
   /**
    * Gets the Container denoted by the given IRI. Preferences are applied.
    * 
    * @param  iri
    *                      The IRI of the container
    * @param  preferences
    *                      List of container preferences
    * @return              The requested Container
    * @throws WapException
    *                      in case any errors occurred
    */
   Container getContainer(String iri, Set<Integer> preferences) throws WapException;

   /**
    * Creates a new Container with the given name as a child to the Container denoted by the baseContainerIri. The
    * Container is serialized as a String in rawContainer with the given Format and constraints.
    * 
    * @param  baseContainerIri
    *                          The IRI of the container to which the newly created one is added
    * @param  name
    *                          The name to use for the new container
    * @param  rawContainer
    *                          The serialized Container
    * @param  format
    *                          The provided format of the rawContainer
    * @return                  The created Container
    * @throws WapException
    *                          In case any errors occurred
    */
   Container postContainer(String baseContainerIri, String name, String rawContainer, Format format)
         throws WapException;

   /**
    * Deletes the {@link Container} denoted by the given IRI if ETags match A Container can only be deleted if it
    * contains no sub-containers. Contained Annotations do not prevent deletion, they will get deleted too.
    * 
    * @param  iri
    *                      The IRI of the container to delete
    * @param  etag
    *                      The ETag associated with the container state known to the client
    * @throws WapException
    *                      In case any errors occurred
    */
   void deleteContainer(String iri, String etag) throws WapException;

   /**
    * Gets the Page with the given number of the Container denoted by the given IRI. Preference of Annotation
    * representation is applied.
    * 
    * @param  containerIri
    *                             The IRI of the container
    * @param  containerPreference
    *                             The preference regarding Annotation representation
    * @param  pageNr
    *                             The number of the page
    * @return                     The requested Page
    * @throws WapException
    *                             In case any errors occurred
    */
   Page getPage(String containerIri, int containerPreference, int pageNr) throws WapException;
}
