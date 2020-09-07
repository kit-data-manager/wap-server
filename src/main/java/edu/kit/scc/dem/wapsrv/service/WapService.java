package edu.kit.scc.dem.wapsrv.service;

import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * Base interface for WapServices
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface WapService {
   /**
    * Checks whether a given format is valid for usage in PUT/POST requests
    * 
    * @param  format
    *                The format to check for validity
    * @return        true if format can be used, false otherwise
    */
   boolean isValidInputFormat(Format format);

   /**
    * Gets the Annotation denoted by the given IRI
    * 
    * @param  iri
    *                      The IRI of the annotation
    * @return              The annotation requested
    * @throws WapException
    *                      In case any errors occurred
    */
   Annotation getAnnotation(String iri) throws WapException;
}
