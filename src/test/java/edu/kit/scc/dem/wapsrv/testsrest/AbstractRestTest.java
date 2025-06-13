package edu.kit.scc.dem.wapsrv.testsrest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.rdf.api.Dataset;
import org.junit.jupiter.api.Tag;
import org.springframework.context.ApplicationContext;
import edu.kit.scc.dem.wapsrv.app.ConfigurationKeys;
import edu.kit.scc.dem.wapsrv.app.WapServerApplication;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.controller.AnnotationConstants;
import edu.kit.scc.dem.wapsrv.controller.ContainerConstants;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.Formatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdFormatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfBackend;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRdfBackend;
import edu.kit.scc.dem.wapsrv.testscommon.OwnHttpURLConnection;
import edu.kit.scc.dem.wapsrv.testscommon.OwnResponse;
import edu.kit.scc.dem.wapsrv.testscommon.SysOut;
import edu.kit.scc.dem.wapsrv.testscommon.TestDataStore;
import edu.kit.scc.dem.wapsrv.testscommon.OwnHttpURLConnection.Request;
import edu.kit.scc.dem.wapsrv.testscommon.TestJenaRespository;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * Used as the base class for all external tests. Contains code that sets up the
 * running application for all test-classes, no matter which one gets executed
 * first.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@Tag("rest")
@ActiveProfiles("test")
public abstract class AbstractRestTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRestTest.class);

    /**
     * If set to true, all tests that are known to fail because of lacking
     * support, will fail with appropriate messages. If false, they will
     * succeed. Usually false should be left or false positives will show up.
     */
    protected static final boolean FAIL_ON_UNSUPPORTED_TESTS = false;
    /**
     * A syntactically valid ETag to use for tests
     */
    protected static final String ETAG_EXAMPLE = "\"ETAG\"";
    /**
     * Enable or disable strict serialization of all external REST tests
     */
    private static final boolean STRICT_SERIALIZATION = true;
    /**
     * The lock used for strict serialization
     */
    private static final Lock LOCK = new ReentrantLock();
    /**
     * Stores the initialization state
     */
    private static boolean isInitialized = false;
    /**
     * The path where the test database and config will be created
     */
    private static final String TEST_DB_PATH = "temp/tests/rest_tests_db";
    /**
     * The RDF backend
     */
    private static RdfBackend rdfBackend;
    // The jsonLdProfileRegistry
    private static JsonLdProfileRegistry jsonLdProfileRegistry;
    // the format registry
    private static FormatRegistry formatRegistry;

    /**
     * The default constructor implicitly called by all subclasses
     *
     * @param init true to call init stack
     */
    protected AbstractRestTest(boolean init) {
        if (init) {
            initApplication();
        }
    }

    /**
     * Create or empty the test folder
     */
    private static void initTestFolder() {
        if (!TEST_DB_PATH.startsWith("temp/")) {
            System.err.println("hier1");
            throw new RuntimeException("Should the testfolder really be created and or deleted outside of temp/ ?");
        }
        File testFolder = new File(TEST_DB_PATH);
        if (!testFolder.exists()) {
            if (!testFolder.mkdirs()) {
                fail("Cannot create test folder");
            }
        } else {
            deleteFolder(testFolder, false);
        }
    }

    private static void deleteFolder(File folder, boolean removeDir) {
        File[] files = folder.listFiles();
        logger.trace("Cleaning test db folder : " + folder.getPath());
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFolder(file, true);
            } else if (!file.delete()) {
                fail("Cannot delete file in test folder : " + file.getName());
            }
        }
        if (removeDir) {
            folder.delete();
        }
    }

    /**
     * Initializes the application once
     */
    private static synchronized void initApplication() {
        if (isInitialized) {
            logger.trace("Is already initialized");
            return;
        }
        logger.trace("Beginning initialization");
        initTestFolder();
        // Get the default properties and override where necessary
        Properties props = WapServerConfig.getDefaultProperties();
        props.setProperty(ConfigurationKeys.DataBasePath.toString(), TEST_DB_PATH);
        props.setProperty(ConfigurationKeys.Hostname.toString(), "localhost");
        props.setProperty(ConfigurationKeys.WapIp.toString(), "localhost");
        props.setProperty("server.tomcat.keep-alive-timeout",  "30000");
        props.setProperty(ConfigurationKeys.WapPort.toString(), "8081");
        props.setProperty("logging.level.edu.kit.scc.dem.wapsrv", "TRACE");
        props.setProperty(ConfigurationKeys.ShouldAppendStackTraceToErrorMessages.toString(), "true");
        props.setProperty(ConfigurationKeys.SparqlReadPort.toString(), "13333"); // for sparql test in CommonRestTests
        props.setProperty(ConfigurationKeys.SparqlWritePort.toString(), "13334"); // for sparql test in CommonRestTests
        props.setProperty(ConfigurationKeys.CorsAllowedOriginsPath.toString(),
                TEST_DB_PATH + "/cors_allowed_origins.conf");
        // Create properties file and override config location
        File testConfigFile = new File(TEST_DB_PATH + "/application.properties");
        WapServerConfig.setWapServerConfigFile(testConfigFile);
        try {
            FileOutputStream out = new FileOutputStream(testConfigFile);
            props.store(out, "Test properties");
            out.flush();
            out.close();
        } catch (IOException ex) {
            System.err.println("cannot create test config, exitting : " + ex.getMessage());
        }
        File testCorsAllowedOriginsFile = new File(TEST_DB_PATH + "/cors_allowed_origins.conf");
        try {
            FileOutputStream out = new FileOutputStream(testCorsAllowedOriginsFile);
            out.write("allowed.org".getBytes());
            out.flush();
            out.close();
        } catch (IOException ex) {
            System.err.println("cannot create test cors allowed origins file, exitting : " + ex.getMessage());
        }
        logger.trace("Starting main");
        Thread helper = new Thread() {
            public void run() {
                WapServerApplication.main(new String[]{});
            }
        };
        helper.start();
        logger.trace("Starting main done");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                // Seems to be not necessary, has its own shutdown hook
            }
        });
        // Wait for complete application startup
        boolean startupOk = WapServerApplication.blockUntilStarted();
        if (!startupOk) {
            System.err.println("Could not start the main application");
            fail("Could not start the main application");
        }
        // IF remote server / ssl ==> uncomment and update the values
        // this is not the usual test behavior, default = keep commented out
        // props.setProperty(ConfigurationKeys.Hostname.toString(), "192.168.2.10");
        // props.setProperty(ConfigurationKeys.WapIp.toString(), "192.168.2.10");
        // props.setProperty(ConfigurationKeys.WapPort.toString(), "443");
        // props.setProperty(ConfigurationKeys.EnableHttps.toString(), "true");
        // WapServerConfig.getInstance().updateConfig(props);
        // Config rest assured once
        RestAssured.baseURI = WapServerConfig.getInstance().getRootContainerIri();
        RestAssured.port = WapServerConfig.getInstance().getWapPort();
        if (WapServerConfig.getInstance().isHttpsEnabled()) {
            // If we want to test against ssl test servers, it can be assumed that they will
            // be using self signed certificates. This disables strict trust checking
            RestAssured.useRelaxedHTTPSValidation();
        }
        // Now get the RDF backend needed for conversion of formats
        rdfBackend = JenaRdfBackend.instance;
        ApplicationContext appContext = WapServerApplication.getRunningApplicationContext();
        jsonLdProfileRegistry = appContext.getBean(JsonLdProfileRegistry.class);
        formatRegistry = appContext.getBean(FormatRegistry.class);
        // Decorate System.err
        System.setErr(new SysOut(System.err));
        logger.trace("Initialization done");
        isInitialized = true;
        // logger.trace("Starting time of tests : " + (System.currentTimeMillis() / 1000));
    }

    /**
     * Depending on the configured serialization either one thread returns after
     * the other finished, or all return at once after the application has been
     * fully started
     */
    protected static void lock() {
        if (STRICT_SERIALIZATION) {
            LOCK.lock();
        } else {
            // The first test instantiated starts initialization of the WapServerApplication
            // and gets to be executed after that has been fully done. The others directly
            // land here and must be blocked at least until initialization completed.
            // We should implement something more elegant here, but to get it working
            // quickly busy waiting is ok for the threads here in a testing environment
            while (!isInitialized) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * If strictly serialized, this informs the next thread it's his turn.
     * Without serialization, this is a noop
     */
    protected static void unlock() {
        if (STRICT_SERIALIZATION) {
            LOCK.unlock();
        } else {
            // nothing to do
        }
    }

    /**
     * Gets the base URL of the WAP endpoint == root container IRI
     *
     * @return The base URL / root container IRI
     */
    protected String getBaseUrl() {
        return WapServerConfig.getInstance().getBaseUrl() + "/wap/";
    }

    /**
     * Gets the string serialization of a random annotation
     *
     * @return A random annotation as string
     */
    protected String getRandomAnnotation() {
        return TestDataStore.getAnnotation();
    }

    /**
     * Gets the annotation identified by the given number. Format of test
     * annotations is example_number_.... or example_number.jsonld
     *
     * @param number The test annotation with the given number
     * @return The numbered annotations as string
     */
    protected String getAnnotation(int number) {
        return TestDataStore.getAnnotation(number);
    }

    /**
     * Extracts the ETag from the header of a response (ETag)
     *
     * @param response The response
     * @return The ETag from the headers
     */
    protected String getEtag(Response response) {
        return response.getHeader("ETag");
    }

    /**
     * Extracts the IRI from the header of a response (Location)
     *
     * @param response The response
     * @return The IRI from the headers
     */
    protected String getIri(Response response) {
        return response.getHeader("Location");
    }

    /**
     * Fetches the ETag of a given path directly from the server
     *
     * @param path The path (base url + path = complete iri)
     * @return The ETag of the object denoted by the path
     */
    protected String getEtagFromServer(String path) {
        RequestSpecification request = RestAssured.given();
        Response response = request.get(path);
        // System.err.println(response.getBody().asString());
        return response.header("ETag");
    }

    /**
     * Fetches the label of a given container path directly from the server
     *
     * @param path The path (base url + path = complete iri)
     * @return The label of the container denoted by the path
     */
    protected String getLabelFromServer(String path) {
        RequestSpecification request = RestAssured.given();
        Response response = request.get(path);
        return response.then().extract().path("label");
    }

    /**
     * Fetches the total value of a given container path directly from the
     * server
     *
     * @param path The path (base url + path = complete iri)
     * @return The total value of the container denoted by the path
     */
    protected int getTotalFromServer(String path) {
        RequestSpecification request = RestAssured.given();
        Response response = request.get(path);
        return Integer.parseInt(response.then().extract().path("total"));
    }

    /**
     * Fetches the modified of a given path directly from the server
     *
     * @param path The path (base url + path = complete iri)
     * @return The modified of the object denoted by the path
     */
    protected String getModifiedFromServer(String path) {
        RequestSpecification request = RestAssured.given();
        Response response = request.get(path);
        // logger.trace(response.getBody().asString());
        return response.then().extract().path("modified");
    }

    /**
     * Extracts the path part of a full IRI. IRI = base url + path
     *
     * @param iri The IRI
     * @return The path part
     */
    protected String getPathFromIri(String iri) {
        return iri.substring(WapServerConfig.getInstance().getRootContainerIri().length());
    }

    /**
     * Creates the default annotation in the given container. The container must
     * be a string relative to baseUrl which concatenated to baseUrl forms the
     * complete container IRI.
     *
     * @param container The container
     * @return The created annotation IRI
     */
    protected String createDefaultAnnotation(String container) {
        if (!container.startsWith("/")) {
            // container= "/" + container;
        }
        String annotation = getAnnotation(1);
        assertNotNull(annotation, "Could not load example annotation");
        RequestSpecification request = RestAssured.given();
        request.contentType("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.accept("application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\"");
        request.body(annotation);
        Response postResponse = request.post(container);
        assertNotNull(postResponse, "Could not get post response");
        if (AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE != postResponse.getStatusCode()) {
            logger.trace(postResponse.asString());
        }
        assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, postResponse.getStatusCode(),
                "Annotation could not be created");
        return getIri(postResponse);
    }

    /**
     * Deletes the container at the given path BaseUrl + path == complete
     * container IRI
     *
     * @param containerPath The container path
     */
    protected void deleteContainer(String containerPath) {
        String etag = getEtagFromServer(containerPath);
        RequestSpecification request = RestAssured.given();
        request.header("If-Match", etag);
        Response deleteResponse = request.delete(containerPath);
        assertNotNull(deleteResponse, "Could not get delete response");
        if (ContainerConstants.DELETE_CONTAINER_SUCCESS_CODE != deleteResponse.getStatusCode()) {
            logger.trace(deleteResponse.asString());
        }
        assertEquals(ContainerConstants.DELETE_CONTAINER_SUCCESS_CODE, deleteResponse.getStatusCode(),
                "Container could not be deleted");
    }

    /**
     * Checks if the given property exists with the given value in the response
     *
     * @param response The response
     * @param propertyName The property to check
     * @param value The value expected
     */
    protected void checkProperty(Response response, String propertyName, String value) {
        response.then().body(propertyName, is(value));
    }

    /**
     * Checks if the given property exists in the response
     *
     * @param response The response
     * @param propertyName The property to check
     */
    protected void checkProperty(Response response, String propertyName) {
        response.then().body("$", org.hamcrest.Matchers.hasKey(propertyName));
    }

    /**
     * Checks if the given property does not exist
     *
     * @param response The response
     * @param propertyName The property to check for non existence
     */
    protected void checkNotProperty(Response response, String propertyName) {
        boolean found = false;
        try {
            response.then().body("$", org.hamcrest.Matchers.hasKey(propertyName));
            found = true;
        } catch (AssertionError ex) {
            // ex.printStackTrace();
            found = false;
        }
        if (found) {
            fail("Property exists within reposnse : " + propertyName);
        }
    }

    /**
     * Checks if the HTTP exception response contains the given exception
     *
     * @param exceptionClass The class of the exception to test
     * @param response The response from the server
     */
    protected void checkException(Class<? extends WapException> exceptionClass, Response response) {
        WapException ex = getInstance(exceptionClass);
        // try {
        assertEquals(ex.getHttpStatusCode(), response.getStatusCode(),
                "Unexpected status code : should be " + ex.getHttpStatusCode() + ", is " + response.getStatusCode());
        response.then().body("error", is(exceptionClass.getSimpleName()));
    }

    /**
     * Checks if the HTTP exception response contains the given exception and
     * the expected message
     *
     * @param exceptionClass The class of the exception to test
     * @param response The response from the server
     * @param message The message string expected
     */
    protected void checkException(Class<? extends WapException> exceptionClass, Response response, String message) {
        WapException ex = getInstance(exceptionClass);
        // try {
        assertEquals(ex.getHttpStatusCode(), response.getStatusCode(),
                "Unexpected status code : should be " + ex.getHttpStatusCode() + ", is " + response.getStatusCode());
        response.then().body("error", is(exceptionClass.getSimpleName()));
        String messageReceived = response.then().extract().path("message");
        assertNotNull(messageReceived, "No message value in the error response from the server");
        if (!messageReceived.startsWith(message)) {
            fail("The message was not as expected : " + messageReceived);
        }
    }

    /**
     * Checks if the HTTP exception response contains the given exception
     *
     * @param exceptionClass The class of the exception to test
     * @param response The response from the server
     * @param message The message string expected
     */
    protected void checkException(Class<? extends WapException> exceptionClass, OwnResponse response, String message) {
        WapException ex = getInstance(exceptionClass);
        if (response.getReceivedString().indexOf("\"error\":\"" + exceptionClass.getSimpleName() + "\"") == -1) {
            logger.trace(response.getReceivedString());
            assertEquals(ex.getHttpStatusCode(), response.getStatus(),
                    "Unexptected status code : should be " + ex.getHttpStatusCode() + ", is " + response.getStatus());
        }
        if (response.getReceivedString().indexOf(message) == -1) {
            System.err.println("expected " + message + "\nbody was\n" + response.getReceivedString());
            fail("The message was not as expected ");
        }
    }

    /**
     * Checks if the http exception response contains the given exception and
     * the expected message
     *
     * @param exceptionClass The class of the exception to test
     * @param response The response from the server
     */
    protected void checkException(Class<? extends WapException> exceptionClass, OwnResponse response) {
        WapException ex = getInstance(exceptionClass);
        if (response.getReceivedString().indexOf("\"error\":\"" + exceptionClass.getSimpleName() + "\"") == -1) {
            logger.trace(response.getReceivedString());
            assertEquals(ex.getHttpStatusCode(), response.getStatus(),
                    "Unexptected status code : should be " + ex.getHttpStatusCode() + ", is " + response.getStatus());
        }
    }

    private WapException getInstance(Class<? extends WapException> exceptionClass) {
        try {
            // First we check for the existence of an empty constructor
            try {
                Constructor<?> emptyConstructor = exceptionClass.getConstructor(new Class<?>[]{});
                // emptyConstructor.setAccessible(true);
                return (WapException) emptyConstructor.newInstance();
            } catch (NoSuchMethodException e) {
                // Ok, empty constructor not found
            }
            // Then we look for one that needs a String
            try {
                Constructor<?> configConstructor = exceptionClass.getConstructor(new Class<?>[]{String.class});
                return (WapException) configConstructor.newInstance("does not matter");
            } catch (NoSuchMethodException ex) {
                // Ok, this one does not exist too
            }
            fail("No usable constructor for Exception class " + exceptionClass);
        } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new InternalServerException(e.getMessage());
        }
        return null;
    }

    /**
     * Creates a default container in the given parentContainer. To create the
     * container within the root container, just provide null or am empty string
     * as the parentContainer The container must be a string relative to baseUrl
     * which concatenated to baseUrl forms the complete container iri.
     *
     * @param parentContainer The parent container to add the new one to (null
     * or empty String for root container)
     * @return The created container iri
     */
    protected String createDefaultContainer(String parentContainer) {
        String newParentContainer = parentContainer;
        if (newParentContainer != null && newParentContainer.trim().length() != 0
                && !newParentContainer.startsWith("/")) {
            newParentContainer = "/" + newParentContainer;
        }
        String containerName = getRandomContainerName();
        RequestSpecification request = RestAssured.given();
        request.body(TestDataStore.getContainer());
        request.header("Link", ContainerConstants.LINK_TYPE);
        request.header("Slug", containerName);
        request.contentType("application/ld+json;");
        Response response = (newParentContainer == null || newParentContainer.trim().length() == 0) ? request.post("/")
                : request.post(newParentContainer);
        if (ContainerConstants.POST_CONTAINER_SUCCESS_CODE != response.getStatusCode()) {
            System.err.println(response.asString());
        }
        return getIri(response);
    }

    /**
     * Creates a random container name
     *
     * @return A random container name
     */
    protected String getRandomContainerName() {
        Random rnd = new Random();
        return "testContainer" + Math.abs(rnd.nextInt());
    }

    /**
     * Extracts the container name from a given container iri.
     * http://example.org/cont1/cont2/ ==> name = cont2
     *
     * @param containerIri The full container iri
     * @return The name of the denoted container
     */
    protected String extractContainerName(String containerIri) {
        return containerIri.substring(containerIri.lastIndexOf("/", containerIri.length() - 2) + 1,
                containerIri.length() - 1);
    }

    /**
     * Compare to objects for equality and throws an Exception with the given
     * message when o1.equals(o2).
     *
     * @param o1 The first object to compare, not null
     * @param o2 The second object to compare, not null
     * @param errorMessage The message to throw on errors
     */
    protected void assertNotEqual(Object o1, Object o2, String errorMessage) {
        if (o1 == null || o2 == null) {
            // null not allowed
            throw new NullPointerException("Null not allowed");
        } else if (o1.equals(o2)) {
            fail(errorMessage);
        }
    }

    /**
     * Compare to ints for equality and throws an Exception with the given
     *
     * @param i1 The first int to compare
     * @param i2 The second int to compare
     * @param errorMessage The message to throw on errors
     */
    protected void assertNotEqual(int i1, int i2, String errorMessage) {
        if (i1 == i2) {
            fail(errorMessage);
        }
    }

    /**
     * Checks the header value of a response against the expected value
     *
     * @param response The response to extract the header from
     * @param headerName The name of the header
     * @param value The expected value
     */
    protected void checkHeader(Response response, String headerName, String value) {
        if (!value.equals(response.header(headerName))) {
            logger.trace("Header is : " + response.header(headerName));
            logger.trace("should be : " + value);
        }
        assertTrue(response.header(headerName).startsWith(value));

        //assertEquals(value, response.header(headerName));
    }

    /**
     * Checks the header of a response does not exist
     *
     * @param response The response to extract the header from
     * @param headerName The name of the header
     */
    protected void checkNoHeader(Response response, String headerName) {
        assertNull(response.header(headerName));
    }

    /**
     * Checks that the header value of a response contains the expected value
     *
     * @param response The response to extract the header from
     * @param headerName The name of the header
     * @param value The contained value
     */
    protected void checkHeaderContains(Response response, String headerName, String value) {
        String isValue = response.header(headerName);
        assertNotNull(isValue);
        if (isValue.indexOf(value) < 0) {
            fail("Header does not contain the required value : value=\"" + value + "\", header = \"" + isValue + "\"");
        }
    }

    /**
     * Checks the header value of a response against the expected values. They
     * must be contained in any order and the header must be one with multiple
     * instances, not one that exists only once and has comma separated values.
     *
     * @param response The response to extract the header from
     * @param headerName The name of the header
     * @param values The expected values
     */
    protected void checkHeader(Response response, String headerName, String[] values) {
        List<String> valuesLeft = new Vector<String>(Arrays.asList(values));
        List<Header> headerList = response.headers().asList();
        for (Header header : headerList) {
            if (header.getName().equals(headerName)) {
                valuesLeft.remove(header.getValue());
            }
        }
        if (valuesLeft.size() > 0) {
            fail("Not all header values were present, missing " + valuesLeft);
        }
    }

    /**
     * Checks that the response contains the requested header
     *
     * @param response The response to extract the header from
     * @param headerName The name of the header
     */
    protected void checkHeaderExists(Response response, String headerName) {
        assertNotNull(response.header(headerName));
    }

    /**
     * Gets the default format String
     *
     * @param type The type of object
     * @return The default format String
     */
    protected String getDefaultFormatString(Type type) {
        try {
            Formatter formatter = JsonLdFormatter.class.newInstance();
            formatter.setAcceptPart(null, type);
            // this applies the default values supplied via Accept headers
            return formatter.getContentType();
        } catch (InstantiationException e) {
            fail(e.getMessage());
            return null; // can never be reached
        } catch (IllegalAccessException e) {
            fail(e.getMessage());
            return null; // can never be reached
        }
    }

    /**
     * Executes a HTTP request of given type to the given url. If headers should
     * be multiple of the same kind, use a list as value mapped to in
     * requestHeaders. In every other case toString() is executed
     *
     * @param url The url to send the request to. No parameters allowed, just
     * the url part. Not null
     * @param requestType The type of request, not null
     * @param requestHeaders The headers as a map from header to values
     * @param parameters The parameters as a String. Without ? e.G.
     * page=0&iris=1. May be null
     * @param body The body, may be null
     * @return The OwnResponse object. The status code is -1 if an I/O Exception
     * occurred
     */
    protected OwnResponse performOwnHttpRequest(URL url, Request requestType, Map<String, Object> requestHeaders,
            String parameters, String body) {
        assertNotNull(url);
        assertNotNull(requestType);
        assertNotNull(requestHeaders);
        OwnHttpURLConnection con = new OwnHttpURLConnection(url, requestType);
        assertNotNull(con);
        for (String key : requestHeaders.keySet()) {
            Object value = requestHeaders.get(key);
            if (value instanceof List) {
                for (Object innerValue : (List<?>) value) {
                    con.setRequestProperty(key, innerValue.toString());
                }
            } else {
                con.setRequestProperty(key, value.toString());
            }
        }
        if (parameters != null) {
            con.setParameters("?" + parameters);
        }
        if (body != null) {
            con.setBody(body);
        }
        int status = con.getResponseCode();
        OwnResponse ownResponse = new OwnResponse(status, con.getTransmittedString(), con.getReceivedString());
        return ownResponse;
    }

    /**
     * Converts the given JSON ld String to an equal String in the requested new
     * Format
     *
     * @param jsonLd The JSON-LD representation
     * @param format The target format
     * @return The converted string
     */
    protected String convertFormat(String jsonLd, Format format) {
        assertNotNull(jsonLd);
        assertNotNull(format);
        if (format == Format.JSON_LD) {
            return jsonLd;
        }
        String expanded = getJsonLdProfileRegistry().expandJsonLd(jsonLd);
        Dataset dataset = rdfBackend.readFromString(expanded, Format.JSON_LD);
        return rdfBackend.getOutput(dataset, format);
    }

    /**
     * Gets the JSON-LD profile registry.
     *
     * @return the JSON-LD profile registry
     */
    protected JsonLdProfileRegistry getJsonLdProfileRegistry() {
        return jsonLdProfileRegistry;
    }

    /**
     * Gets the format registry.
     *
     * @return The format registry
     */
    protected FormatRegistry getFormatRegistry() {
        return formatRegistry;
    }
}
