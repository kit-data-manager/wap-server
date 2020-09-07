package edu.kit.scc.dem.wapsrv.controller;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalHttpParameterException;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidRequestException;
import edu.kit.scc.dem.wapsrv.exceptions.MethodNotAllowedException;
import static edu.kit.scc.dem.wapsrv.controller.ControllerTestHelper.*;

/**
 * Tests the class CatchAllController
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CatchAllController.class, WapServerConfig.class})
@ActiveProfiles("test")
class CatchAllControllerTest {
   @Autowired
   private WapServerConfig wapServerConfig;
   @Autowired
   private CatchAllController controller;

   private String makeUrl(String rel) {
      if (rel.startsWith("/")) {
         return wapServerConfig.getBaseUrl() + rel;
      } else {
         return wapServerConfig.getBaseUrl() + "/" + rel;
      }
   }

   /**
    * Test catchall request to wap path with post to an annotation url
    */
   @Test
   final void testCatchallRequestWapPathPostAnnotation() {
      checkException(MethodNotAllowedException.class, ErrorMessageRegistry.ANNOTATION_POST_TO_NOT_ALLOWED, () -> {
         controller.catchallRequest(new HttpServletRequestAdapter(
               makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1"), HttpMethod.POST.toString()), null);
      });
   }

   /**
    * Test catchall request to wap path with post to a container url with slug but without container link
    */
   @Test
   final void testCatchallRequestWapPathPostContainerWithSlugAndWithoutLink() {
      checkException(InvalidRequestException.class, ErrorMessageRegistry.CONTAINER_LINK_NEEDED_IN_POST, () -> {
         HttpHeaders headers = new HttpHeaders();
         headers.add("Slug", "container1");
         controller.catchallRequest(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
               HttpMethod.POST.toString()), headers);
      });
   }

   /**
    * Test catchall request to wap path with post to a container url without slug and without container link
    */
   @Test
   final void testCatchallRequestWapPathPostContainerWithoutLinkWithoutSlug() {
      checkException(InvalidRequestException.class, ErrorMessageRegistry.ANNOTATION_POST_INVALID, () -> {
         HttpHeaders headers = new HttpHeaders();
         controller.catchallRequest(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
               HttpMethod.POST.toString()), headers);
      });
   }

   /**
    * Test catchall request to wap path with post to a container url without slug and with container link
    */
   @Test
   final void testCatchallRequestWapPathPostContainerWithLinkWithoutSlug() {
      checkException(InvalidRequestException.class, ErrorMessageRegistry.CONTAINER_POST_INVALID, () -> {
         HttpHeaders headers = new HttpHeaders();
         headers.add(HttpHeaders.LINK, "noMatter");
         controller.catchallRequest(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
               HttpMethod.POST.toString()), headers);
      });
   }

   /**
    * Test catchall request to wap path with post to a container url with params
    */
   @Test
   final void testCatchallRequestWapPathPostContainerWithParams() {
      checkException(IllegalHttpParameterException.class, ErrorMessageRegistry.ALL_NO_PARAMETERS_IN_POST, () -> {
         controller.catchallRequest(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
               HttpMethod.POST.toString(), createParamsMap("page=0&iris=1")), null);
      });
   }

   // #############################################################
   /**
    * Test catchall request to wap path with unimplemented method
    */
   @Test
   final void testCatchallRequestWapPathUnimplementedMethod() {
      checkException(MethodNotAllowedException.class, ErrorMessageRegistry.ALL_UNALLOWED_METHOD, () -> {
         controller.catchallRequest(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT),
               org.eclipse.jetty.http.HttpMethod.PROXY.toString()), null);
      });
   }

   /**
    * Test catchall request to wap path with put to an annotation url
    */
   @Test
   final void testCatchallRequestWapPathPutAnnotation() {
      checkException(InternalServerException.class, ErrorMessageRegistry.PUT_ANNOTATION_SHOULD_NOT_LAND_HERE, () -> {
         controller.catchallRequest(new HttpServletRequestAdapter(
               makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1"), HttpMethod.PUT.toString()), null);
      });
   }

   /**
    * Test catchall request to wap path with put to a container url
    */
   @Test
   final void testCatchallRequestWapPathPutContainer() {
      checkException(MethodNotAllowedException.class, ErrorMessageRegistry.CONTAINER_PUT_NOT_IMPLEMENTED, () -> {
         controller.catchallRequest(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
               HttpMethod.PUT.toString()), null);
      });
   }

   /**
    * Test catchall request to wap path with put to a container url with params
    */
   @Test
   final void testCatchallRequestWapPathPutContainerWithParams() {
      checkException(IllegalHttpParameterException.class, ErrorMessageRegistry.ALL_NO_PARAMETERS_IN_PUT, () -> {
         controller.catchallRequest(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
               HttpMethod.PUT.toString(), createParamsMap("page=0&iris=1")), null);
      });
   }

   /**
    * Test catchall request to unknown path not with get
    */
   @Test
   final void testCatchallRequestPutOutsideKnownPaths() {
      assertThrows(MethodNotAllowedException.class, () -> {
         controller.catchallRequest(
               new HttpServletRequestAdapter(makeUrl("invalidBasePath"), HttpMethod.PUT.toString()), null);
      });
   }

   /**
    * Test catchall request to unkown path with get
    */
   @Test
   final void testCatchallRequestGetOutsideKnownPaths() {
      ResponseEntity<?> response = controller.catchallRequest(
            new HttpServletRequestAdapter(makeUrl("invalidBasePath"), HttpMethod.GET.toString()), null);
      assertNotNull(response);
      assertNotNull(response.getBody());
      String message = response.getBody().toString();
      if (!message.startsWith("<html>") && message.endsWith("</html>")) {
         fail("Not getting information message on get outside known paths");
      }
   }

   /**
    * Test catchall request to javadoc with get
    */
   @Test
   final void testCatchallRequestJavadocGet() {
      checkException(InternalServerException.class,
            ErrorMessageRegistry.JAVADOC_GET_IN_CATCHALL + " : " + makeUrl(JavadocController.PATH), () -> {
               controller.catchallRequest(
                     new HttpServletRequestAdapter(makeUrl(JavadocController.PATH), HttpMethod.GET.toString()), null);
            });
   }

   /**
    * Test catchall request to javadoc not with get
    */
   @Test
   final void testCatchallRequestJavadocPut() {
      checkException(MethodNotAllowedException.class, ErrorMessageRegistry.JAVADOC_ONLY_GET_ALLOWED, () -> {
         controller.catchallRequest(
               new HttpServletRequestAdapter(makeUrl(JavadocController.PATH), HttpMethod.PUT.toString()), null);
      });
   }

   /**
    * Test catchall request to webapp with get
    */
   @Test
   final void testCatchallRequestWebappGet() {
      checkException(InternalServerException.class,
            ErrorMessageRegistry.WEBAPP_GET_IN_CATCHALL + " : " + makeUrl(WebClientController.PATH), () -> {
               controller.catchallRequest(
                     new HttpServletRequestAdapter(makeUrl(WebClientController.PATH), HttpMethod.GET.toString()), null);
            });
   }

   /**
    * Test catchall request to webapp not with get
    */
   @Test
   final void testCatchallRequestWebappPut() {
      checkException(MethodNotAllowedException.class, ErrorMessageRegistry.WEBAPP_ONLY_GET_ALLOWED, () -> {
         controller.catchallRequest(
               new HttpServletRequestAdapter(makeUrl(WebClientController.PATH), HttpMethod.PUT.toString()), null);
      });
   }
}
