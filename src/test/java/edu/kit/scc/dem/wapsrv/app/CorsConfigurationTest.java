package edu.kit.scc.dem.wapsrv.app;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * Tests the Cors Configuration
 *
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class CorsConfigurationTest {
   /**
    * Test auto creation
    */
   @Test
   final void testAutocreation() {
      File sslTemp = null;
      try {
         sslTemp = File.createTempFile("cors_allowed_origins", ".conf");
         assertTrue(sslTemp.delete());
      } catch (IOException e) {
         fail(e.getMessage());
      }
      CorsConfiguration config = new CorsConfiguration(sslTemp.getAbsolutePath());
      assertNotNull(config);
   }

   /**
    * Test buildCommaSeparatedString
    */
   @Test
   final void testBuildCommaSeparatedString() {
      assertNull(CorsConfiguration.buildCommaSeparatedString(null));
   }
}
