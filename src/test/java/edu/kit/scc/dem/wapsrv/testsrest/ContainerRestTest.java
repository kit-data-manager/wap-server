package edu.kit.scc.dem.wapsrv.testsrest;

import static org.junit.jupiter.api.Assertions.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.controller.AnnotationConstants;
import edu.kit.scc.dem.wapsrv.controller.ContainerConstants;
import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.exceptions.GlobalErrorCodes;
import edu.kit.scc.dem.wapsrv.exceptions.HttpHeaderException;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalHttpParameterException;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidContainerException;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidRequestException;
import edu.kit.scc.dem.wapsrv.exceptions.MethodNotAllowedException;
import edu.kit.scc.dem.wapsrv.exceptions.NotExistentException;
import edu.kit.scc.dem.wapsrv.exceptions.ResourceDeletedException;
import edu.kit.scc.dem.wapsrv.exceptions.ResourceExistsException;
import edu.kit.scc.dem.wapsrv.model.ContainerPreference;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.Formatter;
import edu.kit.scc.dem.wapsrv.testscommon.OwnHttpURLConnection;
import edu.kit.scc.dem.wapsrv.testscommon.OwnResponse;
import edu.kit.scc.dem.wapsrv.testscommon.TestDataStore;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.test.context.ActiveProfiles;

/**
 * ContainerRestTest
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Tag("rest")
@ActiveProfiles("test")
public class ContainerRestTest extends AbstractRestTest {
   /**
    * The logger to use
    */
   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   /**
    * Create a new ContainerRestTest
    */
   protected ContainerRestTest() {
      super(true);
   }

   /**
    * Gets the lock.
    */
   @BeforeEach
   public void getLock() {
      lock();
      logger.trace("Running REST Container test");
   }

   /**
    * Free lock.
    */
   @AfterEach
   public void freeLock() {
      logger.trace("Finished REST Container test");
      unlock();
   }

   /**
    * Test get container valid with iris params.
    */
   @Test
   public void testGetContainerValidWithIrisParams() {
      String containerIri = createDefaultContainer(null); // in the root container
      String name = extractContainerName(containerIri);
      // iris=0 test
      RequestSpecification request = RestAssured.given();
      request.param("iris", "0");
      Response response = request.get(name + "/");
      assertNotNull(response, "Could not get container response");
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be fetched with iris=0");
      checkProperty(response, "id", containerIri + "?iris=0");
      // iris=1 test
      request = RestAssured.given();
      request.param("iris", "1");
      response = request.get(name + "/");
      assertNotNull(response, "Could not get container response");
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be fetched with iris=1");
      checkProperty(response, "id", containerIri + "?iris=1");
   }

   /**
    * Test get container valid with iris params override prefer.
    */
   @Test
   public void testGetContainerValidWithIrisParamsOverridePrefer() {
      String containerIri = createDefaultContainer(null); // in the root container
      String name = extractContainerName(containerIri);
      // iris=0 test
      RequestSpecification request = RestAssured.given();
      addPreferHeader(request, 1, true);
      request.param("iris", "0");
      Response response = request.get(name + "/");
      assertNotNull(response, "Could not get container response");
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be fetched with iris=0");
      checkProperty(response, "id", containerIri + "?iris=0");
      // iris=1 test
      request = RestAssured.given();
      addPreferHeader(request, 0, true);
      request.param("iris", "1");
      response = request.get(name + "/");
      assertNotNull(response, "Could not get container response");
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be fetched with iris=1");
      checkProperty(response, "id", containerIri + "?iris=1");
   }

   /**
    * Test get container invalid with params.
    */
   @Test
   public void testGetContainerInvalidWithParams() {
      String containerIri = createDefaultContainer(null); // in the root container
      String name = extractContainerName(containerIri);
      // invalid iris test
      RequestSpecification request = RestAssured.given();
      request.param("iris", "-1");
      Response response = request.get(name + "/");
      assertNotNull(response, "Could not get container response");
      checkException(IllegalHttpParameterException.class, response,
            ErrorMessageRegistry.CONTAINER_GET_HEAD_OPTIONS_NO_PARAMETERS_ALLOWED_BUT_IRIS);
   }

   /**
    * Test head options container.
    */
   @Test
   final void testHeadOptionsContainer() {
      String containerIri = createDefaultContainer(null);
      String containerName = extractContainerName(containerIri);
      RequestSpecification request = RestAssured.given();
      Response response = request.options(containerName + "/");
      // System.err.println(response.getHeaders());
      assertNotNull(response.getHeaders(), "Could not get header options response");
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(), "Unexpected response code");
   }

   /**
    * Test get container valid minimal all formats.
    */
   @Test
   public void testGetContainerValidMinimalAllFormats() {
      testReadContainerValidIntAllFormats(0, HttpMethod.GET, false, true);
   }

   /**
    * Test head container valid minimal all formats.
    */
   @Test
   public void testHeadContainerValidMinimalAllFormats() {
      testReadContainerValidIntAllFormats(0, HttpMethod.HEAD, false, true);
   }

   /**
    * Test options container valid minimal all formats.
    */
   @Test
   public void testOptionsContainerValidMinimalAllFormats() {
      testReadContainerValidIntAllFormats(0, HttpMethod.OPTIONS, false, true);
   }

   /**
    * Test cors container valid minimal all formats.
    */
   @Test
   public void testCorsContainerValidMinimalAllFormats() {
      testReadContainerValidIntAllFormats(0, HttpMethod.OPTIONS, true, true);
   }

   /**
    * Test get container valid with iris 0 all formats.
    */
   @Test
   public void testGetContainerValidWithIris0AllFormats() {
      testReadContainerValidIntAllFormats(0, HttpMethod.GET, false, false);
   }

   /**
    * Test head container valid with iris 0 all formats.
    */
   @Test
   public void testHeadContainerValidWithIris0AllFormats() {
      testReadContainerValidIntAllFormats(0, HttpMethod.HEAD, false, false);
   }

   /**
    * Test options container valid with iris 0 all formats.
    */
   @Test
   public void testOptionsContainerValidWithIris0AllFormats() {
      testReadContainerValidIntAllFormats(0, HttpMethod.OPTIONS, false, false);
   }

   /**
    * Test cors container valid with iris 0 all formats.
    */
   @Test
   public void testCorsContainerValidWithIris0AllFormats() {
      testReadContainerValidIntAllFormats(0, HttpMethod.OPTIONS, true, false);
   }

   /**
    * Test get container valid with iris 1 all formats.
    */
   @Test
   public void testGetContainerValidWithIris1AllFormats() {
      testReadContainerValidIntAllFormats(1, HttpMethod.GET, false, false);
   }

   /**
    * Test head container valid with iris 1 all formats.
    */
   @Test
   public void testHeadContainerValidWithIris1AllFormats() {
      testReadContainerValidIntAllFormats(1, HttpMethod.HEAD, false, false);
   }

   /**
    * Test options container valid with iris 1 all formats.
    */
   @Test
   public void testOptionsContainerValidWithIris1AllFormats() {
      testReadContainerValidIntAllFormats(1, HttpMethod.OPTIONS, false, false);
   }

   /**
    * Test cors container valid with iris 1 all formats.
    */
   @Test
   public void testCorsContainerValidWithIris1AllFormats() {
      testReadContainerValidIntAllFormats(1, HttpMethod.OPTIONS, true, false);
   }

   private void testReadContainerValidIntAllFormats(final int iris, final HttpMethod httpMethod, final boolean cors,
         final boolean preferMinimalContainer) {
      final int expectedCode = ContainerConstants.GET_CONTAINER_SUCCESS_CODE;
      assertNotEqual(-1, expectedCode, "Unallowed http method : " + httpMethod.toString());
      // Create container, sub-container and one anno for embedding pages
      final String containerIri = createDefaultContainer(null); // in the root container
      final String name = extractContainerName(containerIri);
      createDefaultAnnotation(name + "/");
      createDefaultContainer(name + "/");
      testReadContainerValidIntJsonLd(name, containerIri, expectedCode, iris, httpMethod, cors, preferMinimalContainer);
      for (String formatString : getFormatRegistry().getFormatStrings()) {
         logger.trace("Getting : " + formatString);
         if (WapServerConfig.getInstance().shouldAlwaysAddDefaultProfilesToJsonLdRequests()) {
            // When we always add the anno profile, this case has already been tested
            // and the second test here would neither work nor be necessary anymore
            if (formatString.startsWith("application/ld+json")) {
               continue;
            }
         }
         RequestSpecification request = RestAssured.given();
         request.accept(formatString);
         addPreferHeader(request, iris, preferMinimalContainer);
         if (cors) {
            request.header("Origin", "http://allowed.org");
         }
         Response response = httpMethod == HttpMethod.GET ? request.get(name + "/")
               : httpMethod == HttpMethod.HEAD ? request.head(name + "/")
                     : httpMethod == HttpMethod.OPTIONS ? request.options(name + "/") : null;
         assertNotNull(response, "Could not get container response");
         assertEquals(expectedCode, response.getStatusCode(), "Container could not be fetched");
         // Check http header
         if (httpMethod != HttpMethod.OPTIONS) {
            checkHeader(response, "Content-Type", formatString);
            checkHeaderExists(response, "ETag");
         }
         for (String method : ContainerConstants.VARY_LIST) {
            checkHeaderContains(response, "Vary", method);
         }
         for (HttpMethod method : ContainerConstants.ALLOWED_METHODS) {
            checkHeaderContains(response, "Allow", method.toString());
         }
         checkHeader(response, "Link",
               new String[] {ContainerConstants.LINK_TYPE, ContainerConstants.LINK_ANNOTATION_PROTOCOL});
      }
   }

   private void testReadContainerValidIntJsonLd(final String name, final String containerIri, final int expectedCode,
         final int iris, final HttpMethod httpMethod, final boolean cors, final boolean preferMinimalContainer) {
      // Test using default profile. Get the label and modified from the server (container values)
      final String label = getLabelFromServer(name + "/");
      final String modified = getModifiedFromServer(name + "/");
      assertNotNull(label);
      assertNotNull(modified);
      RequestSpecification request = RestAssured.given();
      addPreferHeader(request, iris, preferMinimalContainer);
      if (cors) {
         request.header("Origin", "http://allowed.org");
      }
      Response response = httpMethod == HttpMethod.GET ? request.get(name + "/")
            : httpMethod == HttpMethod.HEAD ? request.head(name + "/")
                  : httpMethod == HttpMethod.OPTIONS ? request.options(name + "/") : null;
      assertNotNull(response, "Could not get container response");
      assertEquals(expectedCode, response.getStatusCode(), "Container could not be fetched");
      // Check embedded page
      if (httpMethod == HttpMethod.GET && !preferMinimalContainer) {
         final String pageIri = response.then().extract().path("first.id");
         assertNotNull(pageIri);
         checkValidPageIri(pageIri, iris, 0, containerIri);
         checkProperty(response, "first.type", "AnnotationPage");
         checkEmbeddedPagesItemsExist(response);
         // Validation of items has been done in page tests. since both are created from the
         // exact same code, both will work or both not.
         checkProperty(response, "first.startIndex", "0");
         checkNotProperty(response, "first.next"); // here no next exists
         checkNotProperty(response, "first.prev"); // and no prev
         // partOf should not be contained when embedded
         checkNotProperty(response, "first.partOf");
      }
      // Check container values
      if (httpMethod == HttpMethod.GET) {
         checkProperty(response, "id", containerIri + "?iris=" + iris);
         checkProperty(response, "total", "1");
         // label and modified have been checked before
         checkProperty(response, "label");
         checkContainerTypes(response);
         final String lastPageIri = response.then().extract().path("last");
         assertNotNull(lastPageIri);
         checkValidPageIri(lastPageIri, iris, 0, containerIri);
         if (preferMinimalContainer) {
            // first and last should be the same, last is already verified
             System.out.println("LAST " + lastPageIri);
            checkProperty(response, "first", lastPageIri);
            // check no list of sub containers
            checkLdpContains(response, false);
         } else {
            // first has been verified already during embedded first page validation
            // check list of subcontainers
            checkLdpContains(response, true);
         }
      }
      final String formatString = getDefaultFormatString(Type.CONTAINER);
      // Check http header
      if (httpMethod != HttpMethod.OPTIONS) {
         checkHeader(response, "Content-Type", formatString);
         checkHeaderExists(response, "ETag");
      }
      for (String method : ContainerConstants.VARY_LIST) {
         checkHeaderContains(response, "Vary", method);
      }
      for (HttpMethod method : ContainerConstants.ALLOWED_METHODS) {
         checkHeaderContains(response, "Allow", method.toString());
      }
      checkHeader(response, "Link",
            new String[] {ContainerConstants.LINK_TYPE, ContainerConstants.LINK_ANNOTATION_PROTOCOL});
      // Check Location header fits internal id
      checkHeader(response, "Content-Location", containerIri + "?iris=" + iris);
   }

   private void checkEmbeddedPagesItemsExist(Response response) {
      // Maybe this can be done "better" but works with the actual implementation
      // and the usual checks with then(). ... first.items does not work
      String body = response.getBody().asString();
      boolean contained = body.indexOf("\"items\"") != -1;
      if (!contained) {
         System.err.println("Missing items ? " + body);
      }
      assertTrue(contained, "Embedde page has no items property");
   }

   private void checkLdpContains(Response response, boolean withLdp) {
      // Maybe this can be done "better" but works with the actual implementation
      String body = response.getBody().asString();
      boolean contained = body.indexOf("\"ldp#contains\"") != -1 || body.indexOf("\"ldp:contains\"") != -1
            || body.indexOf("\"contains\"") != -1; // if context exits, it can be simply container
      if (withLdp) {
         if (!contained) {
            System.err.println("not:\n" + body);
         }
         assertTrue(contained, "ldp:contains does not exist, but should");
      } else {
         if (contained) {
            System.err.println("with:\n" + body);
         }
         assertFalse(contained, "ldp:contains exists, but should not");
      }
   }

   private void addPreferHeader(RequestSpecification request, int iris, boolean preferMinimalContainer) {
      // example that would be given in one line : return=representation;include=
      // "http://www.w3.org/ns/ldp#PreferMinimalContainer
      // http://www.w3.org/ns/oa#PreferContainedIRIs"
      StringBuilder preferBuilder = new StringBuilder();
      preferBuilder.append("return=representation;include=\"");
      if (preferMinimalContainer) {
         if (iris == ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS) {
            preferBuilder.append(ContainerPreference.PREFER_MINIMAL_CONTAINER_STRING + " "
                  + ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS_STRING);
         } else if (iris == ContainerPreference.PREFER_CONTAINED_IRIS) {
            preferBuilder.append(ContainerPreference.PREFER_MINIMAL_CONTAINER_STRING + " "
                  + ContainerPreference.PREFER_CONTAINED_IRIS_STRING);
         } else {
            fail("invalid container preference selected : " + iris);
         }
      } else {
         if (iris == ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS) {
            preferBuilder.append(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS_STRING);
         } else if (iris == ContainerPreference.PREFER_CONTAINED_IRIS) {
            preferBuilder.append(ContainerPreference.PREFER_CONTAINED_IRIS_STRING);
         } else {
            fail("invalid container preference selected : " + iris);
         }
      }
      preferBuilder.append("\"");
      request.header("Prefer", preferBuilder.toString());
   }

   private void checkContainerTypes(Response response) {
      // check of types. This is somehow difficult because
      // there may be different but equal representations of the same
      // like type AnnotationCollection == http://www.w3.org/ns/anno#AnnotationCollection
      // == https://www.w3.org/ns/anno#AnnotationCollection and so on
      // therefore we check only for the type word itself that is is contained in any of the type
      // properties. That is the only common thing
      String body = response.getBody().asString();
      String[] types = new String[] {"BasicContainer", "AnnotationCollection"};
      for (String type : types) {
         if (body.indexOf(type) == -1) {
            System.err.println("container body with missing type :\n" + body);
            fail("Container type " + type + " is missing");
         }
      }
   }

   private void checkValidPageIri(String pageIri, int iris, int pageNr, String containerIri) {
      assertEquals(containerIri + "?iris=" + iris + "&page=" + pageNr, pageIri);
   }

   /**
    * Test invalid container preferences.
    */
   @Test
   public void testInvalidContainerPreferences() {
      final String containerIri = createDefaultContainer(null); // in the root container
      final String name = extractContainerName(containerIri);
      final String annoIri = createDefaultAnnotation(name + "/");
      final String subContainerIri = createDefaultContainer(name + "/");
      if (annoIri != null && subContainerIri != null) {
      }
      {
         // Test for an invalid Prefer header syntactically
         RequestSpecification request = RestAssured.given();
         StringBuilder preferBuilder = new StringBuilder();
         preferBuilder.append("return=representation;inclu333de=\"");
         preferBuilder.append(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS_STRING);
         preferBuilder.append("\"");
         request.header("Prefer", preferBuilder.toString());
         Response response = request.get(name + "/");
         assertNotNull(response, "Could not get container response");
         checkException(HttpHeaderException.class, response);
      }
      {
         // Test for an invalid Prefer header type
         RequestSpecification request = RestAssured.given();
         StringBuilder preferBuilder = new StringBuilder();
         preferBuilder.append("return=representation;include=\"");
         preferBuilder.append(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS_STRING + "_2");
         preferBuilder.append("\"");
         request.header("Prefer", preferBuilder.toString());
         Response response = request.get(name + "/");
         assertNotNull(response, "Could not get container response");
         checkException(HttpHeaderException.class, response);
      }
      {
         // Test for the invalid combination
         RequestSpecification request = RestAssured.given();
         StringBuilder preferBuilder = new StringBuilder();
         preferBuilder.append("return=representation;inclu333de=\"");
         preferBuilder.append(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS_STRING);
         preferBuilder.append(" ");
         preferBuilder.append(ContainerPreference.PREFER_CONTAINED_IRIS_STRING);
         preferBuilder.append("\"");
         request.header("Prefer", preferBuilder.toString());
         Response response = request.get(name + "/");
         assertNotNull(response, "Could not get container response");
         checkException(HttpHeaderException.class, response);
      }
   }

   /**
    * Test delete outer container.
    */
   @Test
   public void testDeleteOuterContainer() {
      // Outside container
      final String outerContainerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", outerContainerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      final String outerEtagBefore = getEtag(response);
      if (outerEtagBefore != null) {
      }
      // Inner container
      final String innerContainerName = getRandomContainerName();
      request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", innerContainerName);
      request.contentType("application/ld+json;");
      response = request.post(outerContainerName + "/");
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      final String innerEtag = getEtag(response);
      if (innerEtag != null) {
      }
      // Get outside container etag
      final String outerEtagAfter = getEtagFromServer(outerContainerName + "/");
      // Delete outside container
      request = RestAssured.given();
      request.header("If-Match", outerEtagAfter);
      Response deleteResponse = request.delete(outerContainerName + "/");
      assertNotNull(deleteResponse, "Could not get delete response");
      assertEquals(GlobalErrorCodes.METHOD_NOT_ALLOWED, deleteResponse.getStatusCode(),
            "Container could be deleted even if it contains subcontainer");
   }

   /**
    * Test get container not exist.
    */
   @Test
   public void testGetContainerNotExist() {
      String containerIri = createDefaultContainer(null);
      String containerName = extractContainerName(containerIri);
      RequestSpecification request = RestAssured.given();
      Response response = request.get(containerName + "/");
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be fetched");
      request = RestAssured.given();
      response = request.get(containerName + "/");
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be fetched");
   }

   /**
    * Test head container.
    */
   @Test
   final void testHeadContainer() {
      String containerIri = createDefaultContainer(null);
      String containerName = extractContainerName(containerIri);
      RequestSpecification request = RestAssured.given();
      Response response = request.head(containerName + "/");
      assertNotNull(response, "Could not get head response");
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(), "Unexpected response code");
   }

   /**
    * Test options container.
    */
   @Test
   final void testOptionsContainer() {
      String containerIri = createDefaultContainer(null);
      String containerName = extractContainerName(containerIri);
      RequestSpecification request = RestAssured.given();
      Response response = request.options(containerName + "/");
      assertNotNull(response, "Could not get options response");
      // logger.trace(response.asString());
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(), "Unexpected response code");
   }

   /**
    * Test post container.
    */
   @Test
   public void testPostContainer() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
   }

   /**
    * Test post container all but JSON-LD format.
    */
   @Test
   public void testPostContainerAllButJsonLdFormat() {
      // Tests only that it works. The headers, properties and so on are checked elsewhere
      final String containerJsondLd = TestDataStore.getContainer("example1.jsonld");
      for (String formatString : getFormatRegistry().getFormatStrings()) {
         if (formatString.startsWith("application/ld+json"))
            continue;
         logger.trace("Posting container using " + formatString);
         Formatter formatter = null;
         try {
            formatter = getFormatRegistry().getFormatter(formatString);
         } catch (InternalServerException | NullPointerException e) {
            fail(e.getMessage());
         }
         Format format = formatter.getFormat();
         String convertedContainer = this.convertFormat(containerJsondLd, format);
         String containerName = getRandomContainerName();
         RequestSpecification request = RestAssured.given();
         request.body(convertedContainer);
         request.header("Link", ContainerConstants.LINK_TYPE);
         request.header("Slug", containerName);
         request.config(RestAssured.config()
               .encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs(formatString, ContentType.TEXT)));
         request.contentType(formatter.getContentType());
         Response response = request.post();
         assertNotNull(response, "Could not get response");
         System.err.println(response.getBody().asString());
         assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
               "Container could not be created");
      }
   }

   /**
    * Test post container to non existent one.
    */
   @Test
   public void testPostContainerToNonExistentOne() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post(containerName + "/");
      assertNotNull(response, "Could not get response");
      checkException(NotExistentException.class, response);
   }

   /**
    * Test post container to deleted one.
    */
   @Test
   public void testPostContainerToDeletedOne() {
      String containerIri = this.createDefaultContainer(null);
      assertNotNull("Could not create container");
      String baseContainerName = getPathFromIri(containerIri);
      String etag = getEtagFromServer(baseContainerName);
      // Delete
      RequestSpecification request = RestAssured.given();
      request.header("If-Match", etag);
      Response deleteResponse = request.delete(baseContainerName);
      assertNotNull(deleteResponse, "Could not get delete response");
      assertEquals(ContainerConstants.DELETE_CONTAINER_SUCCESS_CODE, deleteResponse.getStatusCode(),
            "Container could not be deleted");
      String containerName = getRandomContainerName();
      request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post(baseContainerName);
      assertNotNull(response, "Could not get response");
      // System.err.println(response.getBody().asString());
      checkException(ResourceDeletedException.class, response);
   }

   /**
    * Test post container invalid content types.
    */
   @Test
   public void testPostContainerInvalidContentTypes() {
      {
         // No content type
         String containerName = getRandomContainerName();
         RequestSpecification request = RestAssured.given();
         request.body(TestDataStore.getContainer());
         request.header("Link", ContainerConstants.LINK_TYPE);
         request.header("Slug", containerName);
         Response response = request.post();
         assertNotNull(response, "Could not get response");
         checkException(FormatException.class, response);
      }
      {
         // text/html, can never work
         String containerName = getRandomContainerName();
         RequestSpecification request = RestAssured.given();
         request.body(TestDataStore.getContainer());
         request.header("Link", ContainerConstants.LINK_TYPE);
         request.header("Slug", containerName);
         request.contentType("text/html");
         Response response = request.post();
         assertNotNull(response, "Could not get response");
         // System.err.println(response.getHeaders());
         checkException(FormatException.class, response);
      }
   }

   /**
    * Test post container invalid params.
    */
   @Test
   public void testPostContainerInvalidParams() {
      if (WapServerConfig.getInstance().isHttpsEnabled()) {
         if (FAIL_ON_UNSUPPORTED_TESTS) {
            fail("No https support in own connections");
         }
         return;
      }
      try {
         String container = TestDataStore.getContainer();
         String containerName = getRandomContainerName();
         URL url = new URL(WapServerConfig.getInstance().getRootContainerIri());
         String params = "test=1";
         Map<String, Object> requestHeaders = new Hashtable<String, Object>();
         requestHeaders.put("Content-Type", "application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
         requestHeaders.put("Accept", "application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
         requestHeaders.put("Slug", containerName);
         requestHeaders.put("Link", ContainerConstants.LINK_TYPE);
         OwnResponse response
               = performOwnHttpRequest(url, OwnHttpURLConnection.Request.POST, requestHeaders, params, container);
         // logger.trace(response.getTransmittedString());
         // logger.trace(response.getReceivedString());
         assertEquals(GlobalErrorCodes.INVALID_REQUEST, response.getStatus());
         checkException(IllegalHttpParameterException.class, response);
      } catch (MalformedURLException e) {
         fail(e.getMessage());
      }
   }

   /**
    * Test post container invalid names.
    */
   @Test
   public void testPostContainerInvalidNames() {
      String[] invalidNames
            = new String[] {"Test#Test", "TestöTest", "Test!Test", "", "<>", "Invalid Name", "!error", "test?test"};
      for (String invalidName : invalidNames) {
         RequestSpecification request = RestAssured.given();
         request.body(TestDataStore.getContainer());
         request.header("Link", ContainerConstants.LINK_TYPE);
         request.header("Slug", invalidName);
         request.contentType("application/ld+json;");
         Response response = request.post();
         assertNotNull(response, "Could not get response");
         if (response.getStatusCode() == 201) {
            System.err.println("Accepted name " + invalidName);
         }
         checkException(InvalidRequestException.class, response);
      }
   }

   /**
    * Test post container no slug.
    */
   @Test
   public void testPostContainerNoSlug() {
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      // logger.trace(response.getBody().asString());
      if (WapServerConfig.getInstance().isSlugMandatoryInContainerPosts()) {
         checkException(InvalidRequestException.class, response, ErrorMessageRegistry.CONTAINER_SLUG_IS_SET_MANDATORY);
      } else {
         assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
               "Container could not be created");
      }
   }

   /**
    * Test post container no link but slug.
    */
   @Test
   public void testPostContainerNoLinkButSlug() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      // System.err.println(response.getBody().asString());
      checkException(InvalidRequestException.class, response, ErrorMessageRegistry.CONTAINER_LINK_NEEDED_IN_POST);
   }

   /**
    * Test post container no link no slug.
    */
   @Test
   public void testPostContainerNoLinkNoSlug() {
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      // no link and no slug ==> server assumes anno post.
      // System.err.println(response.getBody().asString());
      // but anno post to root is not allowed
      checkException(MethodNotAllowedException.class, response,
            ErrorMessageRegistry.ANNOTATION_NO_POST_TO_ROOT_CONTAINER);
   }

   /**
    * Test post container invalid link type.
    */
   @Test
   public void testPostContainerInvalidLinkType() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      request.header("Link", "<http://www.w3.org/ns/ldp/Container>; rel=\"type\"");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      // logger.trace(response.getBody().asString());
      checkException(InvalidContainerException.class, response);
   }

   /**
    * Test post container twice.
    */
   @Test
   public void testPostContainerTwice() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      response = request.post();
      assertNotNull(response, "Could not get response");
      checkException(ResourceExistsException.class, response);
   }

   /**
    * Test post container again after deletion.
    */
   @Test
   public void testPostContainerAgainAfterDeletion() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      String containerString = TestDataStore.getContainer("example1.jsonld");
      request.body(containerString);
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      String etag = getEtag(response);
      // "label": "Alice data storage on the Web"
      final String labelWithAlice = getLabelFromServer(containerName + "/");
      assertNotEquals(-1, labelWithAlice.indexOf("Alice"));
      assertEquals(-1, labelWithAlice.indexOf("Bob"));
      // Delete
      RequestSpecification deleteRequest = RestAssured.given();
      deleteRequest.header("If-Match", etag);
      Response deleteResponse = deleteRequest.delete(containerName + "/");
      assertNotNull(deleteResponse, "Could not get delete response");
      assertEquals(ContainerConstants.DELETE_CONTAINER_SUCCESS_CODE, deleteResponse.getStatusCode(),
            "Container could not be deleted");
      // post again, now with Bob as the name in the label, not Alice
      containerString = containerString.replaceAll(Pattern.quote("Alice"), "Bob");
      request = RestAssured.given();
      request.body(containerString);
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      response = request.post();
      assertNotNull(response, "Could not get response");
      // we use slug, therefore we should be able to recreate it
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      // "label": "Bob data storage on the Web"
      final String labelWithBob = getLabelFromServer(containerName + "/");
      assertEquals(-1, labelWithBob.indexOf("Alice"));
      assertNotEquals(-1, labelWithBob.indexOf("Bob"));
   }

   /**
    * Test post inner container.
    */
   @Test
   public void testPostInnerContainer() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      // Inner container
      String outerName = containerName;
      containerName = getRandomContainerName();
      request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      response = request.post(outerName + "/");
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      String iri = getIri(response);
      String shouldIri = getBaseUrl() + outerName + "/" + containerName + "/";
      assertEquals(shouldIri, iri, "Iri not as expected");
   }

   /**
    * Test post inner container not twice.
    */
   @Test
   public void testPostInnerContainerNotTwice() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      // Inner container
      String outerName = containerName;
      containerName = getRandomContainerName();
      request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      response = request.post(outerName + "/");
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      String iri = getIri(response);
      String shouldIri = getBaseUrl() + outerName + "/" + containerName + "/";
      assertEquals(shouldIri, iri, "Iri not as expected");
      // try another time to post
      response = request.post(outerName + "/");
      assertNotNull(response, "Could not get response");
      assertEquals(GlobalErrorCodes.METHOD_NOT_ALLOWED, response.getStatusCode(), "Container should not be created");
   }

   /**
    * Test delete container.
    */
   @Test
   public void testDeleteContainer() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      String etag = getEtag(response);
      // Delete
      request = RestAssured.given();
      request.header("If-Match", etag);
      Response deleteResponse = request.delete(containerName + "/");
      assertNotNull(deleteResponse, "Could not get delete response");
      assertEquals(ContainerConstants.DELETE_CONTAINER_SUCCESS_CODE, deleteResponse.getStatusCode(),
            "Container could not be deleted");
   }

   /**
    * Test delete container not exist.
    */
   @Test
   public void testDeleteContainerNotExist() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      String etag = getEtag(response);
      // Delete
      request = RestAssured.given();
      request.header("If-Match", etag);
      Response deleteResponse = request.delete(containerName + "/");
      assertNotNull(deleteResponse, "Could not get delete response");
      assertEquals(ContainerConstants.DELETE_CONTAINER_SUCCESS_CODE, deleteResponse.getStatusCode(),
            "Container could not be deleted");
      Response deleteResponseNew = request.delete(containerName + "/");
      assertNotNull(deleteResponseNew, "Could not get delete response");
      assertEquals(GlobalErrorCodes.RESOURCE_HAS_BEEN_DELETED, deleteResponseNew.getStatusCode(),
            "Container should not be deletable");
   }

   /**
    * Test delete container with annotation.
    */
   @Test
   public void testDeleteContainerWithAnnotation() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      String annotationIri = createDefaultAnnotation(containerName + "/");
      if (annotationIri != null) {
      }
      String etag = getEtagFromServer(containerName + "/");
      // Delete
      request = RestAssured.given();
      request.header("If-Match", etag);
      Response deleteResponse = request.delete(containerName + "/");
      assertNotNull(deleteResponse, "Could not get delete response");
      assertEquals(ContainerConstants.DELETE_CONTAINER_SUCCESS_CODE, deleteResponse.getStatusCode(),
            "Container could not be deleted");
   }

   /**
    * Test delete container with annotation not exist.
    */
   @Test
   public void testDeleteContainerWithAnnotationNotExist() {
      String containerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", containerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      String annotationIri = createDefaultAnnotation(containerName + "/");
      if (annotationIri != null) {
      }
      String etag = getEtagFromServer(containerName + "/");
      // Delete
      request = RestAssured.given();
      request.header("If-Match", etag);
      Response deleteResponse = request.delete(containerName + "/");
      assertNotNull(deleteResponse, "Could not get delete response");
      assertEquals(ContainerConstants.DELETE_CONTAINER_SUCCESS_CODE, deleteResponse.getStatusCode(),
            "Container could not be deleted");
      deleteResponse = request.delete(containerName + "/");
      assertNotNull(deleteResponse, "Could not get delete response");
      assertEquals(GlobalErrorCodes.RESOURCE_HAS_BEEN_DELETED, deleteResponse.getStatusCode(),
            "Container could not be deleted");
   }

   /**
    * Test get root container.
    */
   @Test
   final void testGetRootContainer() {
      String containerName = "/";
      RequestSpecification request = RestAssured.given();
      Response response = request.get(containerName);
      assertNotNull(response, "Could not get root response");
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Root container could not be fetched");
   }

   /**
    * Test head root container.
    */
   @Test
   final void testHeadRootContainer() {
      String containerName = "/";
      RequestSpecification request = RestAssured.given();
      Response response = request.head(containerName);
      assertNotNull(response, "Could not get head root response");
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(), "Unexpected response code");
   }

   /**
    * Test options root container.
    */
   @Test
   final void testOptionsRootContainer() {
      String containerName = "/";
      RequestSpecification request = RestAssured.given();
      Response response = request.options(containerName);
      assertNotNull(response, "Could not get options root response");
      assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(), "Unexpected response code");
   }

   /**
    * Test etag changesno propagation upwards.
    */
   @Test
   public void testEtagChangesnoPropagationUpwards() {
      // Outside container
      final String outerContainerName = getRandomContainerName();
      RequestSpecification request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", outerContainerName);
      request.contentType("application/ld+json;");
      Response response = request.post();
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      final String outerEtagBefore = getEtag(response);
      logger.info("outer etag before  = " + outerEtagBefore);
      // Inner container
      final String innerContainerName = getRandomContainerName();
      request = RestAssured.given();
      request.body(TestDataStore.getContainer());
      request.header("Link", ContainerConstants.LINK_TYPE);
      request.header("Slug", innerContainerName);
      request.contentType("application/ld+json;");
      response = request.post(outerContainerName + "/");
      assertNotNull(response, "Could not get response");
      assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
            "Container could not be created");
      final String innerEtag = getEtag(response);
      logger.info("inner etag = " + innerEtag);
      // Get outside container etag
      final String outerEtagAfter = getEtagFromServer(outerContainerName + "/");
      assertNotEqual(outerEtagBefore, outerEtagAfter, "ETags did not change");
      logger.info("outer etag after  = " + outerEtagAfter);
      // create annotation in inner one and check outer etag stays the same
      String annotationIri = createDefaultAnnotation(outerContainerName + "/" + innerContainerName + "/");
      if (annotationIri != null) {
      }
      // IRI not needed (as an example only)
      final String outerEtagAfterInnerAnno = getEtagFromServer(outerContainerName + "/");
      logger.info("outer etag after inner anno = " + outerEtagAfterInnerAnno);
      assertEquals(outerEtagAfter, outerEtagAfterInnerAnno, "ETags changed, but should not");
      final String innerEtagAfter = getEtagFromServer(outerContainerName + "/" + innerContainerName + "/");
      if (innerEtag.equals(innerEtagAfter)) {
         fail("ETags did not change");
         logger.info("inner etag after  = " + innerEtagAfter);
      }
   }

   /**
    * Test etag changes all causes.
    */
   @Test
   public void testEtagChangesAllCauses() {
      int fixedAnnoId = 1;
      final String containerIri = createDefaultContainer(null);
      final String path = getPathFromIri(containerIri);
      String etagBefore = getEtagFromServer(path);
      // Create subcontainer
      final String subContainerIri = createDefaultContainer(path);
      final String subPath = getPathFromIri(subContainerIri);
      String etag = getEtagFromServer(path);
      assertNotEqual(etagBefore, etag, "ETags did not change through sub container creation");
      // delete subcontainer
      etagBefore = etag;
      {
         String subEtag = getEtagFromServer(subPath);
         RequestSpecification request = RestAssured.given();
         request.header("If-Match", subEtag);
         Response deleteResponse = request.delete(subPath);
         assertNotNull(deleteResponse, "Could not get delete response");
         assertEquals(ContainerConstants.DELETE_CONTAINER_SUCCESS_CODE, deleteResponse.getStatusCode(),
               "Container could not be deleted");
      }
      etag = getEtagFromServer(path);
      assertNotEqual(etagBefore, etag, "ETags did not change through sub container deletion");
      // add anno
      etagBefore = etag;
      String annotation = getAnnotation(fixedAnnoId); // getRandomAnnotation();
      assertNotNull(annotation, "Could not load example annotation");
      RequestSpecification postRequest = RestAssured.given();
      postRequest.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
      postRequest.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
      postRequest.body(annotation);
      Response postResponse = postRequest.post(path);
      assertNotNull(postResponse, "Could not get post response");
      assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, postResponse.getStatusCode(),
            "Annotation could not be created");
      // Update the string with the value deliverd by server, so id matches for update and canonical...
      annotation = postResponse.body().asString();
      final String annoIri = postResponse.header("Location");
      final String annoPath = getPathFromIri(annoIri);
      etag = getEtagFromServer(path);
      assertNotEqual(etagBefore, etag, "ETags did not change through anno creation");
      // put anno
      etagBefore = etag;
      {
         String annoEtag = getEtagFromServer(annoPath);
         RequestSpecification request = RestAssured.given();
         request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
         request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
         request.header("If-Match", annoEtag);
         request.body(annotation);
         Response putResponse = request.put(annoPath);
         assertNotNull(putResponse, "Could not get put response");
         if (putResponse.getStatusCode() != AnnotationConstants.PUT_ANNOTATION_SUCCESS_CODE) {
            System.err.println("Anno putted :\n" + annotation);
            System.err.println(putResponse.getBody().asString());
         }
         assertEquals(AnnotationConstants.PUT_ANNOTATION_SUCCESS_CODE, putResponse.getStatusCode(),
               "Annotation could not be updated");
      }
      etag = getEtagFromServer(path);
      assertNotEqual(etagBefore, etag, "ETags did not change through anno update");
      // delete anno
      etagBefore = etag;
      {
         String annoEtag = getEtagFromServer(annoPath);
         RequestSpecification request = RestAssured.given();
         request.header("If-Match", annoEtag);
         Response deleteResponse = request.delete(annoPath);
         assertNotNull(deleteResponse, "Could not get delete response");
         assertEquals(AnnotationConstants.DELETE_ANNOTATION_SUCCESS_CODE, deleteResponse.getStatusCode(),
               "Annotation could not be deleted");
      }
      etag = getEtagFromServer(path);
      assertNotEqual(etagBefore, etag, "ETags did not change through anno deletion");
   }

   /**
    * Test post invalid containers.
    */
   @Test
   public void testPostInvalidContainers() {
      String[] invalidKeys = TestDataStore.getInvalidContainerKeys();
      List<String> withErrors = new Vector<String>();
      String containerName = getRandomContainerName();
      for (String key : invalidKeys) {
         logger.trace("Testing post invalid container " + key);
         String container = TestDataStore.getContainer(key);
         RequestSpecification request = RestAssured.given();
         request.body(container);
         request.header("Link", ContainerConstants.LINK_TYPE);
         request.header("Slug", containerName);
         request.contentType("application/ld+json;profile=\""
               + "http://www.w3.org/ns/anno.jsonld http://www.w3.org/ns/ldp.jsonld\"");
         Response response = request.post();
         assertNotNull(response, "Could not get response");
         // System.err.println(response.getBody().asString());
         if (response.getStatusCode() == ContainerConstants.POST_CONTAINER_SUCCESS_CODE) {
            System.err.println("Posting successful of this container : " + key + " \n" + container);
            withErrors.add(key);
         }
         // Different type of errors occur depending on the failures within the container
         // the only common thing is not success as code
      }
      if (!withErrors.isEmpty()) {
         fail("Posting successful of these containers : " + withErrors);
      }
   }
}
