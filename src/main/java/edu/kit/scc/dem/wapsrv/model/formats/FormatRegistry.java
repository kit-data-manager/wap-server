package edu.kit.scc.dem.wapsrv.model.formats;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;

/**
 * This class is the central registry for output formatters.
 * <p>
 * All {@link Formatter}s found are automatically registered.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
// The class is final so no subclasses can exist. A single instance is needed.
@Component
public final class FormatRegistry {
   /**
    * A map of all registered format string and the corresponding classes implementing the format
    */
   private final Map<String, Class<? extends Formatter>> formatString2Formatter
         = new Hashtable<String, Class<? extends Formatter>>();
   /**
    * A map of all registered format string to the Format implemented. Needed only for autocreated simple formatters.
    */
   private final Map<String, Format> formatString2Format = new Hashtable<String, Format>();
   /**
    * The profile registry
    */
   @Autowired
   private JsonLdProfileRegistry profileRegistry;

   /**
    * The constructor is called by spring once and sets the {@link instance} variable of the class.
    */
   private FormatRegistry() {
      // Register single instance created by Spring
      // instance = this;
   }

   @Autowired
   private void autocreateSimpleFormatters(WapServerConfig config) {
      String simpleFormatters = config.getSimpleFormatters();
      if (simpleFormatters == null) {
         return;
      }
      String newSimpleFormatters = simpleFormatters.trim();
      String[] formatterParts = newSimpleFormatters.split(Pattern.quote("|"));
      if (formatterParts == null)
         return;
      for (String formatterPart : formatterParts) {
         addSimpleFormatter(formatterPart);
      }
   }

   private void addSimpleFormatter(String formatterPart) {
      if (formatterPart == null) {
         return;
      }
      String newFormatterPart = formatterPart.trim();
      String[] parts = newFormatterPart.split(Pattern.quote("*"));
      if (parts == null || parts.length != 2)
         return;
      try {
         Format format = Format.valueOf(parts[0]);
         String contentType = parts[1];
         Formatter formatter = new SimpleFormatter(format, contentType);
         // The formatter class itself cannot be used, because getInstance() would not
         // have access to format and contentType anymore. We instantiate it here solely to
         // test it can be instantiated with these values. We store them separately
         // and create the fitting class on access with the needed values
         // The values that are needed can then be recognized as the string gets mapped to
         // the simple formatter class, not a subclass of it
         registerFormatter(formatter);
         this.formatString2Format.put(contentType, format);
      } catch (IllegalArgumentException ex) {
         LoggerFactory.getLogger(getClass()).info("Autocreation of simple formatter failed : " + formatterPart);
      }
   }

   /**
    * Registers all formatters found by Spring autowiring
    * 
    * @param formatters
    *                   The found formatters to register
    */
   @Autowired
   private void registerFormatters(List<Formatter> formatters) {
      for (Formatter formatter : formatters) {
         registerFormatter(formatter);
      }
   }

   /**
    * Registers a given formatter
    * 
    * @param formatter
    *                  The formatter to register
    */
   protected void registerFormatter(Formatter formatter) {
      formatString2Formatter.put(formatter.getFormatString(), formatter.getClass());
      LoggerFactory.getLogger(getClass()).info("New Formatter registered : " + formatter.getClass().getName()
            + " for ContentType : " + formatter.getFormatString());
   }

   /**
    * Returns a set of all registered format strings
    * 
    * @return The registered format strings
    */
   public Set<String> getFormatStrings() {
      return formatString2Formatter.keySet();
   }

   /**
    * Gets and instance of the formatter that implements the given format.
    * 
    * @param  formatString
    *                      The format string of which to get the formatter class implementing it
    * @return              The formatter implementing the given formatString
    */
   public Formatter getFormatter(String formatString) {
      if (formatString == null) {
         throw new NullPointerException("Null pointer given, formatString must not be null");
      }
      Class<? extends Formatter> formatterClass = formatString2Formatter.get(formatString);
      if (formatterClass == null) {
         throw new NullPointerException("No class known to implement the given format string " + formatString);
      }
      if (formatterClass.getSimpleName().equals("SimpleFormatter")) {
         // These cannot be directly instantiated, by we have all information needed
         Format format = formatString2Format.get(formatString);
         if (format == null) {
            throw new InternalServerException(
                  "Format String for autocreated formatter has no known Format mapping : " + formatString);
         }
         return new SimpleFormatter(format, formatString);
      } else {
         Formatter formatter;
         try {
            formatter = formatterClass.newInstance();
            // Autowire manually if jsonLdFormatter
            if (formatterClass.equals(JsonLdFormatter.class)) {
               ((JsonLdFormatter) formatter).setProfileRegistry(profileRegistry);
            }
         } catch (InstantiationException | IllegalAccessException e) {
            throw new InternalServerException(
                  "Could not instantiate Formatter : " + formatString + " - " + e.getMessage());
         }
         return formatter;
      }
   }
}
