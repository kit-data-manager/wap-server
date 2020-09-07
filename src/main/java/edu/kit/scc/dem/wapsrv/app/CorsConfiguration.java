package edu.kit.scc.dem.wapsrv.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class centrally manages all CORS relevant configuration
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class CorsConfiguration {
   /**
    * The logger to use
    */
   private static final Logger LOGGER = LoggerFactory.getLogger(CorsConfiguration.class);
   /**
    * Default is false, then * can be used as wildcard for allowed methods, headers... And credentials are never used by
    * the server anyway
    */
   private static final boolean ARE_CREDENTIALS_ALLOWED = false;
   /**
    * The maximum validity in seconds of preflight requests
    */
   private static final int PREFLIGHT_MAX_AGE = 1800;
   /**
    * The allowed methods, * is ok
    */
   private static final String[] ALLOWED_METHODS = new String[] {"*"};
   /**
    * The allowed headers, * is ok
    */
   private static final String[] ALLOWED_HEADERS = new String[] {"*"};
   /**
    * The exposed headers, according to CORS Spec * is ok, but it is not yet implemented in the majority of browsers. We
    * use it anyway and the behavior expected when using * is implemented in SPARQL and WAP CORS implementations. So if
    * this gets changed to anything but *, a major rework of these classes might be necessary. At least regarding
    * sparql.
    * 
    * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Expose-Headers">
    *      https://Developer.mozilla.org</a>
    */
   private static final String[] EXPOSED_HEADERS = new String[] {"*"};
   /**
    * The allowed origins
    */
   private final String[] allowedOrigins;

   /**
    * Creates a new cors configuration extracting its base values from the given config
    * 
    * @param allowedOriginsPath
    *                           The path to the allowed origins file
    */
   public CorsConfiguration(String allowedOriginsPath) {
      this.allowedOrigins = readAllowedOriginsFromFile(allowedOriginsPath);
   }

   /**
    * Creates a comma separated list from a given String array. * An empty array generates an empty String
    * 
    * @param  stringArray
    *                     The array to list
    * @return             The comma separated list of the array, null if stringArray is null
    */
   public static String buildCommaSeparatedString(String[] stringArray) {
      if (stringArray == null)
         return null;
      String singleString = "";
      for (int n = 0; n < stringArray.length; n++) {
         String origin = stringArray[n];
         singleString += origin;
         if (n < stringArray.length - 1) {
            singleString += ",";
         }
      }
      return singleString;
   }

   /**
    * Gets the allowed origins configured by the user from the given file
    * 
    * @return the allowed origins, null if none defined
    */
   private static String[] readAllowedOriginsFromFile(String allowedOriginsPath) {
      if (allowedOriginsPath == null) {
         LOGGER.info("No CORS allowed origins file defined, disabling CORS");
         return null;
      }
      File corsFile = new File(allowedOriginsPath);
      if (corsFile.exists()) {
         try {
            List<String> allowedHosts = new Vector<String>();
            BufferedReader reader = new BufferedReader(new FileReader(corsFile));
            String line = reader.readLine();
            while (line != null) {
               line = line.trim();
               if (line.startsWith("#")) {
                  // We ignore lines starting with #
               } else if (line.equals("*")) {
                  LOGGER.info("CORS allowed origins : Found *, accepting all origins"
                        + " (ignoring expiclitely defined ones)");
                  reader.close();
                  return new String[] {"*"};
               } else {
                  allowedHosts.add(line);
               }
               line = reader.readLine();
            }
            reader.close();
            // As the list only contains hostnames / paths, we then add the http:// and https:// protocols.
            // If this is not the intended behavior, return the list as is and do not add both protocols,
            // but update the external file to hole the full URLs then, not only the hostnames / paths
            return appendProtocols(allowedHosts.toArray(new String[allowedHosts.size()]));
         } catch (IOException e) {
            LOGGER.error("Could not read CORS allowed origins file : " + corsFile + " : " + e.getMessage());
            return null;
         }
      } else {
         try {
            FileOutputStream out = new FileOutputStream(corsFile);
            out.write("#default CORS allowed origins file, * means allow all".getBytes());
            out.write(System.lineSeparator().getBytes());
            out.write("#www.example.org ==> allows http://www.example.org and https://www.example.org".getBytes());
            out.write(System.lineSeparator().getBytes());
            out.write("*".getBytes());
            out.write(System.lineSeparator().getBytes());
            out.flush();
            out.close();
            LOGGER.error("CORS allowed origins file does not exist. "
                  + "Created an empty file to get rid of this message: " + corsFile);
            return new String[] {"*"}; // Change this consistently with the default lines above
         } catch (IOException ex) {
            LOGGER.error("CORS allowed origins file does not exist, autocreation not successful. "
                  + "Create at least an empty file to get rid of this message: " + corsFile);
            return null;
         }
      }
   }

   /**
    * Prepends http:// and https:// to a given list of host paths
    * 
    * @param  hostPaths
    *                   The host paths
    * @return           The prepended host paths
    */
   private static String[] appendProtocols(String[] hostPaths) {
      String[] withProtocols = new String[hostPaths.length * 2];
      for (int n = 0; n < hostPaths.length; n++) {
         withProtocols[2 * n] = "http://" + hostPaths[n];
         withProtocols[2 * n + 1] = "https://" + hostPaths[n];
      }
      return withProtocols;
   }

   /**
    * If CORS is enabled, this method shows it
    * 
    * @return true if enabled, false otherwise
    */
   public boolean isCorsEnabled() {
      return allowedOrigins != null && allowedOrigins.length > 0;
   }

   /**
    * Gets the allowed origins configured by the user
    * 
    * @return the allowed origins, null if none defined == CORS disabled
    */
   public String[] getAllowedOrigins() {
      return allowedOrigins;
   }

   /**
    * Is usage of credentials in CORS requests allowed?
    * 
    * @return true if allowed, false otherwise
    */
   public boolean areCredentialsAllowed() {
      return ARE_CREDENTIALS_ALLOWED;
   }

   /**
    * Gets the maximum age of preflight request validity in seconds
    * 
    * @return The maximum validity of preflight requests in seconds
    */
   public int getMaxAgeInSeconds() {
      return PREFLIGHT_MAX_AGE;
   }

   /**
    * Gets the allowed methods for requests
    * 
    * @return The allowed methods
    */
   public String[] getAllowedMethods() {
      return ALLOWED_METHODS;
   }

   /**
    * Gets the allowed headers for request
    * 
    * @return The allowed headers
    */
   public String[] getAllowedHeaders() {
      return ALLOWED_HEADERS;
   }

   /**
    * Gets the exposed headers in responses. According to CORS Spec * is ok, but it is not yet implemented in the
    * majority of browsers.
    * 
    * @see    <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Expose-Headers">
    *         https://Developer.mozilla.org</a>
    * @return The exposed headers
    */
   public String[] getExposedHeaders() {
      return EXPOSED_HEADERS;
   }
}
