package edu.kit.scc.dem.wapsrv.model.formats;

import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;

/**
 * The ContentTypeParser processes content type string as they are used in Accept and Content-Type HTTP Headers. The
 * class recognizes the format, extracts eventually existent Q-Values (see HTTP content negotiation) and instantiates a
 * Formatter object to process specific details of the format string, if any exist.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class ContentTypeParser {
   /**
    * The default Q Value if none is provided in the format string
    */
   private static final double DEFAULT_Q_VALUE = 1.0; // fixed to 1.0, see http spec
   private final String contentTypeString;
   private final Formatter formatter;
   private final FormatRegistry formatRegistry;
   private double qValue = DEFAULT_Q_VALUE;

   /**
    * Creates a new ContentTypeParser that processes the given contentTypeString
    * 
    * @param contentTypeString
    *                          The string to process.
    * @param type
    *                          The Type of object this accept headers belongs to (Container, Page, Annotation)
    * @param formatRegistry
    *                          The format registry
    */
   public ContentTypeParser(String contentTypeString, Type type, FormatRegistry formatRegistry) {
      this.contentTypeString = contentTypeString;
      this.formatRegistry = formatRegistry;
      this.formatter = processFormat(type);
   }

   /**
    * Process the contentTypeString.
    * <p>
    * If the given part cannot be recognized as a format, null is returned.
    * 
    * @param  type
    * @param  part
    *              The part to process
    * @return      A Formatter if the part could be parsed, null otherwise
    */
   @Autowired
   private Formatter processFormat(Type type) {
      String part = this.contentTypeString;
      if (part == null || part.length() == 0)
         return null;
      for (String formatString : formatRegistry.getFormatStrings()) {
         if (part.startsWith(formatString)) {
            // Cut off everything beyond the format string
            part = part.substring(formatString.length()).trim();
            if (part.startsWith(";")) {
               part = part.substring(1).trim();
            }
            // now this String looks like e.g. q=0.5;profile="..."
            StringBuilder acceptPartBuilder = new StringBuilder(part);
            double q = extractQValue(acceptPartBuilder);
            try {
               // Get an instance
               Formatter formatter = formatRegistry.getFormatter(formatString);
               this.qValue = q;
               formatter.setAcceptPart(acceptPartBuilder.toString(), type);
               return formatter;
            } catch (NullPointerException e) {
               LoggerFactory.getLogger(getClass()).error("Error instantiating Formatter : " + e.getMessage());
               return null;
            } // all other exceptions are not catched here and propagated up the stack
         }
      }
      return null;
   }

   /**
    * Extract the Q value of the Header if any. Remove it from the String too in this case.
    * 
    * @param  builder
    *                 The String to parse for the Q value
    * @return         The provided Q value, default if none was provided
    */
   private double extractQValue(StringBuilder builder) {
      String[] parts = builder.toString().split(Pattern.quote(";"));
      // Reset it
      builder.setLength(0);
      double q = DEFAULT_Q_VALUE;
      for (int n = 0; n < parts.length; n++) {
         String part = parts[n].trim();
         if (part.startsWith("q=")) {
            try {
               q = Double.parseDouble(part.substring(2));
            } catch (NullPointerException | NumberFormatException ex) {
               // invalid q value, ignore it
            }
         } else {
            // if not the q value, append it to the builder again
            if (builder.length() > 0) {
               builder.append(";");
               builder.append(parts[n]);
               // not part, we append it with white space if the client provided that
            } else {
               builder.append(part);
               // here part, the first uses no white space
            }
         }
      }
      return q;
   }

   /**
    * Returns the formatter implementing the format recognized during parsing the header
    * 
    * @return The formatter implementing the format
    */
   public Formatter getFormatter() {
      return formatter;
   }

   /**
    * Returns the HTTP Content Negotiation Q value provided by the client. If none was provided, {@link DEFAULT_Q_VALUE}
    * is returned
    * 
    * @return The Q valued
    */
   public double getQValue() {
      return qValue;
   }
}
