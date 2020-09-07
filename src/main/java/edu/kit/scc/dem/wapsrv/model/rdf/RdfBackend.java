package edu.kit.scc.dem.wapsrv.model.rdf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.RDF;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * The RdfBackend interface used to generate data sets from Strings and Strings from data sets. This is
 * TripleStore-dependent.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface RdfBackend {
   /**
    * Returns the underlying RDF implementation
    * 
    * @return The underlying RDF implementation
    */
   RDF getRdf();

   /**
    * Reads in a given file and parses its content in the given format
    * 
    * @param  filename
    *                      The file to read
    * @param  format
    *                      The format to use
    * @return              The parsed data set, null if any IO errors occurred
    * @throws WapException
    *                      For any error aside from IO errors
    */
   default Dataset readFromFile(String filename, Format format) throws WapException {
      try {
         String serialization = new String(Files.readAllBytes(Paths.get(filename)));
         return readFromString(serialization, format);
      } catch (IOException e) {
         return null;
      }
   }

   /**
    * Parses a data set from the given String in the given format
    * 
    * @param  serialization
    *                       The serialized RDF
    * @param  format
    *                       The format
    * @return               The parsed data set
    * @throws WapException
    *                       If any error occurs
    */
   Dataset readFromString(String serialization, Format format) throws WapException;

   /**
    * Writes the given data set in the given format to a string serialization
    * 
    * @param  dataset
    *                      The data set
    * @param  format
    *                      The format
    * @return              The serialized string
    * @throws WapException
    *                      In case any error occurred
    */
   String getOutput(Dataset dataset, Format format) throws WapException;

   /**
    * Tests whether the given format is usable by the actual RdfBackend implementation
    * 
    * @param  format
    *                The format to check
    * @return        true if supported
    */
   boolean isValidInputFormat(Format format);
}
