package edu.kit.scc.dem.wapsrv.model.formats;

/**
 * The known formats. Their existence here does not mean anything. If the format should be usable as output, a Formatter
 * for it has to be implemented. To be usable as input, there are two cases:
 * <ul>
 * <li>With Annotation Validation: A validator has to be implemented for it and the RdfBackend must support it
 * <li>Without Annotation Validation: The RdfBackend used must support it
 * </ul>
 * <p>
 * The input constraints apply only to annotations, input of containers relies only on backend support.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public enum Format {
   /**
    * JSON-LD Format
    */
   JSON_LD,
   /**
    * Turtle Format
    */
   TURTLE,
   /**
    * RDF/XML
    */
   RDF_XML,
   /**
    * RDF Json
    */
   RDF_JSON,
   /**
    * NTriples
    */
   NTRIPLES,
   /**
    * NQuads
    */
   NQUADS;
}
