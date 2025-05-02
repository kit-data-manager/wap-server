package edu.kit.scc.dem.wapsrv.testsrest;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.apache.jena.fuseki.servlets.CrossOriginFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import edu.kit.scc.dem.wapsrv.app.FusekiRunner;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.controller.AnnotationConstants;
import edu.kit.scc.dem.wapsrv.controller.ContainerConstants;
import edu.kit.scc.dem.wapsrv.controller.PageConstants;
import edu.kit.scc.dem.wapsrv.exceptions.GlobalErrorCodes;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * CorsRestTests that test the CORS functionality including preflight requests
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
public class CorsRestTests extends AbstractRestTest {

    private static final Logger logger = LoggerFactory.getLogger(CorsRestTests.class);

    /**
     * Create a new CorsRestTest
     */
    protected CorsRestTests() {
        super(true);
    }

    /**
     * Set a lock.
     */
    @BeforeEach
    public void getLock() {
        lock();
        logger.trace("Running test");
    }

    /**
     * Free the lock.
     */
    @AfterEach
    public void freeLock() {
        logger.trace("Finished test");
        // Reset restassured, it may have been changed in some tests
        RestAssured.baseURI = WapServerConfig.getInstance().getRootContainerIri();
        RestAssured.port = WapServerConfig.getInstance().getWapPort();
        unlock();
    }

    /**
     * Tests sparql cors preflight behavior with allowed origin on readonly
     * instance
     */
    @Test
    public void sparqlReadonlyAllowedCorsPreflightTest() {
        sparqlAllowedCorsPreflightTest(
                "http://localhost:" + WapServerConfig.getInstance().getSparqlReadPort() + FusekiRunner.ENDPOINT_PREFIX,
                WapServerConfig.getInstance().getSparqlReadPort());
    }

    /**
     * Tests sparql cors preflight behavior with not allowed origin on readonly
     * instance
     */
    @Test
    public void sparqlReadonlyNotAllowedCorsPreflightTest() {
        sparqlNotAllowedCorsPreflightTest(
                "http://localhost:" + WapServerConfig.getInstance().getSparqlReadPort() + FusekiRunner.ENDPOINT_PREFIX,
                WapServerConfig.getInstance().getSparqlReadPort());
    }

    /**
     * Tests sparql cors preflight behavior with not allowed origin on
     * read-write instance
     */
    @Test
    public void sparqlReadWriteNotAllowedCorsPreflightTest() {
        sparqlNotAllowedCorsPreflightTest(
                "http://localhost:" + WapServerConfig.getInstance().getSparqlWritePort() + FusekiRunner.ENDPOINT_PREFIX,
                WapServerConfig.getInstance().getSparqlWritePort());
    }

    /**
     * Tests sparql cors preflight behavior with allowed origin on read-write
     * instance
     */
    @Test
    public void sparqlReadWriteAllowedCorsPreflightTest() {
        sparqlAllowedCorsPreflightTest(
                "http://localhost:" + WapServerConfig.getInstance().getSparqlWritePort() + FusekiRunner.ENDPOINT_PREFIX,
                WapServerConfig.getInstance().getSparqlWritePort());
    }

    /**
     * Tests sparql cors Preflight behavior with allowed origin and given
     * parameters
     *
     * @param baseURI The base URI to use
     * @param port The port to use
     */
    private void sparqlAllowedCorsPreflightTest(String baseURI, int port) {
        RestAssured.baseURI = baseURI;
        RestAssured.port = port;
        final String originAllowed = "http://allowed.org";
        RequestSpecification request = RestAssured.given();
        request.header("Origin", originAllowed);
        request.header(CrossOriginFilter.ACCESS_CONTROL_REQUEST_HEADERS_HEADER, "Link,Content-Type,ETag");
        request.header(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, "POST");
        Response response = request.options("/sparql");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        checkCorsPreflightHeaders(request, response, originAllowed, true);
    }

    /**
     * Tests sparql cors Preflight behavior with not allowed origin and given
     * parameters
     *
     * @param baseURI The base URI to use
     * @param port The port to use
     */
    private void sparqlNotAllowedCorsPreflightTest(String baseURI, int port) {
        // Fuseki always accepts a request, therefore 200 is the return code in any case.
        // The only difference is that when using CORS then the Access-Control... headers are
        // not added in case the Origin and other parameters are not acceptable
        RestAssured.baseURI = baseURI;
        RestAssured.port = port;
        final String originNotAllowed = "http://forbidden.org";
        RequestSpecification request = RestAssured.given();
        request.header("Origin", originNotAllowed);
        Response response = request.options("/sparql");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }

    /**
     * Tests sparql cors behavior with allowed origin on readonly instance
     */
    @Test
    public void sparqlReadonlyAllowedCorsTest() {
        sparqlAllowedCorsTest(
                "http://localhost:" + WapServerConfig.getInstance().getSparqlReadPort() + FusekiRunner.ENDPOINT_PREFIX,
                WapServerConfig.getInstance().getSparqlReadPort());
    }

    /**
     * Tests sparql cors behavior with not allowed origin on readonly instance
     */
    @Test
    public void sparqlReadonlyNotAllowedCorsTest() {
        sparqlNotAllowedCorsTest(
                "http://localhost:" + WapServerConfig.getInstance().getSparqlReadPort() + FusekiRunner.ENDPOINT_PREFIX,
                WapServerConfig.getInstance().getSparqlReadPort());
    }

    /**
     * Tests sparql cors behavior with not allowed origin on read-write instance
     */
    @Test
    public void sparqlReadWriteNotAllowedCorsTest() {
        sparqlNotAllowedCorsTest(
                "http://localhost:" + WapServerConfig.getInstance().getSparqlWritePort() + FusekiRunner.ENDPOINT_PREFIX,
                WapServerConfig.getInstance().getSparqlWritePort());
    }

    /**
     * Tests sparql cors behavior with allowed origin on read-write instance
     */
    @Test
    public void sparqlReadWriteAllowedCorsTest() {
        sparqlAllowedCorsTest(
                "http://localhost:" + WapServerConfig.getInstance().getSparqlWritePort() + FusekiRunner.ENDPOINT_PREFIX,
                WapServerConfig.getInstance().getSparqlWritePort());
    }

    /**
     * Tests sparql cors behavior with allowed origin and given parameters
     *
     * @param baseURI The base URI to use
     * @param port The port to use
     */
    private void sparqlAllowedCorsTest(String baseURI, int port) {
        RestAssured.baseURI = baseURI;
        RestAssured.port = port;
        // The requests results in no sensible answer, we only check the headers
        final String sparqlRequest = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\r\n" + "SELECT ?name\r\n" + "WHERE {\r\n"
                + "    ?person foaf:name ?name .\r\n" + "}";
        // First using POST
        final String originAllowed = "http://allowed.org";
        RequestSpecification request = RestAssured.given();
        request.config(RestAssured.config().encoderConfig(
                EncoderConfig.encoderConfig().encodeContentTypeAs("application/sparql-query", ContentType.TEXT)));
        request.header("Origin", originAllowed);
        request.header("Content-Type", "application/sparql-query");
        request.body(sparqlRequest);
        Response response = request.post("/sparql");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        checkCorsHeaders(response, originAllowed);
        // Now retry using GET
        request = RestAssured.given();
        request.header("Origin", originAllowed);
        request.param("query", sparqlRequest);
        response = request.get("/sparql");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        checkCorsHeaders(response, originAllowed);
    }

    /**
     * Tests sparql cors behavior with not allowed origin and given parameters
     *
     * @param baseURI The base URI to use
     * @param port The port to use
     */
    private void sparqlNotAllowedCorsTest(String baseURI, int port) {
        // Fuseki always accepts a request, therefore 200 is the return code in any case.
        // The only difference is that when using CORS then the Access-Control... headers are
        // not added in case the Origin and other parameters are not acceptable
        RestAssured.baseURI = baseURI;
        RestAssured.port = port;
        // The requests results in no sensible answer, we only check the headers
        final String sparqlRequest = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\r\n" + "SELECT ?name\r\n" + "WHERE {\r\n"
                + "    ?person foaf:name ?name .\r\n" + "}";
        // First request using POST
        final String originNotAllowed = "http://forbidden.org";
        RequestSpecification request = RestAssured.given();
        request.config(RestAssured.config().encoderConfig(
                EncoderConfig.encoderConfig().encodeContentTypeAs("application/sparql-query", ContentType.TEXT)));
        request.header("Origin", originNotAllowed);
        request.header("Content-Type", "application/sparql-query");
        request.body(sparqlRequest);
        Response response = request.post("/sparql");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
        // Now retry using GET
        request = RestAssured.given();
        request.header("Origin", originNotAllowed);
        request.param("query", sparqlRequest);
        response = request.get("/sparql");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }

    /**
     * Global CORS Preflight Tests, individual tests can be found in the
     * specific controller test classes. This one is just to specifically test
     * the CORS preflight headers
     */
    @Test
    public void globalCorsPreflightTest() {
        // Create container and anno for testing
        final String containerIri = createDefaultContainer(null); // in the root container
        final String containerPath = getPathFromIri(containerIri);
        final String annoIri = createDefaultAnnotation(containerPath);
        final String annoPath = getPathFromIri(annoIri);
        // We test only the preflight requests via options here
        preflightFromAllowedCorsOrigin(containerPath, annoPath);
        preflightFromForbiddenCorsOrigin(containerPath, annoPath);
    }

    /**
     * Tests preflight requests with a not allowed origin
     *
     * @param containerPath The container path to use (for container and page
     * requests)
     * @param annoPath The annotation path to use
     */
    private void preflightFromForbiddenCorsOrigin(String containerPath, String annoPath) {
        final String origin = "http://forbidden.org";
        // Check container preflight
        RequestSpecification request = RestAssured.given();
        request.header("Origin", origin);
        Response response = request.options(containerPath);
        assertNotNull(response);
        assertEquals(GlobalErrorCodes.INVALID_CREDENTIALS, response.getStatusCode());
        // Check page preflight
        request = RestAssured.given();
        request.header("Origin", origin);
        response = request.options(containerPath + "?iris=1&page=0");
        assertNotNull(response);
        assertEquals(GlobalErrorCodes.INVALID_CREDENTIALS, response.getStatusCode());
        // check anno preflight
        request = RestAssured.given();
        request.header("Origin", origin);
        response = request.options(annoPath);
        assertNotNull(response);
        assertEquals(GlobalErrorCodes.INVALID_CREDENTIALS, response.getStatusCode());
    }

    /**
     * Tests preflight requests with an allowed origin
     *
     * @param containerPath The container path to use (for container and page
     * requests)
     * @param annoPath The annotation path to use
     */
    private void preflightFromAllowedCorsOrigin(String containerPath, String annoPath) {
        // The requested headers are somehow random. In sum all of the available headers are used
        final String origin = "http://allowed.org";
        // Check container preflight
        for (HttpMethod method : ContainerConstants.ALLOWED_METHODS) {
            RequestSpecification request = RestAssured.given();
            request.header("Origin", origin);
            request.header(CrossOriginFilter.ACCESS_CONTROL_REQUEST_HEADERS_HEADER, "Link,Content-Type,ETag");
            request.header(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, method.toString());
            Response response = request.options(containerPath);
            assertNotNull(response);
            assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode());
            checkCorsPreflightHeaders(request, response, origin, false);
        }
        // Check page preflight
        for (HttpMethod method : PageConstants.ALLOWED_METHODS) {
            RequestSpecification request = RestAssured.given();
            request.header("Origin", origin);
            request.header(CrossOriginFilter.ACCESS_CONTROL_REQUEST_HEADERS_HEADER, "Date,Vary,Allow");
            request.header(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, method.toString());
            Response response = request.options(containerPath + "?iris=1&page=0");
            assertNotNull(response);
            assertEquals(PageConstants.GET_PAGE_SUCCESS_CODE, response.getStatusCode());
            checkCorsPreflightHeaders(request, response, origin, false);
        }
        // Check anno preflight
        for (HttpMethod method : AnnotationConstants.ALLOWED_METHODS) {
            RequestSpecification request = RestAssured.given();
            request.header("Origin", origin);
            request.header(CrossOriginFilter.ACCESS_CONTROL_REQUEST_HEADERS_HEADER,
                    "Content-Location,Content-Length,Location,Connection");
            request.header(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER, method.toString());
            Response response = request.options(annoPath);
            assertNotNull(response);
            assertEquals(AnnotationConstants.GET_ANNOTATION_SUCCESS_CODE, response.getStatusCode());
            checkCorsPreflightHeaders(request, response, origin, false);
        }
    }

    /**
     * Checks that all preflight headers exist as expected
     *
     * @param request The request send
     * @param response The response received
     * @param origin The Origin header used
     * @param sparql If the request was a sparql request
     */
    private void checkCorsPreflightHeaders(RequestSpecification request, Response response, String origin,
            boolean sparql) {
        // We do the usual checks
        checkCorsHeaders(response, origin);
        Headers requestHeaders = ((io.restassured.internal.RequestSpecificationImpl) request).getHeaders();
        if (sparql) {
            // we should have the same in allow and allow-methods
            assertEquals(response.getHeader("Allow"),
                    response.getHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER));
        } else {
            // The controllers via Spring do not call the real methods so far. The Cors filter
            // intercepts the call when it is a preflight request. This way no "real" headers have
            // been added and we do not have the ability to check which methods would finally succedd
            // Spring just copies was was requested to allowed and that's it. Therefore we can only check
            // this has happened
            Header requestMethodHeader = requestHeaders.get(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER);
            if (requestMethodHeader != null) {
                String requestedMethod = requestMethodHeader.getValue().trim();
                // Check we have the corresponding answer headers
                assertNotNull(response.getHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER));
                List<String> allowedMethodsList
                        = Arrays.asList(response.getHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER)
                                .replaceAll(Pattern.quote(" "), "").split(Pattern.quote(",")));
                boolean contained = allowedMethodsList.contains(requestedMethod);
                if (!contained) {
                    System.err.println(response.getHeaders().toString());
                }
                assertTrue(contained, "Method not allowed though preflight-requested : " + requestedMethod);
            } else {
                // Without a request method header, we are strictly speaking not a preflight request...
            }
        }
        // and allowed headers
        Header requestHeadersHeader = requestHeaders.get(CrossOriginFilter.ACCESS_CONTROL_REQUEST_HEADERS_HEADER);
        if (requestHeadersHeader != null) {
            List<String> requestedHeadersList = Arrays
                    .asList(requestHeadersHeader.getValue().replaceAll(Pattern.quote(" "), "").split(Pattern.quote(",")));
            // Check we have the corresponding answer headers
            assertNotNull(response.getHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER));
            List<String> allowedHeadersList
                    = Arrays.asList(response.getHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER)
                            .replaceAll(Pattern.quote(" "), "").split(Pattern.quote(",")));
            for (String header : requestedHeadersList) {
                boolean contained = allowedHeadersList.contains(header);
                if (!contained) {
                    System.err.println(response.getHeaders().toString());
                }
                assertTrue(contained, "Header not allowed though preflight-requested : " + header);
            }
        }
    }

    /**
     * Global CORS Tests, individual tests can be found in the specific
     * controller test classes. This one is just to specifically test the CORS
     * headers
     */
    @Test
    public void globalCorsTest() {
        // Create container and anno for testing
        final String containerIri = createDefaultContainer(null); // in the root container
        final String containerPath = getPathFromIri(containerIri);
        final String annoIri = createDefaultAnnotation(containerPath);
        final String annoPath = getPathFromIri(annoIri);
        // Beside the separate prefligt tests via options, we only test using get.
        // Spring uses the same CORS rules for all other methods, therefore testing one is sufficient
        getFromAllowedCorsOrigin(containerPath, annoPath);
        getFromForbiddenCorsOrigin(containerPath, annoPath);
    }

    /**
     * Tests non-preflight requests with a not allowed origin
     *
     * @param containerPath The container path to use (for container and page
     * requests)
     * @param annoPath The annotation path to use
     */
    private void getFromForbiddenCorsOrigin(String containerPath, String annoPath) {
        final String origin = "http://forbidden.org";
        // Check container get
        RequestSpecification request = RestAssured.given();
        request.header("Origin", origin);
        Response response = request.get(containerPath);
        assertNotNull(response);
        assertEquals(GlobalErrorCodes.INVALID_CREDENTIALS, response.getStatusCode());
        // Check page get
        request = RestAssured.given();
        request.header("Origin", origin);
        response = request.get(containerPath + "?iris=1&page=0");
        assertNotNull(response);
        assertEquals(GlobalErrorCodes.INVALID_CREDENTIALS, response.getStatusCode());
        // check anno get
        request = RestAssured.given();
        request.header("Origin", origin);
        response = request.get(annoPath);
        assertNotNull(response);
        assertEquals(GlobalErrorCodes.INVALID_CREDENTIALS, response.getStatusCode());
    }

    /**
     * Tests non-preflight requests with an allowed origin
     *
     * @param containerPath The container path to use (for container and page
     * requests)
     * @param annoPath The annotation path to use
     */
    private void getFromAllowedCorsOrigin(String containerPath, String annoPath) {
        final String origin = "http://allowed.org";
        // Check container get
        RequestSpecification request = RestAssured.given();
        request.header("Origin", origin);
        Response response = request.get(containerPath);
        assertNotNull(response);
        assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode());
        checkCorsHeaders(response, origin);
        // Check page get
        request = RestAssured.given();
        request.header("Origin", origin);
        response = request.get(containerPath + "?iris=1&page=0");
        assertNotNull(response);
        assertEquals(PageConstants.GET_PAGE_SUCCESS_CODE, response.getStatusCode());
        checkCorsHeaders(response, origin);
        // Check anno get
        request = RestAssured.given();
        request.header("Origin", origin);
        response = request.get(annoPath);
        assertNotNull(response);
        assertEquals(AnnotationConstants.GET_ANNOTATION_SUCCESS_CODE, response.getStatusCode());
        checkCorsHeaders(response, origin);
    }

    /**
     * Checks that all needed CORS headers are there. This method should be used
     * for non preflight requests
     *
     * @param response The response to check
     * @param origin The origin that was tested
     */
    private void checkCorsHeaders(Response response, String origin) {
        // We must have an allow origin header that matches the request
        checkHeaderContains(response, "Access-Control-Allow-Origin", origin);
        // We must find all headers that exist within the exposed headers
        checkExposedHeaders(response);
    }

    /**
     * Checks that every header in the response is exposed. CORS headers are not
     * tested
     *
     * @param response The response to check
     */
    private void checkExposedHeaders(Response response) {
        String exposedHeadersHeader = response.getHeader(CrossOriginFilter.ACCESS_CONTROL_EXPOSE_HEADERS_HEADER);
        assertNotNull(exposedHeadersHeader);
        exposedHeadersHeader = exposedHeadersHeader.replaceAll(Pattern.quote(" "), "");
        assertNotEquals(0, exposedHeadersHeader.length());
        // * according to CORS means : all accessible, uncomment this code if one day should be tested this way
        // if ("*".equals(exposedHeadersHeader)) {
        // return;
        // }
        // add the always accessible ones (might be duplicates, but thats no problem)
        exposedHeadersHeader += ",Cache-Control,Content-Language,Content-Type,Expires,Last-Modified,Pragma";
        // add those just related to http communication
        exposedHeadersHeader += ",Connection,Transfer-Encoding";
        // We compare them case insensitive ==> to lower case on both
        exposedHeadersHeader = exposedHeadersHeader.toLowerCase();
        List<String> exposedHeadersList = Arrays.asList(exposedHeadersHeader.split(Pattern.quote(",")));
        
        for (Header header : response.getHeaders()) {
            String headerName = header.getName().toLowerCase();
            if (headerName.startsWith("access-control") || headerName.startsWith("keep-alive")) {
                continue;
            }
            boolean contained = exposedHeadersList.contains(headerName);
            if (!contained) {
                System.err.println(response.getHeaders().toString());
            }
            assertTrue(contained, " Header not exposed : " + headerName);
        }
    }
}
