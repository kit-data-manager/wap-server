package edu.kit.scc.dem.wapsrv.app;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

/**
 * Tests the class WapServerApplication
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class WapServerApplicationTest {
   private static WapServerApplication app;

   /**
    * The init steps
    */
   @BeforeAll
   public static void init() {
      app = new WapServerApplication();
      // this has to be called to update the state of startupDone
      // Spring would normally call that for us
      app.onApplicationEvent(null);
   }

   /**
    * The shutdown steps
    */
   @AfterAll
   public static void shutdown() {
      // We execute deinit manually to have instance removed at once
      app.deinit(); // Spring would normally call that for us
      app = null;
   }

   /**
    * Test wap server application.
    */
   @Test
   final void testWapServerApplication() {
      assertNotNull(app);
   }

   /**
    * Test block until started.
    */
   @Test
   final void testBlockUntilStarted() {
      // remove instance to test from threshold
      testDeinit();
      long before = WapServerApplication.startUpThreshold;
      WapServerApplication.startUpThreshold = 500;
      assertFalse(WapServerApplication.blockUntilStarted());
      WapServerApplication.startUpThreshold = before; // Reset needed, the value is used by the rest of the tests
      // recreate app object (instance !=null again)
      app = new WapServerApplication();
      // this has to be called to update the state of startupDone
      // Spring would normally call that for us
      app.onApplicationEvent(null);
      // No block until started should work
      assertTrue(WapServerApplication.blockUntilStarted());
   }

   /**
    * Test main.
    */
   @Test
   final void testMain() {
      // Not tested here. Test is executed in
      // edu.kit.scc.dem.wapsrv.testsrest.WapServerApplicationRestTest
   }

   /**
    * Test create config if not existent.
    */
   @Test
   final void testCreateConfigIfNotExistent() {
      // Not tested here. Test is executed in
      // edu.kit.scc.dem.wapsrv.testsrest.WapServerApplicationRestTest
   }

   /**
    * Test get running application context
    */
   @Test
   final void testGetRunningApplicationContext() {
      // We test only the case of no running context. The rest is in
      // edu.kit.scc.dem.wapsrv.testsrest.WapServerApplicationRestTest
      assertNull(WapServerApplication.getRunningApplicationContext());
   }

   /**
    * Test deint.
    */
   final void testDeinit() {
      // this test is executed from testBlockUnitlStarted, not directly thorugh junit
      // therefore do NOT add @Test annotation
      app.setApplicationContext(Mockito.mock(ApplicationContext.class));
      assertNotNull(WapServerApplication.getRunningApplicationContext());
      app.deinit();
      assertNull(WapServerApplication.getRunningApplicationContext()); // instance is null ==> no context
   }

   /**
    * Test set application context
    */
   @Test
   final void testSetApplicationContext() {
      // Not tested here. Test is executed in
      // edu.kit.scc.dem.wapsrv.testsrest.WapServerApplicationRestTest
   }
}
