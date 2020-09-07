package edu.kit.scc.dem.wapsrv.app;

import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * This class creates ETag for usage at various points in the server.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Component
public class EtagFactory {
   /**
    * The first character to use for ETags (ASCII Code): a
    */
   private static final int FIRST_ASCII_CODE = 97;
   /**
    * The first character to use for ETags (ASCII Code): z
    */
   private static final int LAST_ASCII_CODE = 122;
   /**
    * The size of the ETags generated
    */
   private static final int ETAG_SIZE = 20;
   /**
    * Used for random ETags
    */
   private final Random random = new Random();

   /**
    * Generates a random ETag value
    * 
    * @return The random ETag
    */
   public String generateEtag() {
      // we use a common random, which is thread safe in general.
      // ===> no need to synchronized this method on concurrent accesses
      StringBuilder etag = new StringBuilder(ETAG_SIZE + 2);
      for (int n = 0; n < ETAG_SIZE; n++) {
         // use small letters
         int i = random.nextInt(LAST_ASCII_CODE - FIRST_ASCII_CODE);
         etag.append((char) (i + FIRST_ASCII_CODE));
      }
      return etag.toString();
   }

   /**
    * Checks if a given ETag has the needed format. This means it is not null and start and ends with a " quote. No
    * surrounding white space is allowed.
    * 
    * @param  etag
    *              The ETag to check
    * @return      true if valid
    */
   public static boolean isValidEtag(String etag) {
      if (etag == null) {
         return false;
      }
      if (etag.length() != etag.trim().length()) {
         // no surrounding white space allowed
         return false;
      }
      if (etag.length() <= 2) {
         // just a " would fit otherwise
         return false;
      }
      if (!etag.startsWith("\"") || !etag.endsWith("\"")) {
         return false;
      }
      // ok, now finally assure not inner " exist
      char[] chars = etag.toCharArray();
      for (int pos = 1; pos < chars.length - 2; pos++) {
         if (chars[pos] == '"') {
            return false;
         }
      }
      return true;
   }
}
