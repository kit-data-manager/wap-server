package edu.kit.scc.dem.wapsrv.repository.jena;

import java.util.Hashtable;
import java.util.Map;
import org.apache.jena.riot.Lang;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * This is the central point where are formats that should be usable by Jena are registered. Both incoming and outgoing
 * formats must be registered here.
 * <p>
 * Attention: This only indicates triple store support. Depending on the configuration of the server additional
 * validators may be needed for input formats and Formatters for output formats.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class JenaFormatMapper {
   /**
    * The single instance
    */
   private static final JenaFormatMapper INSTANCE = new JenaFormatMapper();
   /**
    * The map containing the registered mappings
    */
   private Map<Format, Lang> format2lang = new Hashtable<Format, Lang>();

   /**
    * Constructs the Jena format mapper singleton.
    */
   private JenaFormatMapper() {
      format2lang.put(Format.JSON_LD, Lang.JSONLD);
      format2lang.put(Format.TURTLE, Lang.TURTLE);
      format2lang.put(Format.RDF_XML, Lang.RDFXML);
      format2lang.put(Format.NQUADS, Lang.NQUADS);
      format2lang.put(Format.NTRIPLES, Lang.NTRIPLES);
      format2lang.put(Format.RDF_JSON, Lang.RDFJSON);
   }

   /**
    * Maps the given format to the corresponding Jena Lang
    * 
    * @param  format
    *                The format to map
    * @return        The corresponding Jena lang, null if format unsupported or format==null
    */
   protected static Lang map(Format format) {
      if (format == null)
         return null;
      return INSTANCE.format2lang.get(format);
   }
}
