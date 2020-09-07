package edu.kit.scc.dem.wapsrv.controller;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;

/**
 * Tests the class WebClientController. This test is quite simple, as no own functionality beside
 * {@link #testGetFilenameForRootRequests()} exists.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WapServerConfig.class})
@ActiveProfiles("test")
class WebClientControllerTest {
   /**
    * Test web client controller.
    */
   @Test
   final void testWebClientController() {
      WebClientController actual;
      actual = null;
      actual = new WebClientController(WapServerConfig.getInstance());
      assertNotNull(actual, "Construction did fail.");
   }

   /**
    * Test getFileResponse
    */
   @Test
   final void testGetFileResponse() {
      final WapServerConfig config = WapServerConfig.getInstance();
      final WebClientController actual = new WebClientController(config);
      actual.getFileResponse(new HttpServletRequestAdapter() {
         @Override
         public StringBuffer getRequestURL() {
            return new StringBuffer(config.getBaseUrl() + WebClientController.PATH);
         }
      }, null); // headers not used
   }

   /**
    * Test get filename for root requests.
    */
   @Test
   final void testGetFilenameForRootRequests() {
      WebClientController objWebClientController;
      String actual;
      String expected;
      objWebClientController = new WebClientController(WapServerConfig.getInstance());
      assertNotNull(objWebClientController, "Construction did fail.");
      actual = null;
      actual = objWebClientController.getFilenameForRootRequests();
      assertNotNull(actual, "Could not get filename for root requests.");
      expected = "index.html";
      assertEquals(actual, expected, "Root filename should be: " + expected);
   }
}
