package edu.kit.scc.dem.wapsrv.model.rdf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.Calendar;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRdfBackend;
import java.util.TimeZone;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests the class RdfUtilities
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {JenaRdfBackend.class})
@EnableConfigurationProperties
@ActiveProfiles("test")
class RdfUtilitiesTest {

    /**
     * Test rename node IRI in graph blank node or IRI in blank node.
     */
    @Test
    final void testRenameNodeIriGraphBlankNodeOrIRIBlankNodeOrIRI() {
        // DOTEST write the test for this method
    }

    /**
     * Test rename node IRI dataset in blank node or IRI in blank node.
     */
    @Test
    final void testRenameNodeIriDatasetBlankNodeOrIRIBlankNodeOrIRI() {
        // DOTEST write the test for this method
    }

    /**
     * Test clone graph RDF.
     */
    @Test
    final void testCloneGraphRDF() {
        // DOTEST write the test for this method
    }

    /**
     * Test clone dataset RDF.
     */
    @Test
    final void testCloneDatasetRDF() {
        // DOTEST write the test for this method
    }

    /**
     * Test N string to string.
     */
    @Test
    final void testNStringToString() {
        // DOTEST write the test for this method
    }

    /**
     * Test get sub dataset.
     */
    @Test
    final void testGetSubDataset() {
        // DOTEST write the test for this method
    }

    /**
     * Test RDF literal from calendar.
     */
    @Test
    final void testRdfLiteralFromCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(1979, 3, 29, 13, 56, 13); // month 0-value based 3-> April, 0-> January
        Literal literalDefault = RdfUtilities.rdfLiteralFromCalendar(cal, new SimpleRDF());
        assertThat(literalDefault.ntriplesString(), startsWith("\"1979-04-29T13:56:13Z\""));
        cal.set(2018, 8, 11, 11, 0, 0); // month 0-value based 8-> September, 0-> January
        Literal literalDefault2 = RdfUtilities.rdfLiteralFromCalendar(cal, new SimpleRDF());
        assertThat(literalDefault2.ntriplesString(), startsWith("\"2018-09-11T11:00:00Z\""));
    }

    /**
     * Test N string to string.
     */
    @Test
    final void testNStringtoString() {
        assertThat(RdfUtilities.nStringToString("\"http://www.test.de/\""), is("http://www.test.de/"));
        assertThat(RdfUtilities.nStringToString("<http://www.test.de/>"), is("http://www.test.de/"));
        assertThat(RdfUtilities.nStringToString("http://www.test.de/"), is("http://www.test.de/"));
    }
}
