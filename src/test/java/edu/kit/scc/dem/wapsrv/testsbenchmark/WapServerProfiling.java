package edu.kit.scc.dem.wapsrv.testsbenchmark;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import edu.kit.scc.dem.wapsrv.controller.AnnotationConstants;
import edu.kit.scc.dem.wapsrv.controller.ContainerConstants;
import edu.kit.scc.dem.wapsrv.testscommon.TestDataStore;
import edu.kit.scc.dem.wapsrv.testsrest.AbstractRestTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for simple profiling the WAP-Server
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
public class WapServerProfiling extends AbstractRestTest {

    private static final Logger logger = LoggerFactory.getLogger(WapServerProfiling.class);

    private final String contentType = "application/ld+json; profile=\"http://www.w3.org/ns/anno.jsonld\"";
    private final String testContainer;
    private final String testAnnotationPraefix
            = "{ \"@context\": \"http://www.w3.org/ns/anno.jsonld\", \"id\": \"http://example.org/anno";
    private final String testAnnotationPostfix = "\", \"type\": \"Annotation\", \"body\": \"http://example.org/post1\", "
            + "\"target\": \"http://example.com/page1\" }";

    private final String gQuerySource = "PREFIX oa: <http://www.w3.org/ns/oa#>\r\n"
            + "\r\n"
            + "SELECT ?anno {\r\n"
            + "  GRAPH ?g {\r\n"
            + "    ?anno oa:hasTarget ?t .\r\n"
            + "    ?t oa:hasSource <http://diglib.hab.de/drucke/218-20-quod-2s/00094.jpg> .\r\n"
            + "  }\r\n"
            + "}\r\n";

    private final String gQueryDistinct = "PREFIX oa: <http://www.w3.org/ns/oa#>\r\n"
            + "\r\n"
            + "SELECT DISTINCT ?source {\r\n"
            + "  GRAPH ?g {\r\n"
            + "    ?anno oa:hasTarget ?t .\r\n"
            + "    ?t oa:hasSource ?source .\r\n"
            + "  }\r\n"
            + "}";

    private final String gQueryRelativeAnnoSize = "PREFIX oa: <http://www.w3.org/ns/oa#>\r\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
            + "PREFIX  dc:<http://purl.org/dc/elements/1.1/>\r\n"
            + "\r\n"
            + "SELECT ?anno ?width ?height {\r\n"
            + "  GRAPH ?g {\r\n"
            + "    ?anno oa:hasBody ?b1 .\r\n"
            + "    ?anno oa:hasBody ?b2 .\r\n"
            + "    ?b1 dc:title \"RelativeWidth\" .\r\n"
            + "    ?b1 rdf:value ?width .\r\n"
            + "    ?b2 dc:title \"RelativeHeight\" .\r\n"
            + "    ?b2 rdf:value ?height .\r\n"
            + "    FILTER(xsd:decimal(?width) <= 2)\r\n"
            + "    FILTER(xsd:decimal(?height) <= 2)\r\n"
            + "  }\r\n"
            + "}\r\n";

    /**
     * Creates a new AnnotationRestTest
     *
     * @param init True to call complete init stack
     */
    public WapServerProfiling(boolean init) {
        super(init);
        this.testContainer = getRandomContainerName();
        createTestContainer();
    }

    /**
     * Starts the profiler
     *
     * @param args Ignored
     */
    public static void main(String[] args) {
        //example how to connect to remote host : args = new String[] {"http://192.168.2.51:8080"};
        //Cannot use JUNIT for this. The profiling should not execute during normal testing
        //and it interferes with the other rest tests, which will not work anymore
        //therefore start it via "Run as Java Application", the usual main style
        WapServerProfiling profiler = null;
        if (args != null && args.length == 1) {
            String urlStr = args[0];
            URL url = null;
            try {
                url = new URL(urlStr);
            } catch (MalformedURLException e) {
                logger.trace("Invalid url : " + urlStr);
                return;
            }
            if (!url.getProtocol().equalsIgnoreCase("http") && !url.getProtocol().equalsIgnoreCase("https")) {
                logger.trace("Invalid url , only http and https possible : " + urlStr);
                return;
            }
            logger.trace("Profiling  " + url);
            configureRestAssured(url);

            System.err.println("#### Not implemented yet, jena must be able to use remote host and");
            System.err.println("#### AbstractRestTest needs to be not directly linked to Spring");
            if (System.currentTimeMillis() > 0) {
                return;
            }
            profiler = new WapServerProfiling(false);
        } else {
            logger.trace("Profiling local server (autostarted)");
            profiler = new WapServerProfiling(true);
        }

        profiler.profileAnnotationPost();

        System.exit(0);
    }

    private static void configureRestAssured(URL url) {
        RestAssured.baseURI = url.toString();
        RestAssured.port = url.getPort();
        if (url.getProtocol().equalsIgnoreCase("https")) {
            // If we want to test against ssl test servers, it can be assumed that they will
            // be using self signed certificates. This disables strict trust checking
            RestAssured.useRelaxedHTTPSValidation();
        }
    }

    private void createTestContainer() {
        RequestSpecification request = RestAssured.given();
        request.body(TestDataStore.getContainer());
        request.header("Link", ContainerConstants.LINK_TYPE);
        request.header("Slug", testContainer);
        request.contentType("application/ld+json;");
        Response response = request.post();
        if (response.getStatusCode() != ContainerConstants.POST_CONTAINER_SUCCESS_CODE) {
            logger.trace("Error while creating test container at: " + getIri(response));
        } else {
            logger.trace("Test container created at: " + getIri(response));
        }
    }

    private boolean postAnnotation(String annotation) {
        boolean result;
        RequestSpecification request = RestAssured.given();
        request.contentType(contentType);
        request.accept(contentType);
        request.body(annotation);
        Response response = request.post(testContainer + "/");
        result = response.getStatusCode() == AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE;
        if (!result) {
            logger.trace("Error while creating test annotation: " + response.getStatusCode());
        }
        return result;
    }

    @Override
    protected String getRandomAnnotation() {
        return getAnnotation(1); // remove this method if testing should use all
    }

    /**
     * Profile annotation put.
     */
    //@Test
    public void profileAnnotationPost() {
        // Number of annotations to post for the test
        final int number = 10000;
        Boolean result = true;
        int counter = 1;
        long durationAll = 0;
        long durationSingle;
        long startTest;
        long endTest;
        String testAnno;
        logger.trace("~~~ Starting profiling ~~~");
        while (counter <= number && result) {
            testAnno = testAnnotationPraefix + counter + testAnnotationPostfix;
            startTest = System.currentTimeMillis();
            result = postAnnotation(testAnno);
            endTest = System.currentTimeMillis();
            durationSingle = endTest - startTest;
            durationAll += durationSingle;
            logger.trace("# " + counter + " Duration: " + durationSingle + " ms ");
            logger.trace(" ---------- Estimated Duration to finish: "
                    + (1.0 * (durationAll / counter) * (number - counter)) / 1000 / 60 / 60
                    + " hours ------------");
            counter++;
        }
        logger.trace("~~~ Profiling results for " + (counter - 1) + " annotations ~~~");
        logger.trace("   Entire Duration: " + (int) (durationAll / 1000) + " s " + durationAll % 1000 + " ms");
        logger.trace("   Mean annotation put duration: " + durationAll / counter + " ms");
        logger.trace("~~~ Finishing profiling ~~~");

        testQueryTime(counter, "List DB Content", "SELECT * { GRAPH ?g { ?s ?p ?o }}");
        testQueryTime(counter, "Source", gQuerySource);
        testQueryTime(counter, "Distinct", gQueryDistinct);
        testQueryTime(counter, "Relative AonnoSize", gQueryRelativeAnnoSize);

    }

    /**
     * @param counter
     * @param query
     * @param
     */
    private void testQueryTime(int counter, String queryName, String query) {
        long durationAll;
        long startTest;
        long endTest;
        // list all annotations
        startTest = System.currentTimeMillis();
        doSparqlQuery(query);
        endTest = System.currentTimeMillis();
        durationAll = endTest - startTest;
        logger.trace("############## Query: " + queryName + " ################");
        logger.trace("~~~ SPARQL Profiling results for " + (counter - 1) + " annotations ~~~");
        logger.trace("   Entire Duration: " + (int) (durationAll / 1000) + " s " + durationAll % 1000 + " ms");
        logger.trace("~~~ SPARQL Finishing profiling ~~~");
    }

    private void doSparqlQuery(String query) {
        // test read only SPARQL
        RDFConnection conn = RDFConnectionFactory.connectFuseki("http://localhost:13333/sparql");
        // conn.load("data.ttl") ;
        QueryExecution qExec = conn.query(query);
        // QueryExecution qExec = conn.query("SELECT DISTINCT ?n WHERE { ?s
        // <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?n . }");
        ResultSet rs = qExec.execSelect();
        // Resource oldGraph = null;
        logger.trace(" ***** SPARQL Result start *****");
        while (rs.hasNext()) {
            rs.next();
            // If we got something, thats enough
        }
        logger.trace(" *****  SPARQL Result end  *****");
        qExec.close();
        conn.close();
    }
}
