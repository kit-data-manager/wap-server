package edu.kit.scc.dem.wapsrv.testsrest;

import static org.junit.jupiter.api.Assertions.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
import edu.kit.scc.dem.wapsrv.exceptions.MethodNotAllowedException;
import edu.kit.scc.dem.wapsrv.exceptions.NotExistentException;
import edu.kit.scc.dem.wapsrv.exceptions.ResourceDeletedException;
import edu.kit.scc.dem.wapsrv.exceptions.UnallowedPropertyChangeException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * AnnotationRestTest
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@ExtendWith(HoverflyExtension.class)
@HoverflySimulate(source = @HoverflySimulate.Source(value = "w3c_simulation.json", type = HoverflySimulate.SourceType.DEFAULT_PATH))
@Tag("rest")
@ActiveProfiles("test")
public class AnnotationRestTest extends AbstractRestTest {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationRestTest.class);

    private final String testContainer;

    /**
     * Creates a new AnnotationRestTest
     */
    public AnnotationRestTest() {
        super(true);
        testContainer = getRandomContainerName();
        // create testcontainer
        RequestSpecification request = RestAssured.given();
        request.body(TestDataStore.getContainer());
        request.header("Link", ContainerConstants.LINK_TYPE);
        request.header("Slug", testContainer);
        request.contentType("application/ld+json;");
        Response response = request.post();
        assertNotNull(response, "Could not get response");
        logger.trace(response.asString());
        assertEquals(ContainerConstants.POST_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
                "Test Container could not be created");
        getIri(response);
    }

    private String getAnnotationId(Response response) {
        return response.header("Location").substring(response.header("Location").lastIndexOf("/") + 1);
    }

    private String getAnnotationId(String iri) {
        return iri.substring(iri.lastIndexOf("/") + 1);
    }

    /**
     * Gets the lock.
     */
    @BeforeEach
    public void getLock() {
        lock();
        logger.trace("Running test");
    }

    /**
     * Free The lock.
     */
    @AfterEach
    public void freeLock() {
        logger.trace("Finished test");
        unlock();
    }

    /**
     * Test post annotation.
     */
    @Test
    public void testPostAnnotation() {
        String annotation = getRandomAnnotation();
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response response = postAnnotation(request);
        assertNotNull(response, "Could not get response");
        assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                "Annotation could not be created");
        String annotationIri = response.then().extract().path("id");
        // Check we have the annotation IRI in Location header
        checkHeader(response, "Location", annotationIri);
    }

    /**
     * Test post annotation all but JSON-LD format.
     */
    @Test
    public void testPostAnnotationAllButJsonLdFormat() {
        String annotation = getRandomAnnotation();
        assertNotNull(annotation, "Could not load example annotation");
        for (String formatString : getFormatRegistry().getFormatStrings()) {
            if (formatString.startsWith("application/ld+json")) {
                continue;
            }
            logger.trace("Posting annotation using " + formatString);
            Formatter formatter = null;
            try {
                formatter = getFormatRegistry().getFormatter(formatString);
            } catch (NullPointerException | InternalServerException e) {
                fail(e.getMessage());
            }
            Format format = formatter.getFormat();
            String convertedAnnotation = this.convertFormat(annotation, format);
            // logger.trace("Format " + formatter.getContentType());
            // System.out.print(convertedAnnotation);
            RequestSpecification request = RestAssured.given();
            request.config(RestAssured.config()
                    .encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs(formatString, ContentType.TEXT)));
            request.contentType(formatter.getContentType());
            request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.body(convertedAnnotation);
            Response response = postAnnotation(request);
            assertNotNull(response, "Could not get response");
            // logger.trace(response.getBody().asString());
            assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                    "Annotation could not be created");
            String annotationIri = response.then().extract().path("id");
            // Check we have the annotation IRI in Location header
            checkHeader(response, "Location", annotationIri);
        }
    }

    /**
     * Test post annotation with no ID.
     */
    @Test
    public void testPostAnnotationWithNoId() {
        String annotation = getAnnotation(46); // 46 has no id
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response response = postAnnotation(request);
        assertNotNull(response, "Could not get response");
        assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                "Annotation could not be created");
        String annotationIri = response.then().extract().path("id");
        // Check we have the annotation IRI in Location header
        checkHeader(response, "Location", annotationIri);
        String via = response.then().extract().path("via");
        assertNull(via, "Via exists, but should not : " + via);
    }

    /**
     * Test post annotation with id.
     */
    @Test
    public void testPostAnnotationWithId() {
        String annotation = getAnnotation(1); // 1 an id
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response response = postAnnotation(request);
        assertNotNull(response, "Could not get response");
        assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                "Annotation could not be created");
        String annotationIri = response.then().extract().path("id");
        // Check we have the annotation IRI in Location header
        checkHeader(response, "Location", annotationIri);
        String via = response.then().extract().path("via");
        assertNotNull(via, "Via does not exist, but should");
    }

    /**
     * Test post multiple annotation.
     */
    @Test
    public void testPostMultipleAnnotation() {
        if (!WapServerConfig.getInstance().isMultipleAnnotationPostAllowed()) {
            return;
        } else {
            // Needs to go into separate container for anno counting
            final String containerIri = createDefaultContainer(null); // in the root container
            final String containerName = extractContainerName(containerIri);
            StringBuilder annoBuilder = new StringBuilder();
            annoBuilder.append("[\n");
            String[] keys = TestDataStore.listAnnotations();
            for (int n = 0; n < keys.length; n++) {
                String key = keys[n];
                String annotation = TestDataStore.getAnnotation(key);
                annoBuilder.append(annotation);
                if (n < keys.length - 1) {
                    annoBuilder.append(",\n");
                } else {
                    annoBuilder.append("\n");
                }
            }
            annoBuilder.append("]");
            String annotation = annoBuilder.toString();
            // System.out.print("Posting :\n" + annotation);
            assertNotNull(annotation, "Could not load example annotation");
            RequestSpecification request = RestAssured.given();
            request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.accept("text/turtle");
            request.body(annotation);
            Response response = request.post(containerName + "/");
            assertNotNull(response, "Could not get response");
            // logger.trace(request.that().toString());
            // logger.trace(response.getBody().asString());
            assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                    "Annotation could not be created");
            // Check we have the container IRI in Location header
            checkHeader(response, "Location", containerIri);
            // We have negotiated turtle, but multi post ignores it and always return JSON-LD, assert that
            checkHeader(response, "Content-Type", "application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            // Check that all have been created (just the number suffices here)
            int total = getTotalFromServer(containerName + "/");
            assertEquals(keys.length, total, "Not as much annos as expected created (container)");
            total = getTotalFromBody(response.getBody().asString());
            assertEquals(keys.length, total, "Not as much annos as expected created (body)");
        }
    }

    /*
    * Helper method that gets the number of @context in a string == number of annos in an array
     */
    private int getTotalFromBody(String str) {
        int found = 0;
        int fromIndex = 0;
        int index = str.indexOf("\"@context\"", fromIndex);
        while (index != -1) {
            found++;
            fromIndex = index + 1;
            index = str.indexOf("\"@context\"", fromIndex);
        }
        return found;
    }

    /**
     * Test post annotation to non existent container.
     */
    @Test
    public void testPostAnnotationToNonExistentContainer() {
        String annotation = getRandomAnnotation();
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response response = request.post("nonExistentContainer" + System.currentTimeMillis() + "/");
        assertNotNull(response, "Could not get response");
        // logger.trace(response.getBody().asString());
        checkException(NotExistentException.class, response);
    }

    /**
     * Test post annotation to root.
     */
    @Test
    public void testPostAnnotationToRoot() {
        String annotation = getRandomAnnotation();
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response response = request.post();
        assertNotNull(response, "Could not get response");
        // System.err.println(response.getBody().asString());
        checkException(MethodNotAllowedException.class, response,
                ErrorMessageRegistry.ANNOTATION_NO_POST_TO_ROOT_CONTAINER);
    }

    /**
     * Test post annotation with params.
     */
    @Test
    public void testPostAnnotationWithParams() {
        if (WapServerConfig.getInstance().isHttpsEnabled()) {
            if (FAIL_ON_UNSUPPORTED_TESTS) {
                fail("No https support in own connections");
            }
            return;
        }
        String annotation = getRandomAnnotation();
        String containerIri = createDefaultContainer(null); // in the root container
        try {
            URL url = new URL(containerIri);
            String params = "test=1";
            Map<String, Object> requestHeaders = new Hashtable<String, Object>();
            requestHeaders.put("Content-Type", "application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            // requestHeaders.put("If-Match", "\"etag\"");
            requestHeaders.put("Accept", "application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            OwnResponse response
                    = performOwnHttpRequest(url, OwnHttpURLConnection.Request.POST, requestHeaders, params, annotation);
            // logger.trace(response.getTransmittedString());
            // logger.trace(response.getReceivedString());
            assertEquals(GlobalErrorCodes.INVALID_REQUEST, response.getStatus());
            if (response.getReceivedString().indexOf(IllegalHttpParameterException.class.getSimpleName()) == -1) {
                logger.trace(response.getReceivedString());
                fail("Unexpected error received");
            }
        } catch (MalformedURLException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test post annotation without content type.
     */
    @Test
    public void testPostAnnotationWithoutContentType() {
        String annotation = getRandomAnnotation();
        RequestSpecification request = RestAssured.given();
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response response = request.post("container1/");
        assertNotNull(response, "Could not get annotation response");
        // System.err.println(response.getBody().asString());
        // we cannot leave the content type header away with rest assured
        // it is autocreated to text/plain then. Therefore check for FormatException
        // until either self implement this type of request or rest assured can some time
        // send invalid requests
        checkException(FormatException.class, response);
        // checkException(HttpHeaderException.class, response);
    }

    /**
     * Test post annotation with invalid content types.
     */
    @Test
    public void testPostAnnotationWithInvalidContentTypes() {
        String annotation = getRandomAnnotation();
        // html can never be posted
        RequestSpecification request = RestAssured.given();
        request.contentType("text/html");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response response = request.post("container1/");
        assertNotNull(response, "Could not get annotation response");
        checkException(FormatException.class, response);
    }

    /**
     * Test get annotation with parameters.
     */
    @Test
    public void testGetAnnotationWithParams() {
        String containerIri = createDefaultContainer(null); // in the root container
        String name = extractContainerName(containerIri);
        String annoIri = createDefaultAnnotation(name + "/");
        String annoId = getAnnotationId(annoIri);
        String requestPath = name + "/" + annoId;
        RequestSpecification request = RestAssured.given();
        request.param("test", "1");
        Response response = request.get(requestPath);
        assertNotNull(response, "Could not get annotation response");
        checkException(IllegalHttpParameterException.class, response);
    }

    /**
     * Test get annotation deleted.
     */
    @Test
    public void testGetAnnotationDeleted() {
        String containerIri = createDefaultContainer(null); // in the root container
        String name = extractContainerName(containerIri);
        String annoIri = createDefaultAnnotation(name + "/");
        String annoId = getAnnotationId(annoIri);
        String requestPath = name + "/" + annoId;
        String etag = getEtagFromServer(requestPath);
        assertNotNull(etag);
        // 2. delete it
        RequestSpecification request = RestAssured.given();
        request.header("If-Match", etag);
        Response deleteResponse = request.delete(requestPath);
        assertNotNull(deleteResponse, "Could not get delete response");
        assertEquals(AnnotationConstants.DELETE_ANNOTATION_SUCCESS_CODE, deleteResponse.getStatusCode(),
                "Annotation could not be deleted");
        request = RestAssured.given();
        Response response = request.get(requestPath);
        assertNotNull(response, "Could not get annotation response");
        checkException(ResourceDeletedException.class, response);
    }

    /**
     * Test get annotation non existent.
     */
    @Test
    public void testGetAnnotationNonExistent() {
        String containerIri = createDefaultContainer(null); // in the root container
        String name = extractContainerName(containerIri);
        String annoId = "inexistentId";
        String requestPath = name + "/" + annoId;
        // Valid container, invalid anno
        RequestSpecification request = RestAssured.given();
        Response response = request.get(requestPath);
        assertNotNull(response, "Could not get annotation response");
        checkException(NotExistentException.class, response);
        // Invalid container
        request = RestAssured.given();
        response = request.get("00" + requestPath);
        assertNotNull(response, "Could not get annotation response");
        checkException(NotExistentException.class, response);
    }

    /**
     * Test get annotation with invalid accepts.
     */
    @Test
    public void testGetAnnotationWithInvalidAccepts() {
        String containerIri = createDefaultContainer(null); // in the root container
        String name = extractContainerName(containerIri);
        String annoIri = createDefaultAnnotation(name + "/");
        String annoId = getAnnotationId(annoIri);
        String requestPath = name + "/" + annoId;
        // With invalid accept, we expect JSON-LD with default anno profile
        RequestSpecification request = RestAssured.given();
        request.accept("text/html");
        Response response = request.get(requestPath);
        assertNotNull(response, "Could not get get response");
        // System.err.println("Response = " + response.asString());
        assertEquals(AnnotationConstants.GET_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                "Annotation could not be fetched");
        checkHeader(response, "Content-Type", getDefaultFormatString(Type.ANNOTATION));
        checkHeaderExists(response, "Etag");
        checkHeader(response, "Link", AnnotationConstants.LINK_HEADER);
        for (String method : AnnotationConstants.VARY_LIST) {
            checkHeaderContains(response, "Vary", method);
        }
        for (HttpMethod method : AnnotationConstants.ALLOWED_METHODS) {
            checkHeaderContains(response, "Allow", method.toString());
        }
        // now have a typo
        request = RestAssured.given();
        request.accept("application/ld-json");
        response = request.get(requestPath);
        assertNotNull(response, "Could not get get response");
        // System.err.println("Response = " + response.asString());
        assertEquals(AnnotationConstants.GET_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                "Annotation could not be fetched");
        checkHeader(response, "Content-Type", getDefaultFormatString(Type.ANNOTATION));
        checkHeaderExists(response, "Etag");
        checkHeader(response, "Link", AnnotationConstants.LINK_HEADER);
        for (String method : AnnotationConstants.VARY_LIST) {
            checkHeaderContains(response, "Vary", method);
        }
        for (HttpMethod method : AnnotationConstants.ALLOWED_METHODS) {
            checkHeaderContains(response, "Allow", method.toString());
        }
    }

    /**
     * Test get annotation all formats.
     */
    @Test
    public void testGetAnnotationAllFormats() {
        testReadAnnotationAllFormats(HttpMethod.GET, false);
    }

    /**
     * Test head annotation all formats.
     */
    @Test
    public void testHeadAnnotationAllFormats() {
        testReadAnnotationAllFormats(HttpMethod.HEAD, false);
    }

    /**
     * Test options annotation all formats.
     */
    @Test
    public void testOptionsAnnotationAllFormats() {
        testReadAnnotationAllFormats(HttpMethod.OPTIONS, false);
    }

    /**
     * Test cors annotation all formats.
     */
    @Test
    public void testCorsAnnotationAllFormats() {
        testReadAnnotationAllFormats(HttpMethod.OPTIONS, true);
    }

    private void testReadAnnotationAllFormats(HttpMethod httpMethod, boolean cors) {
        String containerIri = createDefaultContainer(null); // in the root container
        String name = extractContainerName(containerIri);
        String annoIri = createDefaultAnnotation(name + "/");
        String annoId = getAnnotationId(annoIri);
        String requestPath = name + "/" + annoId;
        int expectedCode = AnnotationConstants.GET_ANNOTATION_SUCCESS_CODE;
        assertNotEqual(-1, expectedCode, "Unallowed http method : " + httpMethod.toString());
        // Get one with the JSON-LD default settings using the anno profile
        {
            RequestSpecification request = RestAssured.given();
            if (cors) {
                request.header("Origin", "http://allowed.org");
            }
            Response response = httpMethod == HttpMethod.GET ? request.get(requestPath)
                    : httpMethod == HttpMethod.HEAD ? request.head(requestPath)
                            : httpMethod == HttpMethod.OPTIONS ? request.options(requestPath) : null;
            assertNotNull(response, "Could not get get response");
            // System.err.println("Response = " + response.asString());
            assertEquals(expectedCode, response.getStatusCode(), "Annotation could not be fetched");
            if (httpMethod != HttpMethod.OPTIONS) {
                checkHeader(response, "Content-Type", getDefaultFormatString(Type.ANNOTATION));
                checkHeaderExists(response, "ETag");
            }
            if (httpMethod == HttpMethod.GET) {
                // is the IRI identical to the embedded id
                checkIriMatchesId(response, annoIri);
                // is there a target
                this.checkProperty(response, "target");
                // is the type annotaton ?
                checkProperty(response, "type", "Annotation");
                // are modified and created there?
                this.checkProperty(response, "modified");
                this.checkProperty(response, "created");
            }
            checkHeader(response, "Link", AnnotationConstants.LINK_HEADER);
            for (String method : AnnotationConstants.VARY_LIST) {
                checkHeaderContains(response, "Vary", method);
            }
            for (HttpMethod method : AnnotationConstants.ALLOWED_METHODS) {
                checkHeaderContains(response, "Allow", method.toString());
            }
        }
        for (String formatString : getFormatRegistry().getFormatStrings()) {
            // logger.trace("Getting : " + formatString);
            if (WapServerConfig.getInstance().shouldAlwaysAddDefaultProfilesToJsonLdRequests()) {
                // When we always add the anno profile, this case has already been tested
                // and the second test here would neither work nor be necessary anymore
                if (formatString.startsWith("application/ld+json")) {
                    continue;
                }
            }
            RequestSpecification request = RestAssured.given();
            if (cors) {
                request.header("Origin", "http://allowed.org");
            }
            request.accept(formatString);
            Response response = httpMethod == HttpMethod.GET ? request.get(requestPath)
                    : httpMethod == HttpMethod.HEAD ? request.head(requestPath)
                            : httpMethod == HttpMethod.OPTIONS ? request.options(requestPath) : null;
            assertNotNull(response, "Could not get get response");
            // logger.trace(response.asString());
            assertEquals(expectedCode, response.getStatusCode(), "Annotation could not be fetched");
            if (httpMethod != HttpMethod.OPTIONS) {
                checkHeader(response, "Content-Type", formatString);
                checkHeaderExists(response, "ETag");
            }
            checkHeader(response, "Link", AnnotationConstants.LINK_HEADER);
            for (String method : AnnotationConstants.VARY_LIST) {
                checkHeaderContains(response, "Vary", method);
            }
            for (HttpMethod method : AnnotationConstants.ALLOWED_METHODS) {
                checkHeaderContains(response, "Allow", method.toString());
            }
        }
    }

    private void checkIriMatchesId(Response response, String annoIri) {
        this.checkProperty(response, "id", annoIri);
    }

    /**
     * Test put valid annotations.
     */
    @Test
    public void testPutValidAnnotations() {
        String[] validKeys = TestDataStore.listAnnotations();
        List<String> withErrors = new Vector<String>();
        for (String key : validKeys) {
            logger.trace("Testing put valid annotation " + key);
            String annotation = TestDataStore.getAnnotation(key);
            RequestSpecification request = RestAssured.given();
            request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.body(annotation);
            Response response = postAnnotation(request); // posts to the default test container
            assertNotNull(response, "Could not get response");
            if (response.getStatusCode() != AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE) {
                System.err.println("Posting not successful of this anno : " + key + " \n" + annotation);
                System.err.println(response.getBody().asString());
                withErrors.add(key);
                continue;
            }
            annotation = response.body().asString();
            String etag = getEtag(response);
            String id = getAnnotationId(response);
            // 2. update it
            request = RestAssured.given();
            request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.header("If-Match", etag);
            request.body(annotation);
            Response putResponse = putAnnotation(request, id);
            assertNotNull(putResponse, "Could not get put response");
            if (putResponse.getStatusCode() != AnnotationConstants.PUT_ANNOTATION_SUCCESS_CODE) {
                System.err.println("Puting not successful of this anno : " + key);
                System.err.println(" Orig:\n" + TestDataStore.getAnnotation(key));
                System.err.println("  PUT:\n" + annotation);
                System.err.println("Error:\n" + putResponse.getBody().asString());
                withErrors.add(key);
                continue;
            }
        }
        if (!withErrors.isEmpty()) {
            fail("Puting not successful of these annos : " + withErrors);
        }
    }

    /**
     * Test put annotation.
     */
    @Test
    public void testPutAnnotation() {
        // 1. create one
        String annotation = getRandomAnnotation();
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response postResponse = postAnnotation(request);
        assertNotNull(postResponse, "Could not get post response");
        assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, postResponse.getStatusCode(),
                "Annotation could not be created");
        annotation = postResponse.body().asString();
        String etag = getEtag(postResponse);
        String id = getAnnotationId(postResponse);
        // 2. update it
        request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.header("If-Match", etag);
        request.body(annotation);
        Response putResponse = putAnnotation(request, id);
        assertNotNull(putResponse, "Could not get put response");
        assertEquals(AnnotationConstants.PUT_ANNOTATION_SUCCESS_CODE, putResponse.getStatusCode(),
                "Annotation could not be updated");
    }

    /**
     * Test put annotation all but JSON-LD.
     */
    @Test
    public void testPutAnnotationAllButJsonLd() {
        // 1. create one
        String annotationString = getAnnotation(1);
        assertNotNull(annotationString, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotationString);
        Response postResponse = postAnnotation(request);
        assertNotNull(postResponse, "Could not get post response");
        assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, postResponse.getStatusCode(),
                "Annotation could not be created");
        final String annotationJsonLd = postResponse.body().asString();
        final String annotationIri = getIri(postResponse);
        final String annotationPath = getPathFromIri(annotationIri);
        // The annotation has a body like this, assert that
        // "body" : "http://example.org/post1"
        String annoBodyProperty = postResponse.then().extract().path("body");
        assertEquals("http://example.org/post1", annoBodyProperty, "annotation has not the expected body property");
        // Now we can just change the number at the end one after the other to see update works
        int counter = 2;
        for (String formatString : getFormatRegistry().getFormatStrings()) {
            if (formatString.startsWith("application/ld+json")) {
                continue;
            }
            logger.trace("Putting annotation using " + formatString);
            Formatter formatter = null;
            try {
                formatter = getFormatRegistry().getFormatter(formatString);
            } catch (InternalServerException | NullPointerException e) {
                fail(e.getMessage());
            }
            Format format = formatter.getFormat();
            String newBodyValue = "http://example.org/post" + counter;
            String convertedAnnotation = this.convertFormat(
                    annotationJsonLd.replaceAll(Pattern.quote("http://example.org/post1"), newBodyValue), format);
            // logger.trace("Format " + formatter.getContentType());
            // System.out.print(convertedAnnotation);
            String etag = getEtagFromServer(annotationPath);
            // update the body and then the annotation
            request = RestAssured.given();
            request.config(RestAssured.config()
                    .encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs(formatString, ContentType.TEXT)));
            request.contentType(formatter.getContentType());
            request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.body(convertedAnnotation);
            request.header("If-Match", etag);
            Response putResponse = request.put(annotationPath);
            assertNotNull(putResponse, "Could not get put response");
            assertEquals(AnnotationConstants.PUT_ANNOTATION_SUCCESS_CODE, putResponse.getStatusCode(),
                    "Annotation could not be updated");
            // logger.trace(putResponse.getBody().asString());
            this.checkProperty(putResponse, "body", newBodyValue);
            counter++;
        }
    }

    /**
     * Test put annotation with blank iri.
     */
    @Test
    public void testPutAnnotationWithBlankIri() {
        // 1. create one
        String annotation = getAnnotation(46); // 46 has no id ==> blank node iri
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response postResponse = postAnnotation(request);
        assertNotNull(postResponse, "Could not get post response");
        assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, postResponse.getStatusCode(),
                "Annotation could not be created");
        annotation = postResponse.body().asString();
        String etag = getEtag(postResponse);
        String id = getAnnotationId(postResponse);
        // 2. update it
        request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.header("If-Match", etag);
        request.body(annotation);
        Response putResponse = putAnnotation(request, id);
        assertNotNull(putResponse, "Could not get put response");
        assertEquals(AnnotationConstants.PUT_ANNOTATION_SUCCESS_CODE, putResponse.getStatusCode(),
                "Annotation could not be updated");
    }

    /**
     * Test put annotation with params.
     */
    @Test
    public void testPutAnnotationWithParams() {
        if (WapServerConfig.getInstance().isHttpsEnabled()) {
            if (FAIL_ON_UNSUPPORTED_TESTS) {
                fail("No https support in own connections");
            }
            return;
        }
        String annotation = getRandomAnnotation();
        // The annotation does not have to exist for this test
        String containerIri = createDefaultContainer(null); // in the root container
        try {
            URL url = new URL(containerIri);
            String params = "test=1";
            Map<String, Object> requestHeaders = new Hashtable<String, Object>();
            requestHeaders.put("Content-Type", "application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            requestHeaders.put("If-Match", "\"etag\"");
            requestHeaders.put("Accept", "application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            OwnResponse response
                    = performOwnHttpRequest(url, OwnHttpURLConnection.Request.PUT, requestHeaders, params, annotation);
            // logger.trace(response.getTransmittedString());
            // logger.trace(response.getReceivedString());
            checkException(IllegalHttpParameterException.class, response, ErrorMessageRegistry.ALL_NO_PARAMETERS_IN_PUT);
        } catch (MalformedURLException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test put annotation without content type.
     */
    @Test
    public void testPutAnnotationWithoutContentType() {
        String annotation = getRandomAnnotation();
        // The annotation does not have to exist for this test
        RequestSpecification request = RestAssured.given();
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.header("If-Match", ETAG_EXAMPLE);
        request.body(annotation);
        Response response = request.put("container1/" + System.currentTimeMillis());
        assertNotNull(response, "Could not get annotation response");
        // System.err.println(response.getBody().asString());
        // we cannot leave the content type header away with rest assured
        // it is autocreated to text/plain then. Therefore check for FormatException
        // until either self implement this type of request or rest assured can some time
        // send invalid requests
        checkException(FormatException.class, response, ErrorMessageRegistry.ALL_UNKNOWN_INPUT_FORMAT);
        // checkException(HttpHeaderException.class, response);
    }

    /**
     * Test put annotation with invalid content types.
     */
    @Test
    public void testPutAnnotationWithInvalidContentTypes() {
        String annotation = getRandomAnnotation();
        // The annotation does not have to exist for this test
        // html can never be puttet
        RequestSpecification request = RestAssured.given();
        request.contentType("text/html");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.header("If-Match", ETAG_EXAMPLE);
        request.body(annotation);
        Response response = request.put("container1/" + System.currentTimeMillis());
        assertNotNull(response, "Could not get annotation response");
        // System.err.println(response.getBody().asString());
        checkException(FormatException.class, response, ErrorMessageRegistry.ALL_UNKNOWN_INPUT_FORMAT);
    }

    /**
     * Test put annotation without etag.
     */
    @Test
    public void testPutAnnotationWithoutEtag() {
        String annotation = getRandomAnnotation();
        // The annotation does not have to exist for this test
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response response = request.put("container1/" + System.currentTimeMillis());
        assertNotNull(response, "Could not get annotation response");
        // logger.trace(response.getBody().asString());
        checkException(HttpHeaderException.class, response);
    }

    /**
     * Test put annotation wrong id.
     */
    @Test
    public void testPutAnnotationWrongId() {
        // 1. create one
        String annotation = getRandomAnnotation();
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response postResponse = postAnnotation(request);
        assertNotNull(postResponse, "Could not get post response");
        assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, postResponse.getStatusCode(),
                "Annotation could not be created");
        annotation = postResponse.body().asString();
        String etag = getEtag(postResponse);
        String id = getAnnotationId(postResponse);
        // 2. update it
        request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.header("If-Match", etag);
        request.body(annotation);
        Response putResponse = putAnnotation(request, id);
        assertNotNull(putResponse, "Could not get put response");
        assertEquals(AnnotationConstants.PUT_ANNOTATION_SUCCESS_CODE, putResponse.getStatusCode(),
                "Annotation could not be updated");
    }

    /**
     * Test etag changes all causes.
     */
    @Test
    public void testEtagChangesAllCauses() {
        final String containerIri = createDefaultContainer(null);
        final String path = getPathFromIri(containerIri);
        // add anno
        String annotation = getRandomAnnotation();
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
        final String etagBefore = getEtag(postResponse);
        final String etagBeforeFromServer = getEtagFromServer(annoPath);
        if (!etagBefore.equals(etagBeforeFromServer)) {
            fail("ETag in post response does not match etag in db");
        }
        // put anno
        String annoEtag = getEtagFromServer(annoPath);
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.header("If-Match", annoEtag);
        request.body(annotation);
        Response putResponse = request.put(annoPath);
        assertNotNull(putResponse, "Could not get put response");
        assertEquals(AnnotationConstants.PUT_ANNOTATION_SUCCESS_CODE, putResponse.getStatusCode(),
                "Annotation could not be updated");
        final String etagAfter = getEtagFromServer(annoPath);
        assertNotEqual(etagBefore, etagAfter, "ETags did not change through anno update");
    }

    /**
     * Test delete annotation.
     */
    @Test
    public void testDeleteAnnotation() {
        // 1. create one
        String annotation = getRandomAnnotation();
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response postResponse = postAnnotation(request);
        assertNotNull(postResponse, "Could not get post response");
        assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, postResponse.getStatusCode(),
                "Annotation could not be created");
        annotation = postResponse.body().asString();
        String etag = getEtag(postResponse);
        String id = getAnnotationId(postResponse);
        // 2. delete it
        request = RestAssured.given();
        request.header("If-Match", etag);
        Response deleteResponse = deleteAnnotation(request, id);
        assertNotNull(deleteResponse, "Could not get delete response");
        assertEquals(AnnotationConstants.DELETE_ANNOTATION_SUCCESS_CODE, deleteResponse.getStatusCode(),
                "Annotation could not be deleted");
    }

    private Response deleteAnnotation(RequestSpecification request, String id) {
        return request.delete(testContainer + "/" + id);
    }

    @Override
    protected String getRandomAnnotation() {
        return getAnnotation(1); // remove this method if testing should use all
    }

    private Response postAnnotation(RequestSpecification request) {
        return request.post(testContainer + "/");
    }

    private Response putAnnotation(RequestSpecification request, String id) {
        return request.put(testContainer + "/" + id);
    }

    /**
     * Test post valid annotations.
     */
    @Test
    public void testPostValidAnnotations() {
        String[] validKeys = TestDataStore.listAnnotations();
        List<String> withErrors = new Vector<String>();
        for (String key : validKeys) {
            logger.trace("Testing post valid annotation " + key);
            String annotation = TestDataStore.getAnnotation(key);
            RequestSpecification request = RestAssured.given();
            request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.body(annotation);
            Response response = postAnnotation(request); // posts to the default test container
            assertNotNull(response, "Could not get response");
            if (response.getStatusCode() != AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE) {
                System.err.println("Posting not successful of this anno : " + key + " \n" + annotation);
                withErrors.add(key);
            }
        }
        if (!withErrors.isEmpty()) {
            fail("Posting not successful of these annos : " + withErrors);
        }
    }

    /**
     * Test post invalid annotations.
     */
    @Test
    public void testPostInvalidAnnotations() {
        Set<String> invalidKeys = TestDataStore.getInvalidAnnotationKeys();
        List<String> withErrors = new Vector<String>();
        for (String key : invalidKeys) {
            logger.trace("Testing post invalid annotation " + key);
            String annotation = TestDataStore.getAnnotation(key);
            RequestSpecification request = RestAssured.given();
            request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.body(annotation);
            Response response = postAnnotation(request); // posts to the default test container
            assertNotNull(response, "Could not get response");
            if (response.getStatusCode() == AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE) {
                System.err.println("Posting successful of this anno : " + key + " \n" + annotation);
                withErrors.add(key);
            }
            // Different type of errors occur depending on the failures within the annotation
            // the only common thing is not success as code
            // checkException(NotAnAnnotationException.class, response);
        }
        if (!withErrors.isEmpty()) {
            fail("Posting successful of these annos : " + withErrors);
        }
    }

    /**
     * Test put invalid annotations.
     */
    @Test
    public void testPutInvalidAnnotations() {
        String annoIri = createDefaultAnnotation(testContainer + "/");
        String annoId = getAnnotationId(annoIri);
        String etag = getEtagFromServer(annoIri);
        Set<String> invalidKeys = TestDataStore.getInvalidAnnotationKeys();
        for (String key : invalidKeys) {
            logger.trace("Testing put invalid annotation " + key);
            String annotation = TestDataStore.getAnnotation(key);
            RequestSpecification request = RestAssured.given();
            request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
            request.header("If-Match", etag);
            request.body(annotation);
            Response response = request.put(testContainer + "/" + annoId);
            assertNotNull(response, "Could not get response");
            if (response.getStatusCode() == AnnotationConstants.PUT_ANNOTATION_SUCCESS_CODE) {
                logger.trace("Putting successful of this anno : \n" + annotation);
                fail("Putting successful of this anno : " + key);
            }
            // logger.trace(response.body().asString());
            // Different type of errors occur depending on the failures within the annotation
            // the only common thing is not success as code
            // checkException(NotAnAnnotationException.class, response);
        }
    }

    /**
     * Test put annotation with unallowed changes.
     */
    @Test
    public void testPutAnnotationWithUnallowedChanges() {
        String annotation = getAnnotation(50); // The one holds a canonical and a via value
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response response = postAnnotation(request);
        assertNotNull(response, "Could not get response");
        assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                "Annotation could not be created");
        final String annoInDb = response.getBody().asString();
        String etag = getEtag(response);
        final String realId = getAnnotationId(response);
        // post with a different id
        String annoWithDifferentId = annoInDb.replaceAll(Pattern.quote(realId), "0" + realId);
        request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.header("If-Match", etag);
        request.body(annoWithDifferentId);
        // logger.trace("from : \n" + annoInDb + "\nto :\n" + annoWithDifferentId);
        Response putResponse = putAnnotation(request, realId); // the real id, only within json wrong
        assertNotNull(putResponse, "Could not get put response");
        checkException(UnallowedPropertyChangeException.class, putResponse);
        // post with a different canonical
        // the canonical value is : http://www.should-not-be-changed.de
        String annoWithDifferentCanonical = annoInDb.replaceAll(Pattern.quote("-changed"), "");
        request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.header("If-Match", etag);
        request.body(annoWithDifferentCanonical);
        // logger.trace("from : \n" + annoInDb + "\nto :\n" + annoWithDifferentCanonical);
        putResponse = putAnnotation(request, realId); // the real id, only within json wrong
        assertNotNull(putResponse, "Could not get put response");
        checkException(UnallowedPropertyChangeException.class, putResponse);
        // post with a different via
        // the via value is : http://www.leave-via-alone.de
        String annoWithDifferentVia = annoInDb.replaceAll(Pattern.quote("-alone"), "");
        request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.header("If-Match", etag);
        request.body(annoWithDifferentVia);
        logger.trace("from : \n" + annoInDb + "\nto :\n" + annoWithDifferentVia);
        putResponse = putAnnotation(request, realId); // the real id, only within json wrong
        assertNotNull(putResponse, "Could not get put response");
        checkException(UnallowedPropertyChangeException.class, putResponse);
    }

    /**
     * Test posting annotation with a series of multiple escaped characters.
     * @throws JSONException
     */
    @Test
    public void testPostAnnoWithMultipleEscapes() throws JSONException {
        String annotation = getAnnotation(101);
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response response = postAnnotation(request);
        assertNotNull(response, "Could not get response");
        assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                "Annotation could not be created");
        String annoInDb = response.getBody().asString();

        JSONObject expectedAnno = new JSONObject(annotation);
        JSONObject actualAnno = new JSONObject(annoInDb);
        //checks for existence of all values in the posted annotation payload but ignores additional fields (like 'id' or 'created')
        JSONAssert.assertEquals(expectedAnno, actualAnno, JSONCompareMode.LENIENT);

    }
}
