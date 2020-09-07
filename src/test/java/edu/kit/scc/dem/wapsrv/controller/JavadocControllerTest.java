package edu.kit.scc.dem.wapsrv.controller;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;

/**
 * Tests the class JavadocController. This test is quite simple, as no own functionality beside
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
class JavadocControllerTest {
   /**
    * Test javadoc controller.
    */
   @Test
   final void testJavadocController() {
      JavadocController actual;
      actual = null;
      actual = new JavadocController(WapServerConfig.getInstance());
      assertNotNull(actual, "Construction did fail.");
   }

   /**
    * Test getFileResponse
    */
   @Test
   final void testGetFileResponse() {
      WapServerConfig paramWapServerConfig = WapServerConfig.getInstance();
      JavadocController actual = new JavadocController(paramWapServerConfig);
      actual.getFileResponse(new HttpServletRequestAdapter() {
         @Override
         public StringBuffer getRequestURL() {
            return new StringBuffer(paramWapServerConfig.getBaseUrl() + JavadocController.PATH);
         }
      }, null); // headers not used
   }

   /**
    * Test get filename for root requests.
    */
   @Test
   final void testGetFilenameForRootRequests() {
      JavadocController objJavadocController;
      String actual;
      String expected;
      objJavadocController = new JavadocController(WapServerConfig.getInstance());
      assertNotNull(objJavadocController, "Construction did fail.");
      actual = null;
      actual = objJavadocController.getFilenameForRootRequests();
      assertNotNull(actual, "Could not get filename for root requests.");
      expected = "index.html";
      assertEquals(expected, actual, "Root filename should be: " + expected);
   }
}
