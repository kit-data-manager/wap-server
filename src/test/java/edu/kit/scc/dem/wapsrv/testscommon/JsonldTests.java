package edu.kit.scc.dem.wapsrv.testscommon;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaDataset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdConsts;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import edu.kit.scc.dem.wapsrv.app.CorsConfiguration;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdFormatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.LdpVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRdfBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JsonldTests.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {WapServerConfig.class, JsonLdProfileRegistry.class, FormatRegistry.class, JsonLdFormatter.class})
@ExtendWith(HoverflyExtension.class)
@HoverflySimulate(source = @HoverflySimulate.Source(value = "w3c_simulation.json", type = HoverflySimulate.SourceType.DEFAULT_PATH))
@ActiveProfiles("test")
@Tag("old")
public class JsonldTests {

    private static final Logger logger = LoggerFactory.getLogger(JsonldTests.class);

    /**
     * Disable remote context loading again.
     */
    @AfterAll
    protected static final void afterAll() {
        // disable remote context loading afterwards, done here to be executed even
        // if tests failed
        System.setProperty(DocumentLoader.DISALLOW_REMOTE_CONTEXT_LOADING, "false");
    }

    /**
     * Format with context.
     *
     * @throws JsonLdError A JSON LD error exception
     * @throws IOException A I/O exception
     */
    @Test
    public void formatingWithContext() throws JsonLdError, IOException {
        System.setProperty(DocumentLoader.DISALLOW_REMOTE_CONTEXT_LOADING, "false");
        JenaRdfBackend rdfLib = new JenaRdfBackend();
        IRI contIri = rdfLib.getRdf().createIRI("http://localhost:8080/wap/container1/");
        // IRI annoIri = rdfLib.getRdf().createIRI(contIri.getIRIString() + "anno1");
        String currentDirectory;
        File file = new File(".");
        currentDirectory = file.getAbsolutePath();
        logger.trace("Current working directory : " + currentDirectory);
        JenaDataset annoDs = (JenaDataset) rdfLib.readFromFile(
                currentDirectory + "/src/main/resources/testdata/annotations/example21.jsonld", Format.JSON_LD);
        annoDs.getGraph().iterate().forEach(t -> {
            logger.trace(t.getSubject().ntriplesString() + " " + t.getPredicate().ntriplesString() + " "
                    + t.getObject().ntriplesString());
        });
        String jenaOutput = rdfLib.getOutput(annoDs, Format.NQUADS);
        logger.trace("**** Jena Output:  " + jenaOutput);
        // JsonLdProfileRegistry profileRegistry = JsonLdProfileRegistry.getInstance();
        // JsonLdOptions options = profileRegistry.getJsonLdOptions();
        Graph contGraph = rdfLib.getRdf().createGraph();
        contGraph.add(contIri, RdfVocab.type, LdpVocab.basicContainer);
        Object jsonObject = JsonLdProcessor.fromRDF(jenaOutput);
        final JsonLdOptions options = new JsonLdOptions();
        options.format = JsonLdConsts.APPLICATION_NQUADS;
        options.setCompactArrays(true);
        Object frame = JsonUtils.fromString("{\n" + "  \"@context\": \"http://www.w3.org/ns/anno.jsonld\",\n"
                + "  \"@omitDefault\": true,\n" + "  \"type\": \"Annotation\",\n" + "  \"via\": {\"@embed\": false},\n"
                + "  \"canonical\": {\"@embed\": false},\n" + "  \"rights\": {\"@embed\": false},\n"
                + "  \"motivation\": {\"@embed\": false},\n" + "  \"body\": {\"@embed\": true},\n"
                + "  \"target\": {\"@embed\": true},\n" + "  \"creator\": {\"@embed\": true},\n"
                + "  \"generator\": {\"@embed\": true},\n" + "  \"audience\": {\"@embed\": true}\n" + "}");
        final Map<String, Object> framed = JsonLdProcessor.frame(jsonObject, frame, options);
        final List<String> newContexts = new LinkedList<>();
        newContexts.add("http://www.w3.org/ns/anno.jsonld");
        final Map<String, Object> compacted = JsonLdProcessor.compact(framed, newContexts, options);
        logger.trace("\n\nAfter compact:");
        logger.trace(JsonUtils.toPrettyString(compacted));
    }
}
