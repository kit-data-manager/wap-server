package edu.kit.scc.dem.wapsrv.testsrest;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.controller.PageConstants;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalHttpParameterException;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalPageIriException;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidRequestException;
import edu.kit.scc.dem.wapsrv.exceptions.NotExistentException;
import edu.kit.scc.dem.wapsrv.exceptions.ResourceDeletedException;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * PageRestTest
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
@ActiveProfiles("test")
@Tag("rest")
public class PageRestTest extends AbstractRestTest {

    private static final Logger logger = LoggerFactory.getLogger(PageRestTest.class);

    /**
     * Create a new PageRestTest
     */
    protected PageRestTest() {
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
        unlock();
    }

    /**
     * Test get page valid with IRIS.
     */
    @Test
    public void testGetPageValidwithIris0AllFormats() {
        testReadPageValidinternalAllFormats(0, HttpMethod.GET, false);
    }

    /**
     * Test head page valid with IRIS.
     */
    @Test
    public void testHeadPageValidwithIris0AllFormats() {
        testReadPageValidinternalAllFormats(0, HttpMethod.HEAD, false);
    }

    /**
     * Test options page valid with IRIS.
     */
    @Test
    public void testOptionsPageValidwithIris0AllFormats() {
        testReadPageValidinternalAllFormats(0, HttpMethod.OPTIONS, false);
    }

    /**
     * Test cors page valid with IRIS.
     */
    @Test
    public void testCorsPageValidwithIris0AllFormats() {
        testReadPageValidinternalAllFormats(0, HttpMethod.OPTIONS, true);
    }

    /**
     * Test get page validwith IRIS.
     */
    @Test
    public void testGetPageValidwithIris1AllFormats() {
        testReadPageValidinternalAllFormats(1, HttpMethod.GET, false);
    }

    /**
     * Test head page valid with IRIS.
     */
    @Test
    public void testHeadPageValidwithIris1AllFormats() {
        testReadPageValidinternalAllFormats(1, HttpMethod.HEAD, false);
    }

    /**
     * Test options page valid with IRIS.
     */
    @Test
    public void testOptionsPageValidwithIris1AllFormats() {
        testReadPageValidinternalAllFormats(1, HttpMethod.OPTIONS, false);
    }

    /**
     * Test CORS page valid with IRIS.
     */
    @Test
    public void testCorsPageValidwithIris1AllFormats() {
        testReadPageValidinternalAllFormats(1, HttpMethod.OPTIONS, true);
    }

    private void testReadPageValidinternalAllFormats(final int iris, final HttpMethod httpMethod, final boolean cors) {
        final int expectedCode = PageConstants.GET_PAGE_SUCCESS_CODE;
        assertNotEqual(-1, expectedCode, "Unallowed http method : " + httpMethod.toString());
        final String containerIri = createDefaultContainer(null); // in the root container
        final String name = extractContainerName(containerIri);
        createDefaultAnnotation(name + "/");
        {
            final String label = getLabelFromServer(name + "/");
            final String modified = getModifiedFromServer(name + "/");
            RequestSpecification request = RestAssured.given();
            request.param("iris", iris + "");
            request.param("page", "0");
            if (cors) {
                request.header("Origin", "http://allowed.org");
            }
            Response response = httpMethod == HttpMethod.GET ? request.get(name + "/")
                    : httpMethod == HttpMethod.HEAD ? request.head(name + "/")
                            : httpMethod == HttpMethod.OPTIONS ? request.options(name + "/") : null;
            assertNotNull(response, "Could not get page response");
            assertEquals(expectedCode, response.getStatusCode(), "Page could not be fetched");
            if (httpMethod == HttpMethod.GET) {
                final String pageIri = response.then().extract().path("id");
                checkProperty(response, "type", "AnnotationPage");
                checkProperty(response, "items");
                checkProperty(response, "partOf");
                // These three are checked further within testGetPage_AnnotationOrder();
                checkProperty(response, "startIndex", "0");
                checkNotProperty(response, "next"); // here no next exists
                checkNotProperty(response, "prev"); // and no prev
                // partOf must have the expected values
                checkProperty(response, "partOf.id", containerIri + "?iris=" + iris);
                checkProperty(response, "partOf.total", "1");
                checkProperty(response, "partOf.modified", modified);
                checkProperty(response, "partOf.label", label);
                checkProperty(response, "partOf.first", pageIri);
                checkProperty(response, "partOf.last", pageIri);
            }
            if (httpMethod == HttpMethod.GET) { // head and options do not have a body
                checkHeader(response, "Content-Type", getDefaultFormatString(Type.PAGE));
            }
            for (String method : PageConstants.VARY_LIST) {
                checkHeaderContains(response, "Vary", method);
            }
            for (HttpMethod method : PageConstants.ALLOWED_METHODS) {
                checkHeaderContains(response, "Allow", method.toString());
            }
        }
        for (String formatString : getFormatRegistry().getFormatStrings()) {
            logger.trace("Getting : " + formatString);
            if (WapServerConfig.getInstance().shouldAlwaysAddDefaultProfilesToJsonLdRequests()) {
                if (formatString.startsWith("application/ld+json")) {
                    continue;
                }
            }
            RequestSpecification request = RestAssured.given();
            request.param("iris", iris + "");
            request.param("page", "0");
            request.accept(formatString);
            if (cors) {
                request.header("Origin", "http://allowed.org");
            }
            Response response = httpMethod == HttpMethod.GET ? request.get(name + "/")
                    : httpMethod == HttpMethod.HEAD ? request.head(name + "/")
                            : httpMethod == HttpMethod.OPTIONS ? request.options(name + "/") : null;
            assertNotNull(response, "Could not get page response");
            assertEquals(expectedCode, response.getStatusCode(), "Page could not be fetched");
            if (httpMethod == HttpMethod.GET) {
                checkHeader(response, "Content-Type", formatString);
            }
            for (String method : PageConstants.VARY_LIST) {
                checkHeaderContains(response, "Vary", method);
            }
            for (HttpMethod method : PageConstants.ALLOWED_METHODS) {
                checkHeaderContains(response, "Allow", method.toString());
            }
            checkNoHeader(response, "Link");
        }
    }

    /**
     * Test get page annotation order.
     */
    @Test
    public void testGetPageAnnotationOrder() {
        String containerIri = createDefaultContainer(null); // in the root container
        String name = extractContainerName(containerIri);
        String[] annotationIris = new String[WapServerConfig.getInstance().getPageSize()];
        for (int n = 0; n < annotationIris.length; n++) {
            annotationIris[n] = createDefaultAnnotation(name + "/");
        }
        // add one additinal to create second page
        createDefaultAnnotation(name + "/");
        // to test its working change any position to something wrong
        // annotationIris[5] = "something wrong";
        RequestSpecification request = RestAssured.given();
        request.param("iris", "1");
        request.param("page", "0");
        Response response = request.get(name + "/");
        assertNotNull(response, "Could not get page response");
        assertEquals(PageConstants.GET_PAGE_SUCCESS_CODE, response.getStatusCode(), "Page could not be fetched");
        // logger.trace(response.asString());
        response.then().body("items", contains(annotationIris));
        checkProperty(response, "startIndex", "0");
        checkProperty(response, "next"); // next exists
        checkNotProperty(response, "prev"); // and no prev
        request = RestAssured.given();
        request.param("iris", "1");
        request.param("page", "1");
        response = request.get(name + "/");
        assertNotNull(response, "Could not get page response");
        assertEquals(PageConstants.GET_PAGE_SUCCESS_CODE, response.getStatusCode(), "Page could not be fetched");
        checkProperty(response, "startIndex", annotationIris.length + "");
        checkNotProperty(response, "next"); // next does not exist
        checkProperty(response, "prev"); // but prev
    }

    /**
     * Test get page too high page number.
     */
    @Test
    public void testGetPageTooHighPageNumber() {
        String containerIri = createDefaultContainer(null); // in the root container
        String name = extractContainerName(containerIri);
        logger.trace("create annotation in " + containerIri);
        logger.trace("           with name " + name);
        createDefaultAnnotation(name + "/");
        RequestSpecification request = RestAssured.given();
        request.param("iris", "0");
        request.param("page", "1");
        Response response = request.get(name + "/");
        assertNotNull(response, "Could not get page response");
        checkException(NotExistentException.class, response);
    }

    /**
     * Test get page container not existent.
     */
    @Test
    public void testGetPageContainerNotExistent() {
        String nonExistentName = getRandomContainerName();
        RequestSpecification request = RestAssured.given();
        request.param("iris", "0");
        request.param("page", "0");
        Response response = request.get(nonExistentName + "/");
        assertNotNull(response, "Could not get page response");
        checkException(NotExistentException.class, response);
    }

    /**
     * Test get page container empty.
     */
    @Test
    public void testGetPageContainerEmpty() {
        // in the root container
        String containerIri = createDefaultContainer(null);
        String name = extractContainerName(containerIri);
        RequestSpecification request = RestAssured.given();
        request.param("iris", "0");
        request.param("page", "0");
        Response response = request.get(name + "/");
        assertNotNull(response, "Could not get page response");
        checkException(NotExistentException.class, response);
    }

    /**
     * Test get page container deleted.
     */
    @Test
    public void testGetPageContainerDeleted() {
        // in the root container
        String containerIri = createDefaultContainer(null);
        String name = extractContainerName(containerIri);
        deleteContainer(name + "/");
        RequestSpecification request = RestAssured.given();
        request.param("iris", "0");
        request.param("page", "0");
        // System.err.println(name);
        Response response = request.get(name + "/");
        assertNotNull(response, "Could not get page response");
        checkException(ResourceDeletedException.class, response);
    }

    /**
     * Test get page invalid params.
     */
    @Test
    public void testGetPageInvalidParams() {
        // in the root container
        String containerIri = createDefaultContainer(null);
        String name = extractContainerName(containerIri);
        // typo in iri
        RequestSpecification request = RestAssured.given();
        request.param("iri", "0");
        request.param("page", "0");
        Response response = request.get(name + "/");
        assertNotNull(response, "Could not get page response");
        checkException(InvalidRequestException.class, response, ErrorMessageRegistry.PAGE_WITH_PAGE_BUT_IRIS_MISSING);
        // typo in page
        request = RestAssured.given();
        request.param("iris", "0");
        request.param("pge", "0");
        response = request.get(name + "/");
        assertNotNull(response, "Could not get page response");
        checkException(IllegalHttpParameterException.class, response);
        // too many parameters
        request = RestAssured.given();
        request.param("iris", "0");
        request.param("page", "0");
        request.param("house", "0");
        response = request.get(name + "/");
        assertNotNull(response, "Could not get page response");
        checkException(IllegalHttpParameterException.class, response);
        // iris missing
        request = RestAssured.given();
        request.param("page", "0");
        response = request.get(name + "/");
        assertNotNull(response, "Could not get page response");
        checkException(InvalidRequestException.class, response, ErrorMessageRegistry.PAGE_WITH_PAGE_BUT_IRIS_MISSING);
        // invalid container preference
        request = RestAssured.given();
        request.param("iris", "2");
        request.param("page", "0");
        response = request.get(name + "/");
        assertNotNull(response, "Could not get page response");
        checkException(IllegalPageIriException.class, response);
        // invalid page number
        request = RestAssured.given();
        request.param("iris", "1");
        request.param("page", "-1");
        response = request.get(name + "/");
        assertNotNull(response, "Could not get page response");
        checkException(IllegalPageIriException.class, response);
    }
}
