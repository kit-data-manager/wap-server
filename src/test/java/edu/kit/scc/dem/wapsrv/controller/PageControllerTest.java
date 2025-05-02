package edu.kit.scc.dem.wapsrv.controller;

import static edu.kit.scc.dem.wapsrv.controller.ControllerTestHelper.checkException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.apache.commons.rdf.api.Dataset;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalHttpParameterException;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalPageIriException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.ContainerPreference;
import edu.kit.scc.dem.wapsrv.model.Page;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdFormatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.TurtleFormatter;
import edu.kit.scc.dem.wapsrv.service.ContainerService;
import edu.kit.scc.dem.wapsrv.service.ContainerServiceMock;
import static edu.kit.scc.dem.wapsrv.controller.ControllerTestHelper.*;

/**
 * Tests the class PageController
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {PageController.class, WapServerConfig.class, JsonLdProfileRegistry.class,
      FormatRegistry.class, JsonLdFormatter.class, ContainerServiceMock.class, TurtleFormatter.class})
@ExtendWith(HoverflyExtension.class)
@HoverflySimulate(source = @HoverflySimulate.Source(value = "w3c_simulation.json", type = HoverflySimulate.SourceType.DEFAULT_PATH))
@ActiveProfiles("test")
class PageControllerTest extends BasicWapControllerTest {
   @Autowired
   private PageController controller;
   @Autowired
   private WapServerConfig wapServerConfig;
   @Autowired
   private ContainerService containerServiceMock;

   /**
    * Test is valid service format.
    */
   @Test
   final void testIsValidServiceFormat() {
      assertFalse(controller.isValidServiceFormat(null));
   }

   /**
    * Test get page.
    */
   @Test
   final void testGetPage() {
      testGetHeadOptionsPage(HttpMethod.GET);
   }

   /**
    * Test head page.
    */
   @Test
   final void testHeadPage() {
      testGetHeadOptionsPage(HttpMethod.HEAD);
   }

   /**
    * Test options page.
    */
   @Test
   final void testOptionsPage() {
      testGetHeadOptionsPage(HttpMethod.OPTIONS);
   }

   private void testGetHeadOptionsPage(final HttpMethod method) {
      // Test too many params
      testTooManyParams(method.toString());
      // Test invalid params
      testInvalidParams(method.toString());
      // test valid page
      testValidPage(method.toString());
   }

   private void testValidPage(final String method) {
      ResponseEntity<?> response = null;
      final String url = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/");
      final int iris = ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS;
      final int pageNr = 0;
      when(containerServiceMock.getPage(url, iris, pageNr)).thenReturn(createPage());
      switch (method) {
      case "GET":
         response = controller.getPage(
               new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                     HttpMethod.GET.toString(), createParamsMap(null), TurtleFormatter.TURTLE_STRING),
               null, iris, pageNr);
         break;
      case "HEAD":
         response = controller.headPage(
               new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                     HttpMethod.HEAD.toString(), createParamsMap(null), TurtleFormatter.TURTLE_STRING),
               null, iris, pageNr);
         break;
      case "OPTIONS":
         response = controller.optionsPage(
               new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                     HttpMethod.OPTIONS.toString(), createParamsMap(null), TurtleFormatter.TURTLE_STRING),
               null, iris, pageNr);
         break;
      default:
         fail("Unimplemented test method : " + method);
      }
      // check common headers
      checkAllowHeader(response, org.springframework.http.HttpMethod.GET, org.springframework.http.HttpMethod.HEAD,
            org.springframework.http.HttpMethod.OPTIONS);
      checkVaryHeader(response, "Accept");
      assertEquals(PageConstants.GET_PAGE_SUCCESS_CODE, response.getStatusCode().value());
      switch (method) {
      case "GET":
         if (!response.getHeaders().get("Content-Type").get(0).startsWith(TurtleFormatter.TURTLE_STRING)) {
            fail("Unexpected format of answer : " + response.getHeaders().get("Content-Type").get(0));
         }
         assertEquals("pageBody", response.getBody());
         break;
      case "HEAD":
         if (!response.getHeaders().get("Content-Type").get(0).startsWith(TurtleFormatter.TURTLE_STRING)) {
            fail("Unexpected format of answer : " + response.getHeaders().get("Content-Type").get(0));
         }
         assertNotNull(response.getBody());
         // The head request should not have a body. But our response needs one so spring / jetty
         // can determine the correct content length. They remove it from the final answer
         break;
      case "OPTIONS":
         assertNull(response.getHeaders().getContentType());
         assertNull(response.getBody());
         break;
      default:
         fail("Unimplemented test method : " + method);
      }
   }

   private Page createPage() {
      // we do not need an actual page here. this would be part of the repository test
      // or any other test. All we need is a page that can be formatted by the formatter
      return new Page() {
         @Override
         public String toString(Format format) {
            return "pageBody";
         }

         @Override
         public Type getType() {
            return null;
         }

         @Override
         public String getIri() {
            return null;
         }

         @Override
         public int getPageNr() {
            return 0;
         }

         @Override
         public String getContainerIri() {
            return null;
         }

         @Override
         public int getContainerPreference() {
            return 0;
         }

         @Override
         public String getNextPage() {
            return null;
         }

         @Override
         public String getPreviousPage() {
            return null;
         }

         @Override
         public int getFirstAnnotationPosition() {
            return 0;
         }

         @Override
         public void addAnnotation(Annotation anno) {
         }

         @Override
         public void addAnnotationIri(String annoIri) {
         }

         @Override
         public void closeAdding() {
         }

         @Override
         public Dataset getDataset() {
            return null;
         }
      };
   }

   private void testInvalidParams(final String method) {
      // test invalid iris
      checkException(IllegalPageIriException.class, IllegalPageIriException.ERROR_MESSAGE, () -> {
         switch (method) {
         case "GET":
            controller.getPage(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                  HttpMethod.GET.toString(), createParamsMap(null)), null, -1, 0);
            break;
         case "HEAD":
            controller.headPage(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                  HttpMethod.HEAD.toString(), createParamsMap(null)), null, -1, 0);
            break;
         case "OPTIONS":
            controller.optionsPage(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                  HttpMethod.OPTIONS.toString(), createParamsMap(null)), null, -1, 0);
            break;
         default:
            break; // throws no exception ==> fail
         }
      });
      // test invalid pageNr
      checkException(IllegalPageIriException.class, IllegalPageIriException.ERROR_MESSAGE, () -> {
         switch (method) {
         case "GET":
            controller.getPage(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                  HttpMethod.GET.toString(), createParamsMap(null)), null, 0, -1);
            break;
         case "HEAD":
            controller.headPage(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                  HttpMethod.HEAD.toString(), createParamsMap(null)), null, 0, -1);
            break;
         case "OPTIONS":
            controller.optionsPage(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                  HttpMethod.OPTIONS.toString(), createParamsMap(null)), null, 0, -1);
            break;
         default:
            break; // throws no exception ==> fail
         }
      });
   }

   private void testTooManyParams(final String method) {
      checkException(IllegalHttpParameterException.class, ErrorMessageRegistry.PAGE_INVALID_GIVEN_PARAMETERS, () -> {
         switch (method) {
         case "GET":
            controller.getPage(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                  HttpMethod.GET.toString(), createParamsMap("iris=0&page=0&third=true")), null, 0, 0);
            break;
         case "HEAD":
            controller.headPage(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                  HttpMethod.HEAD.toString(), createParamsMap("iris=0&page=0&third=true")), null, 0, 0);
            break;
         case "OPTIONS":
            controller.optionsPage(new HttpServletRequestAdapter(makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/"),
                  HttpMethod.OPTIONS.toString(), createParamsMap("iris=0&page=0&third=true")), null, 0, 0);
            break;
         default:
            break; // throws no exception ==> fail
         }
      });
   }

   @Override
   protected WapServerConfig getWapServerConfig() {
      return wapServerConfig;
   }
}
