package edu.kit.scc.dem.wapsrv.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.ConfigurationKeys;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.app.EtagFactory;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.FormatNotAvailableException;
import edu.kit.scc.dem.wapsrv.exceptions.HttpHeaderException;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalHttpParameterException;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidContainerException;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidRequestException;
import edu.kit.scc.dem.wapsrv.exceptions.MethodNotAllowedException;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.ContainerPreference;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdFormatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.TurtleFormatter;
import static edu.kit.scc.dem.wapsrv.controller.ControllerTestHelper.*;
import edu.kit.scc.dem.wapsrv.service.ContainerService;
import edu.kit.scc.dem.wapsrv.service.ContainerServiceMock;

/**
 * Tests the class ContainerController
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
      classes = {ContainerController.class, WapServerConfig.class, JsonLdProfileRegistry.class, FormatRegistry.class,
            JsonLdFormatter.class, ContainerServiceMock.class, TurtleFormatter.class, EtagFactory.class})
@ExtendWith(HoverflyExtension.class)
@HoverflySimulate(source = @HoverflySimulate.Source(value = "w3c_simulation.json", type = HoverflySimulate.SourceType.DEFAULT_PATH))
@ActiveProfiles("test")
class ContainerControllerTest extends BasicWapControllerTest {
   @Autowired
   private ContainerController controller;
   @Autowired
   private WapServerConfig wapServerConfig;
   @Autowired
   private ContainerService service;
   @Autowired
   private EtagFactory etagFactory;

   /**
    * Called after each test, because some manipulate the config and if fail, do not reset it
    */
   @AfterEach
   protected void afterEach() {
      Properties props = WapServerConfig.getDefaultProperties();
      wapServerConfig.updateConfig(props);
   }

   /**
    * Test is valid service format.
    */
   @Test
   final void testIsValidServiceFormat() {
      // this relies completely on the service and is tested there
   }

   /**
    * Test get container.
    */
   @Test
   final void testGetContainer() {
      testGetHeadOptionsContainer(HttpMethod.GET.toString(), false);
   }

   /**
    * Test head container.
    */
   @Test
   final void testHeadContainer() {
      testGetHeadOptionsContainer(HttpMethod.HEAD.toString(), false);
   }

   /**
    * Test options container.
    */
   @Test
   final void testOptionsContainer() {
      testGetHeadOptionsContainer(HttpMethod.OPTIONS.toString(), false);
   }

   /**
    * Test get root container.
    */
   @Test
   final void testGetRootContainer() {
      testGetHeadOptionsContainer(HttpMethod.GET.toString(), true);
   }

   /**
    * Test head root container.
    */
   @Test
   final void testHeadRootContainer() {
      testGetHeadOptionsContainer(HttpMethod.HEAD.toString(), true);
   }

   /**
    * Test options root container.
    */
   @Test
   final void testOptionsRootContainer() {
      testGetHeadOptionsContainer(HttpMethod.OPTIONS.toString(), true);
   }

   /**
    * Test prefer overriding invalid exception
    */
   @Test
   final void testPreferenceViaParamsInvalid() {
      // this test is only performed once, thats enough because all execute the same code
      // and we only check this one answer parameter, the rest is done elsewhere
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/");
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.ACCEPT, TurtleFormatter.TURTLE_STRING);
      checkException(IllegalHttpParameterException.class,
            ErrorMessageRegistry.CONTAINER_GET_HEAD_OPTIONS_NO_PARAMETERS_ALLOWED_BUT_IRIS, () -> {
               controller.getContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap("iris=5")),
                     httpHeaders);
            });
   }

   /**
    * Test prefer overriding number format
    */
   @Test
   final void testPreferenceViaParamsNotNumnber() {
      // this test is only performed once, thats enough because all execute the same code
      // and we only check this one answer parameter, the rest is done elsewhere
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/");
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.ACCEPT, TurtleFormatter.TURTLE_STRING);
      checkException(IllegalHttpParameterException.class,
            ErrorMessageRegistry.CONTAINER_GET_HEAD_OPTIONS_NO_PARAMETERS_ALLOWED_BUT_IRIS, () -> {
               controller.getContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap("iris=abc")),
                     httpHeaders);
            });
   }

   /**
    * Test prefer overriding
    */
   @Test
   final void testPreferenceOverriding() {
      // this test is only performed once, thats enough because all execute the same code
      // and we only check this one answer parameter, the rest is done elsewhere
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/");
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.ACCEPT, TurtleFormatter.TURTLE_STRING);
      // we cannot access the value of preferences in getContainer(..) directly
      // therefore we use mockito to show this to us indirectly via getting the fitting
      // container object
      Mockito.reset(service);
      // accept turtle as input format
      when(service.isValidInputFormat(Format.TURTLE)).thenReturn(true);
      // Add a container with preference embed
      Set<Integer> preferDesc = new HashSet<Integer>();
      preferDesc.add(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS);
      when(service.getContainer(Mockito.any(), Mockito.eq(preferDesc)))
            .thenReturn(createContainer(iri + "_" + ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS));
      // Add a container with preference iris only
      Set<Integer> preferIris = new HashSet<Integer>();
      preferIris.add(ContainerPreference.PREFER_CONTAINED_IRIS);
      when(service.getContainer(Mockito.any(), Mockito.eq(preferIris)))
            .thenReturn(createContainer(iri + "_" + ContainerPreference.PREFER_CONTAINED_IRIS));
      // Request contained desc via prefer and check correct returned container
      String prefer
            = "return=representation;include=\"" + ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS_STRING + "\"";
      httpHeaders.add("Prefer", prefer);
      ResponseEntity<?> response = controller.getContainer(
            new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap(null)), httpHeaders);
      assertNotNull(response);
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode().value());
      assertEquals(iri + "_" + ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS,
            response.getHeaders().getFirst("Content-Location"));
      // Request contained desc via prefer (still) but now override via params and check correct returned container
      response = controller.getContainer(new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(),
            createParamsMap("iris=" + ContainerPreference.PREFER_CONTAINED_IRIS)), httpHeaders);
      assertNotNull(response);
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode().value());
      assertEquals(iri + "_" + ContainerPreference.PREFER_CONTAINED_IRIS,
            response.getHeaders().getFirst("Content-Location"));
      // and now vice versa
      // Request contained desc via prefer and check correct returned container
      httpHeaders.remove("Prefer");
      prefer = "return=representation;include=\"" + ContainerPreference.PREFER_CONTAINED_IRIS_STRING + "\"";
      httpHeaders.add("Prefer", prefer);
      response = controller.getContainer(
            new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap(null)), httpHeaders);
      assertNotNull(response);
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode().value());
      assertEquals(iri + "_" + ContainerPreference.PREFER_CONTAINED_IRIS,
            response.getHeaders().getFirst("Content-Location"));
      // Request contained desc via prefer (still) but now override via params and check correct returned container
      response = controller.getContainer(new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(),
            createParamsMap("iris=" + ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS)), httpHeaders);
      assertNotNull(response);
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode().value());
      assertEquals(iri + "_" + ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS,
            response.getHeaders().getFirst("Content-Location"));
   }

   // ###########################################
   // ###########################################
   private void testGetHeadOptionsContainer(final String method, final boolean toRootContainer) {
      // We do not want to have too many test, we therefore execute all relevant ones in one call
      testGetHeadOptionsContainerInvalidPreferences(method, toRootContainer);
      testGetHeadOptionsContainerInvalidPreferenceCombination(method, toRootContainer);
      testGetHeadOptionsContainerWithPageParam(method, toRootContainer);
      testGetHeadOptionsContainerWithUnkownParam(method, toRootContainer);
      testValidGetHeadOptionsContainer(method, toRootContainer);
   }

   private void testGetHeadOptionsContainerWithUnkownParam(final String method, final boolean toRootContainer) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final HttpHeaders httpHeaders = new HttpHeaders();
      checkException(IllegalHttpParameterException.class,
            ErrorMessageRegistry.CONTAINER_GET_HEAD_OPTIONS_NO_PARAMETERS_ALLOWED_BUT_IRIS, () -> {
               if (toRootContainer) {
                  switch (method) {
                  case "GET":
                     controller.getRootContainer(
                           new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap("inv=0")),
                           httpHeaders);
                     break;
                  case "HEAD":
                     controller.headRootContainer(
                           new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(), createParamsMap("inv=0")),
                           httpHeaders);
                     break;
                  case "OPTIONS":
                     controller.optionsRootContainer(
                           new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(), createParamsMap("inv=0")),
                           httpHeaders);
                     break;
                  default:
                     fail("Unimplemented test method : " + method);
                  }
               } else {
                  switch (method) {
                  case "GET":
                     controller.getContainer(
                           new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap("inv=0")),
                           httpHeaders);
                     break;
                  case "HEAD":
                     controller.headContainer(
                           new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(), createParamsMap("inv=0")),
                           httpHeaders);
                     break;
                  case "OPTIONS":
                     controller.optionsContainer(
                           new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(), createParamsMap("inv=0")),
                           httpHeaders);
                     break;
                  default:
                     fail("Unimplemented test method : " + method);
                  }
               }
            });
   }

   private void testGetHeadOptionsContainerWithPageParam(final String method, final boolean toRootContainer) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final HttpHeaders httpHeaders = new HttpHeaders();
      checkException(InvalidRequestException.class, ErrorMessageRegistry.PAGE_WITH_PAGE_BUT_IRIS_MISSING, () -> {
         if (toRootContainer) {
            switch (method) {
            case "GET":
               controller.getRootContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap("page=0")),
                     httpHeaders);
               break;
            case "HEAD":
               controller.headRootContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(), createParamsMap("page=0")),
                     httpHeaders);
               break;
            case "OPTIONS":
               controller.optionsRootContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(), createParamsMap("page=0")),
                     httpHeaders);
               break;
            default:
               fail("Unimplemented test method : " + method);
            }
         } else {
            switch (method) {
            case "GET":
               controller.getContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap("page=0")),
                     httpHeaders);
               break;
            case "HEAD":
               controller.headContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(), createParamsMap("page=0")),
                     httpHeaders);
               break;
            case "OPTIONS":
               controller.optionsContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(), createParamsMap("page=0")),
                     httpHeaders);
               break;
            default:
               fail("Unimplemented test method : " + method);
            }
         }
      });
   }

   private void testValidGetHeadOptionsContainer(String method, boolean toRootContainer) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.ACCEPT, TurtleFormatter.TURTLE_STRING);
      // to get 100% coverage, we test all combos here
      // The value is not used, it just has to be valid
      String prefer = null;
      switch (method) {
      case "GET":
         prefer = "return=representation;include=\"" + ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS_STRING + "\"";
         break;
      case "HEAD":
         prefer = "return=representation;include=\"" + ContainerPreference.PREFER_CONTAINED_IRIS_STRING + "\"";
         break;
      default:
         prefer = null;
         break;
      }
      if (prefer != null) {
         httpHeaders.add("Prefer", prefer);
      }
      // accept turtle as input format
      when(service.isValidInputFormat(Format.TURTLE)).thenReturn(true);
      // Add a container
      when(service.getContainer(Mockito.any(), Mockito.any())).thenReturn(createContainer(iri));
      ResponseEntity<?> response = null;
      if (toRootContainer) {
         switch (method) {
         case "GET":
            response = controller.getRootContainer(
                  new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap(null)), httpHeaders);
            break;
         case "HEAD":
            response = controller.headRootContainer(
                  new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(), createParamsMap(null)), httpHeaders);
            break;
         case "OPTIONS":
            response = controller.optionsRootContainer(
                  new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(), createParamsMap(null)),
                  httpHeaders);
            break;
         default:
            fail("Unimplemented test method : " + method);
         }
      } else {
         switch (method) {
         case "GET":
            response = controller.getContainer(
                  new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap(null)), httpHeaders);
            break;
         case "HEAD":
            response = controller.headContainer(
                  new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(), createParamsMap(null)), httpHeaders);
            break;
         case "OPTIONS":
            response = controller.optionsContainer(
                  new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(), createParamsMap(null)),
                  httpHeaders);
            break;
         default:
            fail("Unimplemented test method : " + method);
         }
      }
      // check common headers
      assertNotNull(response);
      if (toRootContainer) {
         checkAllowHeader(response, org.springframework.http.HttpMethod.GET, org.springframework.http.HttpMethod.HEAD,
               org.springframework.http.HttpMethod.OPTIONS, org.springframework.http.HttpMethod.POST);
      } else {
         checkAllowHeader(response, org.springframework.http.HttpMethod.GET, org.springframework.http.HttpMethod.HEAD,
               org.springframework.http.HttpMethod.OPTIONS, org.springframework.http.HttpMethod.POST,
               org.springframework.http.HttpMethod.DELETE);
      }
      checkVaryHeader(response, "Accept");
      assertNotNull(response.getHeaders().getFirst("Content-Location"));
      checkLinkHeader(response, new String[] {"<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"",
            "<http://www.w3.org/TR/annotation-protocol/>; rel=\"http://www.w3.org/ns/ldp#constrainedBy\""});
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode().value());
      switch (method) {
      case "GET":
         assertNotNull(response.getHeaders().getETag());
         if (!response.getHeaders().get("Content-Type").get(0).startsWith(TurtleFormatter.TURTLE_STRING)) {
            fail("Unexpected format of answer : " + response.getHeaders().get("Content-Type").get(0));
         }
         assertEquals("containerBody", response.getBody());
         assertNotNull(response.getHeaders().getETag());
         break;
      case "HEAD":
         assertNotNull(response.getHeaders().getETag());
         if (!response.getHeaders().get("Content-Type").get(0).startsWith(TurtleFormatter.TURTLE_STRING)) {
            fail("Unexpected format of answer : " + response.getHeaders().get("Content-Type").get(0));
         }
         assertNotNull(response.getBody());
         // The head request should not have a body. But our response needs one so spring / jetty
         // can determine the correct content length. They remove it from the final answer
         assertNotNull(response.getHeaders().getETag());
         break;
      case "OPTIONS":
         assertNull(response.getHeaders().getContentType());
         assertNull(response.getBody());
         break;
      default:
         fail("Unimplemented test method : " + method);
      }
   }

   /**
    * Test get head options container invalid preferences.
    *
    * @param method
    *                        the method
    * @param toRootContainer
    *                        the to root container
    */
   private void testGetHeadOptionsContainerInvalidPreferences(final String method, final boolean toRootContainer) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, TurtleFormatter.TURTLE_STRING);
      httpHeaders.add(HttpHeaders.LINK, "<http://www.w3.org/ns/ldp#BasicContainer>;  rel=\"type\"");
      httpHeaders.add("Prefer", "invalidPref");
      checkException(HttpHeaderException.class, ErrorMessageRegistry.CONTAINER_INVALID_PREFERENCES, () -> {
         if (toRootContainer) {
            switch (method) {
            case "GET":
               controller.getRootContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap(null)), httpHeaders);
               break;
            case "HEAD":
               controller.headRootContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(), createParamsMap(null)),
                     httpHeaders);
               break;
            case "OPTIONS":
               controller.optionsRootContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(), createParamsMap(null)),
                     httpHeaders);
               break;
            default:
               fail("Unimplemented test method : " + method);
            }
         } else {
            switch (method) {
            case "GET":
               controller.getContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap(null)), httpHeaders);
               break;
            case "HEAD":
               controller.headContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(), createParamsMap(null)),
                     httpHeaders);
               break;
            case "OPTIONS":
               controller.optionsContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(), createParamsMap(null)),
                     httpHeaders);
               break;
            default:
               fail("Unimplemented test method : " + method);
            }
         }
      });
   }

   private void testGetHeadOptionsContainerInvalidPreferenceCombination(final String method,
         final boolean toRootContainer) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, TurtleFormatter.TURTLE_STRING);
      httpHeaders.add(HttpHeaders.LINK, "<http://www.w3.org/ns/ldp#BasicContainer>;  rel=\"type\"");
      String invalidCombo
            = "return=representation;include=\"" + ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS_STRING + " "
                  + ContainerPreference.PREFER_CONTAINED_IRIS_STRING + "\"";
      httpHeaders.add("Prefer", invalidCombo);
      checkException(HttpHeaderException.class, ErrorMessageRegistry.CONTAINER_UNALLOWED_PREFERENCE_COMBINATION, () -> {
         if (toRootContainer) {
            switch (method) {
            case "GET":
               controller.getRootContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap(null)), httpHeaders);
               break;
            case "HEAD":
               controller.headRootContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(), createParamsMap(null)),
                     httpHeaders);
               break;
            case "OPTIONS":
               controller.optionsRootContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(), createParamsMap(null)),
                     httpHeaders);
               break;
            default:
               fail("Unimplemented test method : " + method);
            }
         } else {
            switch (method) {
            case "GET":
               controller.getContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(), createParamsMap(null)), httpHeaders);
               break;
            case "HEAD":
               controller.headContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(), createParamsMap(null)),
                     httpHeaders);
               break;
            case "OPTIONS":
               controller.optionsContainer(
                     new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(), createParamsMap(null)),
                     httpHeaders);
               break;
            default:
               fail("Unimplemented test method : " + method);
            }
         }
      });
   }

   /**
    * Test post container.
    */
   @Test
   final void testPostContainer() {
      testPostContainer(false, null);
   }

   /**
    * Test post root container.
    */
   @Test
   final void testPostRootContainer() {
      testPostContainer(true, null);
   }

   /**
    * Test post container WithSlug
    */
   @Test
   final void testPostContainerWithSlug() {
      testPostContainer(false, "container");
   }

   /**
    * Test post root container WithSlug
    */
   @Test
   final void testPostRootContainerWithSlug() {
      testPostContainer(true, "container");
   }

   /**
    * Test post container Without Slug but mandatory
    */
   @Test
   final void testPostContainerWithoutMandatorySlug() {
      Properties props = WapServerConfig.getDefaultProperties();
      props.setProperty(ConfigurationKeys.EnableMandatorySlugInContainerPost.toString(), "true");
      // will be reset in afterEach
      wapServerConfig.updateConfig(props);
      testPostContainerWithoutMandatorySlug(false);
   }

   /**
    * Test post root container without mandatory slug.
    */
   @Test
   final void testPostRootContainerWithoutMandatorySlug() {
      Properties props = WapServerConfig.getDefaultProperties();
      props.setProperty(ConfigurationKeys.EnableMandatorySlugInContainerPost.toString(), "true");
      // will be reset in afterEach
      wapServerConfig.updateConfig(props);
      testPostContainerWithoutMandatorySlug(true);
   }

   /**
    * Test post container WithoutContentType
    */
   @Test
   final void testPostContainerWithoutContentType() {
      testPostContainerWithoutContentType(false);
   }

   /**
    * Test post root container WithoutContentType
    */
   @Test
   final void testPostRootContainerWithoutContentType() {
      testPostContainerWithoutContentType(true);
   }

   /**
    * Test post container WithParams
    */
   @Test
   final void testPostContainerWithParams() {
      testPostContainerWithParams(false);
   }

   /**
    * Test post root container WithParams
    */
   @Test
   final void testPostRootContainerWithParams() {
      testPostContainerWithParams(true);
   }

   /**
    * Test post container InvalidInternalIri
    */
   @Test
   final void testPostContainerInvalidInternalIri() {
      testPostContainerInvalidInternalIri(false);
   }

   /**
    * Test post root container InvalidInternalIri
    */
   @Test
   final void testPostRootContainerInvalidInternalIri() {
      testPostContainerInvalidInternalIri(true);
   }

   /**
    * Test post container with empty name
    */
   @Test
   final void testPostContainerWithEmptyName() {
      testPostContainerWithEmptyName(false);
   }

   /**
    * Test post root container with emptyname
    */
   @Test
   final void testPostRootContainerWithEmptyName() {
      testPostContainerWithEmptyName(true);
   }

   /**
    * Test post container NotBasicContainer
    */
   @Test
   final void testPostContainerNotBasicContainer() {
      testPostContainerNotBasicContainer(false);
   }

   /**
    * Test post root container NotBasicContainer
    */
   @Test
   final void testPostRootContainerNotBasicContainer() {
      testPostContainerNotBasicContainer(true);
   }

   /**
    * Test post root container WithSlug but without link
    */
   @Test
   final void testPostRootContainerWithoutLinkWithSlug() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT);
      final String body = null; // Body does not matter
      final String name = "container";
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, TurtleFormatter.TURTLE_STRING);
      httpHeaders.add("Slug", name);
      checkException(InvalidRequestException.class, ErrorMessageRegistry.CONTAINER_LINK_NEEDED_IN_POST, () -> {
         controller.postRootContainer(
               new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(), createParamsMap(null)), body,
               httpHeaders);
      });
   }

   /**
    * Test post root container without link and slug
    */
   @Test
   final void testPostRootContainerWithoutLinkWithoutSlug() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT);
      final String body = null; // Body does not matter
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, TurtleFormatter.TURTLE_STRING);
      checkException(MethodNotAllowedException.class, ErrorMessageRegistry.ANNOTATION_NO_POST_TO_ROOT_CONTAINER, () -> {
         controller.postRootContainer(
               new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(), createParamsMap(null)), body,
               httpHeaders);
      });
   }

   private void testPostContainerWithoutMandatorySlug(final boolean toRootContainer) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final String body = "body"; // Body does not matter
      // accept turtle as input format
      when(service.isValidInputFormat(Format.TURTLE)).thenReturn(true);
      // Add a container
      when(service.postContainer(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(createContainer(iri));
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, TurtleFormatter.TURTLE_STRING);
      httpHeaders.add(HttpHeaders.LINK, "<http://www.w3.org/ns/ldp#BasicContainer>;  rel=\"type\"");
      checkException(InvalidRequestException.class, ErrorMessageRegistry.CONTAINER_SLUG_IS_SET_MANDATORY, () -> {
         if (toRootContainer) {
            controller.postRootContainer(
                  new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(), createParamsMap(null)), body,
                  httpHeaders);
         } else {
            controller.postContainer(
                  new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(), createParamsMap(null)), body,
                  httpHeaders);
         }
      });
   }

   private void testPostContainer(final boolean toRootContainer, final String name) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final String body = "body"; // Body does not matter
      // accept turtle as input format
      when(service.isValidInputFormat(Format.TURTLE)).thenReturn(true);
      // Add a container
      when(service.postContainer(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(createContainer(iri));
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, TurtleFormatter.TURTLE_STRING);
      if (name != null) {
         httpHeaders.add("Slug", name);
      }
      httpHeaders.add(HttpHeaders.LINK, "<http://www.w3.org/ns/ldp#BasicContainer>;  rel=\"type\"");
      ResponseEntity<?> response = null;
      if (toRootContainer) {
         response = controller.postRootContainer(
               new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(), createParamsMap(null)), body,
               httpHeaders);
      } else {
         response = controller.postContainer(
               new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(), createParamsMap(null)), body,
               httpHeaders);
      }
      assertNotNull(response);
      if (toRootContainer) {
         checkAllowHeader(response, org.springframework.http.HttpMethod.GET, org.springframework.http.HttpMethod.HEAD,
               org.springframework.http.HttpMethod.OPTIONS, org.springframework.http.HttpMethod.POST);
      } else {
         checkAllowHeader(response, org.springframework.http.HttpMethod.GET, org.springframework.http.HttpMethod.HEAD,
               org.springframework.http.HttpMethod.OPTIONS, org.springframework.http.HttpMethod.POST,
               org.springframework.http.HttpMethod.DELETE);
      }
      checkVaryHeader(response, "Accept");
      assertNotNull(response.getHeaders().getETag());
      assertNotNull(response.getHeaders().getLocation());
      checkLinkHeader(response, new String[] {"<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"",
            "<http://www.w3.org/TR/annotation-protocol/>; rel=\"http://www.w3.org/ns/ldp#constrainedBy\""});
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode().value());
   }

   private void testPostContainerInvalidInternalIri(final boolean toRootContainer) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final String body = null; // Body does not matter
      final String name = "container";
      // accept turtle as input format
      when(service.isValidInputFormat(Format.TURTLE)).thenReturn(true);
      // Add an annotation
      when(service.postContainer(iri, name, body, Format.TURTLE)).thenReturn(createContainer("#####"));
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, TurtleFormatter.TURTLE_STRING);
      httpHeaders.add(HttpHeaders.LINK, "<http://www.w3.org/ns/ldp#BasicContainer>;  rel=\"type\"");
      httpHeaders.add("Slug", name);
      checkException(InternalServerException.class, ErrorMessageRegistry.INTERNAL_IRI_NOT_A_URI + "#####", () -> {
         if (toRootContainer) {
            controller.postRootContainer(
                  new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(), createParamsMap(null)), body,
                  httpHeaders);
         } else {
            controller.postContainer(
                  new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(), createParamsMap(null)), body,
                  httpHeaders);
         }
      });
   }

   private Container createContainer(final String iri) {
      // we do not need an actual container here. this would be part of the repository test
      // or any other test. All we need is a container that can be formatted by the formatter
      return new Container() {
         @Override
         public Dataset getDataset() {
            return null;
         }

         @Override
         public BlankNodeOrIRI getIri() {
            return null;
         }

         @Override
         public void setIri(BlankNodeOrIRI iri) {
         }

         @Override
         public void setIri(BlankNodeOrIRI iri, boolean copyVia) {
         }

         @Override
         public void setIri(String iri, boolean copyVia) {
         }

         @Override
         public void setIri(String iri) {
         }

         @Override
         public String getEtagQuoted() {
            return "\"" + etagFactory.generateEtag() + "\""; // we need a valid one, but do not care about its value
         }

         @Override
         public String getIriString() {
            if (iri != null)
               return iri;
            else
               return "http://www.example.org/container1/";
            // we need a valid one, but do not care about its value
         }

         @Override
         public String getEtag() {
            return null;
         }

         @Override
         public void setEtag(String etag) {
         }

         @Override
         public void setCreated() {
         }

         @Override
         public String getValue(IRI propertyName) {
            return null;
         }

         @Override
         public List<String> getValues(IRI propertyName) {
            return null;
         }

         @Override
         public boolean isDeleted() {
            return false;
         }

         @Override
         public String toString(Format format) throws FormatNotAvailableException {
            return "containerBody";
         }

         @Override
         public Type getType() {
            return null;
         }

         @Override
         public String getLabel() {
            return null;
         }

         @Override
         public void createDefaultLabel() {
         }

         @Override
         public boolean isMinimalContainer() {
            return false;
         }
      };
   }

   private void testPostContainerWithParams(final boolean toRootContainer) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final String body = null; // Body does not matter
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, JsonLdFormatter.JSON_LD_STRING);
      httpHeaders.add(HttpHeaders.LINK, "<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"");
      checkException(IllegalHttpParameterException.class,
            ErrorMessageRegistry.CONTAINER_POST_DELETE_NO_PARAMETERS_ALLOWED, () -> {
               if (toRootContainer) {
                  controller.postRootContainer(new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(),
                        createParamsMap("iris=0"), TurtleFormatter.TURTLE_STRING), body, httpHeaders);
               } else {
                  controller.postContainer(new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(),
                        createParamsMap("iris=0"), TurtleFormatter.TURTLE_STRING), body, httpHeaders);
               }
            });
   }

   private void testPostContainerWithEmptyName(final boolean toRootContainer) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final String body = null; // Body does not matter
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, JsonLdFormatter.JSON_LD_STRING);
      httpHeaders.add("Slug", "");
      httpHeaders.add(HttpHeaders.LINK, "<http://www.w3.org/ns/ldp#BasicContainer>;  rel=\"type\"");
      checkException(InvalidRequestException.class, ErrorMessageRegistry.CONTAINER_NO_EMPTY_SLUG_ALLOWED, () -> {
         if (toRootContainer) {
            controller.postRootContainer(new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(),
                  createParamsMap(null), TurtleFormatter.TURTLE_STRING), body, httpHeaders);
         } else {
            controller.postContainer(new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(),
                  createParamsMap(null), TurtleFormatter.TURTLE_STRING), body, httpHeaders);
         }
      });
   }

   private void testPostContainerNotBasicContainer(final boolean toRootContainer) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final String body = null; // Body does not matter
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, JsonLdFormatter.JSON_LD_STRING);
      httpHeaders.add(HttpHeaders.LINK, "<http://www.w3.org/ns/ldp#DirectContainer>;  rel=\"type\"");
      checkException(InvalidContainerException.class, ErrorMessageRegistry.CONTAINER_ONLY_BASIC_CONTAINER_ALLOWED,
            () -> {
               if (toRootContainer) {
                  controller.postRootContainer(new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(),
                        createParamsMap(null), TurtleFormatter.TURTLE_STRING), body, httpHeaders);
               } else {
                  controller.postContainer(new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(),
                        createParamsMap(null), TurtleFormatter.TURTLE_STRING), body, httpHeaders);
               }
            });
   }

   private void testPostContainerWithoutContentType(final boolean toRootContainer) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + (toRootContainer ? "" : "container1/"));
      final String body = null; // Body does not matter
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.LINK, "<http://www.w3.org/ns/ldp#BasicContainer>;  rel=\"type\"");
      checkException(HttpHeaderException.class, ErrorMessageRegistry.ALL_CONTENT_TYPE_NEEDED_IN_POST, () -> {
         if (toRootContainer) {
            controller.postRootContainer(new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(),
                  createParamsMap(null), TurtleFormatter.TURTLE_STRING), body, httpHeaders);
         } else {
            controller.postContainer(new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(),
                  createParamsMap(null), TurtleFormatter.TURTLE_STRING), body, httpHeaders);
         }
      });
   }

   /**
    * Test delete container with params
    */
   @Test
   final void testDeleteContainerWithParams() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/");
      final String etag = etagFactory.generateEtag();
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.IF_MATCH, "\"" + etag + "\"");
      checkException(IllegalHttpParameterException.class,
            ErrorMessageRegistry.CONTAINER_POST_DELETE_NO_PARAMETERS_ALLOWED, () -> {
               controller.deleteContainer(new HttpServletRequestAdapter(iri, HttpMethod.DELETE.toString(),
                     createParamsMap("iris=0"), TurtleFormatter.TURTLE_STRING), httpHeaders);
            });
   }

   /**
    * Test delete container without ifmatch
    */
   @Test
   final void testDeleteContainerWithoutIfMatch() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/");
      final HttpHeaders httpHeaders = new HttpHeaders();
      checkException(HttpHeaderException.class, ErrorMessageRegistry.ALL_ETAG_NEEDED_FOR_DELETE, () -> {
         controller.deleteContainer(new HttpServletRequestAdapter(iri, HttpMethod.DELETE.toString(),
               createParamsMap(null), TurtleFormatter.TURTLE_STRING), httpHeaders);
      });
   }

   /**
    * Test delete container with invalid etag
    */
   @Test
   final void testDeleteContainerInvalidEtag() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/");
      final String etag = etagFactory.generateEtag();
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.IF_MATCH, etag); // not quoted
      checkException(HttpHeaderException.class, ErrorMessageRegistry.ALL_INVALID_ETAG_FORMAT, () -> {
         controller.deleteContainer(new HttpServletRequestAdapter(iri, HttpMethod.DELETE.toString(),
               createParamsMap(null), TurtleFormatter.TURTLE_STRING), httpHeaders);
      });
   }

   /**
    * Test delete container.
    */
   @Test
   final void testDeleteContainerValid() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/");
      final String etag = etagFactory.generateEtag();
      // Nothing to mock, the method should just not throw an exception, return=void
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.IF_MATCH, "\"" + etag + "\"");
      ResponseEntity<?> response = controller.deleteContainer(new HttpServletRequestAdapter(iri,
            HttpMethod.DELETE.toString(), createParamsMap(null), TurtleFormatter.TURTLE_STRING), httpHeaders);
      assertEquals(ContainerConstants.DELETE_CONTAINER_SUCCESS_CODE, response.getStatusCode().value());
      assertNull(response.getBody());
   }

   @Override
   protected WapServerConfig getWapServerConfig() {
      return wapServerConfig;
   }
}
