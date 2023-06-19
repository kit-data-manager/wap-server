package edu.kit.scc.dem.wapsrv.testsrest;

import static org.junit.jupiter.api.Assertions.*;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import edu.kit.scc.dem.wapsrv.app.FusekiRunner;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.controller.AnnotationConstants;
import edu.kit.scc.dem.wapsrv.controller.ContainerConstants;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * CommonRestTests
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
public class CommonRestTests extends AbstractRestTest {

    private static final Logger logger = LoggerFactory.getLogger(CommonRestTests.class);

    /**
     * Create a new CommonnRestTest
     */
    protected CommonRestTests() {
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
     * Test sparql connection.
     */
    @Test
    public void testSparqlConnection() {
        // test read only SPARQL
        RDFConnection conn
                = RDFConnectionFactory.connectFuseki("http://localhost:13333" + FusekiRunner.ENDPOINT_PREFIX + "/sparql");
        // conn.load("data.ttl") ;
        QueryExecution qExec = conn.query("SELECT * { GRAPH ?g { ?s ?p ?o }}");
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
        // Test (potentially) writable SPARQL, just existence is enough
        conn = RDFConnectionFactory.connectFuseki("http://localhost:13334" + FusekiRunner.ENDPOINT_PREFIX + "/sparql");
        // conn.load("data.ttl") ;
        qExec = conn.query("SELECT * { GRAPH ?g { ?s ?p ?o }}");
        // QueryExecution qExec = conn.query("SELECT DISTINCT ?n WHERE { ?s
        // <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?n . }");
        rs = qExec.execSelect();
        // oldGraph = null;
        logger.trace(" ***** SPARQL Result start *****");
        while (rs.hasNext()) {
            rs.next();
            // Code left here as a howto access sparql, to get something is enough
            // Resource graph = qs.getResource("g");
            // if (oldGraph == null || !graph.toString().equals(oldGraph.toString())) {
            // logger.trace("--- Graph: " + graph + " ---");
            // oldGraph = graph;
            // }
            // Resource subject = qs.getResource("s");
            // Resource predicate = qs.getResource("p");
            // String object;
            // object = "<" + qs.getResource("o").toString() + ">";
            // logger.trace(" <" + subject + "> <" + predicate + "> " + object);
        }
        logger.trace(" *****  SPARQL Result end  *****");
        qExec.close();
        conn.close();
    }

    /**
     * Test content negotiation annotations.
     */
    @Test
    public void testContentNegotiationAnnotations() {
        if (WapServerConfig.getInstance().shouldAlwaysAddDefaultProfilesToJsonLdRequests()) {
            return; // we cannot test under this circumstance
        }
        // Create a container with a subcontainer and an annotation.
        // Then fetch the anno with different profiles requested
        // and check the reponses
        final String containerIri = createDefaultContainer(null); // in the root container
        final String name = extractContainerName(containerIri);
        final String annoIri = createDefaultAnnotation(name + "/");
        createDefaultContainer(name + "/");
        // Request annotation without any given accept header profile, expect the default one
        RequestSpecification request = RestAssured.given();
        Response response = request.get(getPathFromIri(annoIri));
        assertNotNull(response, "Could not get annotation response");
        assertEquals(AnnotationConstants.GET_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                "Annotation could not be fetched");
        String formatString = getDefaultFormatString(Type.ANNOTATION);
        checkHeader(response, "Content-Type", formatString);
        int index = response.getBody().asString().indexOf("\"body\"");
        assertNotEqual(-1, index, "body was not compacted as expected");
        // Request annotation without any given profile, expect an expanded annotation
        request = RestAssured.given();
        formatString = "application/ld+json";
        request.accept(formatString);
        response = request.get(getPathFromIri(annoIri));
        assertNotNull(response, "Could not get annotation response");
        assertEquals(AnnotationConstants.GET_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                "Annotation could not be fetched");
        checkHeader(response, "Content-Type", formatString);
        index = response.getBody().asString().indexOf("\"body\"");
        assertEquals(-1, index, "body was compacted but expected expanded");
        // Request annotation with container profiles, expect both listed, the layout does not change
        request = RestAssured.given();
        formatString = getDefaultFormatString(Type.CONTAINER);
        request.accept(formatString);
        System.err.println(formatString);
        response = request.get(getPathFromIri(annoIri));
        assertNotNull(response, "Could not get annotation response");
        assertEquals(AnnotationConstants.GET_ANNOTATION_SUCCESS_CODE, response.getStatusCode(),
                "Annotation could not be fetched");
        checkHeader(response, "Content-Type", formatString);
        index = response.getBody().asString().indexOf("\"body\"");
        assertNotEqual(-1, index, "body was not compacted as expected");
    }

    /**
     * Test content negotiation container.
     */
    @Test
    public void testContentNegotiationContainer() {
        if (WapServerConfig.getInstance().shouldAlwaysAddDefaultProfilesToJsonLdRequests()) {
            return; // we cannot test under this circumstance
        }
        // Create a container with a subcontainer and an annotation.
        // Then fetch the container with different profiles requested
        // and check the reponses
        final String containerIri = createDefaultContainer(null); // in the root container
        final String name = extractContainerName(containerIri);
        createDefaultAnnotation(name + "/");
        createDefaultContainer(name + "/");
        // Request container without any given accept header profile, expect the default ones
        RequestSpecification request = RestAssured.given();
        Response response = request.get(name + "/");
        assertNotNull(response, "Could not get container response");
        assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
                "Container could not be fetched");
        String formatString = getDefaultFormatString(Type.CONTAINER);
        checkHeader(response, "Content-Type", formatString);
        System.err.println(response.getBody().asString());
        int index = response.getBody().asString().indexOf("\"contains\"");
        assertNotEqual(-1, index, "contains was not compacted as expected");
        index = response.getBody().asString().indexOf("\"body\"");
        assertNotEqual(-1, index, "body was not compacted as expected");
        // Request container without any given profile, expect an expanded container and annotation (within first)
        request = RestAssured.given();
        formatString = "application/ld+json";
        request.accept(formatString);
        response = request.get(name + "/");
        assertNotNull(response, "Could not get container response");
        assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
                "Container could not be fetched");
        checkHeader(response, "Content-Type", formatString);
        index = response.getBody().asString().indexOf("\"body\"");
        assertEquals(-1, index, "body was compacted but expected expanded");
        index = response.getBody().asString().indexOf("\"contains\"");
        assertEquals(-1, index, "contains was compacted but expected expanded");
        // Request container with anno profile
        request = RestAssured.given();
        formatString = getDefaultFormatString(Type.ANNOTATION);
        request.accept(formatString);
        response = request.get(name + "/");
        assertNotNull(response, "Could not get container response");
        assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
                "Container could not be fetched");
        checkHeader(response, "Content-Type", formatString);
        index = response.getBody().asString().indexOf("\"contains\"");
        assertEquals(-1, index, "contains was compacted but expected expanded");
        index = response.getBody().asString().indexOf("\"body\"");
        assertNotEqual(-1, index, "body was not compacted as expected");
        // Request container with ldp profile
        request = RestAssured.given();
        formatString = "application/ld+json;profile=\"http://www.w3.org/ns/ldp.jsonld\"";
        request.accept(formatString);
        response = request.get(name + "/");
        assertNotNull(response, "Could not get container response");
        assertEquals(ContainerConstants.GET_CONTAINER_SUCCESS_CODE, response.getStatusCode(),
                "Container could not be fetched");
        checkHeader(response, "Content-Type", formatString);
        index = response.getBody().asString().indexOf("\"contains\"");
        assertNotEqual(-1, index, "contains was not compacted as expected");
        index = response.getBody().asString().indexOf("\"body\"");
        assertEquals(-1, index, "body was compacted but expected expanded");
    }
}
