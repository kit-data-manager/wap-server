package edu.kit.scc.dem.wapsrv.app;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.net.URL;
import org.junit.jupiter.api.Test;
import edu.kit.scc.dem.wapsrv.installer.JarUtilities;

/**
 * Tests the class JarUtilities
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class JarUtilitiesTest {
   /**
    * Test get currently running jar url.
    */
   @Test
   final void testGetCurrentlyRunningJarUrl() {
      URL actual;
      actual = null;
      actual = JarUtilities.getCurrentlyRunningJarUrl();
      assertNotNull(actual, "Could not get Jar URL.");
   }

   /**
    * Test get currently running jar file.
    */
   @Test
   final void testGetCurrentlyRunningJarFile() {
      File actual;
      actual = null;
      actual = JarUtilities.getCurrentlyRunningJarFile();
      assertNotNull(actual, "Could not get Jar File.");
   }

   /**
    * Test extract folder.
    */
   @Test
   final void testExtractFolder() {
      // We do not test installer (jarUtilities is part of that)
      // if someone wants to implement it, provide a valid environment
      // make a temp folder. place a jar there, extract something from it
   }
}
