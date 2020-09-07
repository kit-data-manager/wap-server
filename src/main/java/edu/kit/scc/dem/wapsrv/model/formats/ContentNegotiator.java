package edu.kit.scc.dem.wapsrv.model.formats;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;

/**
 * This class implements content negotiation based on the HTTP accept header field.
 * <p>
 * If the field is not present or could not be processed, the default values are used. This complies to the protocol
 * since servers are free to ignore client preferences regarding formats.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class ContentNegotiator {
   /**
    * The logger to use
    */
   private static final Logger LOGGER = LoggerFactory.getLogger(ContentNegotiator.class);
   /**
    * The default format to use if none is preferred by the client
    */
   private static final Class<? extends Formatter> DEFAULT_FORMATTER_CLASS = JsonLdFormatter.class;
   /**
    * The accept header field as specified by the client
    */
   private final String accept;
   /**
    * Shows if content negotiation gives a valid result.
    * <p>
    * This means that we honored the clients preference, and not used default values
    */
   private boolean clientSelected = true;
   /**
    * The formats requested by the user.
    */
   private final List<Formatter> formatters = new Vector<Formatter>();
   /**
    * The weights to use for sorting the formatters
    */
   private final Map<Formatter, Double> formatterWeights = new Hashtable<Formatter, Double>();
   /**
    * The profile registry for JSON-LD profiles
    */
   private JsonLdProfileRegistry profileRegistry;
   /**
    * The format registry
    */
   private FormatRegistry formatRegistry;

   /**
    * Constructs a new ContentNegotiater that processes the given accept header.
    * 
    * @param accept
    *                        The accept header. If null, default values are used
    * @param type
    *                        The Type of object this accept headers belongs to (Container, Page, Annotation)
    * @param profileRegistry
    *                        The JSON-LD profile registry to use
    * @param formatRegistry
    *                        The format registry
    */
   public ContentNegotiator(String accept, Type type, JsonLdProfileRegistry profileRegistry,
         FormatRegistry formatRegistry) {
      this.accept = accept;
      this.profileRegistry = profileRegistry;
      this.formatRegistry = formatRegistry;
      if (accept == null || !processAccept(type)) {
         // Nothing requested or request could not be processed (formatters is still
         // empty), apply default values
         formatters.add(getDefaultFormatter(type));
         clientSelected = false;
      }
   }

   /**
    * Gets an instance of the default Formatter
    * 
    * @param  type
    * @return      a default formatter instance
    */
   private Formatter getDefaultFormatter(Type type) {
      try {
         Formatter formatter = DEFAULT_FORMATTER_CLASS.newInstance();
         // Autowire manually if jsonLdFormatter
         if (DEFAULT_FORMATTER_CLASS.equals(JsonLdFormatter.class)) {
            ((JsonLdFormatter) formatter).setProfileRegistry(profileRegistry);
         }
         formatter.setAcceptPart(null, type);
         // this applies the default values supplied via Accept headers
         return formatter;
      } catch (InstantiationException e) {
         LOGGER.error(e.getMessage());
      } catch (IllegalAccessException e) {
         LOGGER.error(e.getMessage());
      }
      return null;
   }

   /**
    * Called when an accept header is provided and must be processed.
    * <p>
    * Return false if we could not parse at least one format successfully from accept.
    * 
    * @param  type
    * @return      true if accept header could be successfully parsed, at least partially, false otherwise
    */
   private boolean processAccept(Type type) {
      // Example: application/ld+json; profile="http://www.example.org/ns/test.jsonld
      // http://www.example.org/ns/ldp.jsonld", text/turtle
      // Separate on commas
      String[] parts = this.accept.split(Pattern.quote(","));
      for (String part : parts) {
         ContentTypeParser parser = new ContentTypeParser(part.trim(), type, formatRegistry);
         Formatter formatter = parser.getFormatter();
         double q = parser.getQValue();
         if (formatter != null && formatter.isValid()) {
            // A valid formatter is added
            this.formatters.add(formatter);
            this.formatterWeights.put(formatter, q);
         }
      }
      // Sort using weights, do not change relative order on equality
      java.util.Collections.sort(formatters, new Comparator<Formatter>() {
         @Override
         public int compare(Formatter o1, Formatter o2) {
            double w1 = formatterWeights.get(o1);
            double w2 = formatterWeights.get(o2);
            if (w1 == w2)
               return 0;
            else
               return w1 < w2 ? +1 : -1;
         }
      });
      // If formats is still empty, all parts were skipped, we could not parse a
      // single part of the accept header
      return !this.formatters.isEmpty();
   }

   /**
    * Returns whether the given accept header could be processed correctly
    * 
    * @return true if valid, false otherwise
    */
   public boolean isClientSelected() {
      return clientSelected;
   }

   /**
    * Returns the accept header as provided by the client
    * 
    * @return The accept header, may be null
    */
   public String getAccept() {
      return accept;
   }

   /**
    * Return a list of all recognized formatters the client requested ordered by client preference
    * 
    * @return A list of all formatters
    */
   public List<Formatter> getFormatters() {
      return formatters;
   }

   /**
    * Returns the client's most wanted Formatter
    * <p>
    * This either is the one with the highest Q-Rating, or if none are provided, the first in the list that can be used.
    * If none of the client requests can be fulfilled, it is an instance of the {@link #DEFAULT_FORMATTER_CLASS}
    * 
    * @return The clients most wanted Formatter
    */
   public Formatter getFormatter() {
      return formatters.get(0);
   }

   /**
    * The Q value weight provided for the given formatter. 0.5 if none existed.
    * 
    * @param  formatter
    *                   The formatter
    * @return           The formatters Q Value
    */
   public double getQValue(Formatter formatter) {
      return formatterWeights.get(formatter);
   }

   @Override
   public String toString() {
      return "ContentNegotiator [accept=" + this.accept + ", clientSelected=" + this.clientSelected + ", formats="
            + this.formatters + "]";
   }
}
