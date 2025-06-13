package edu.kit.scc.dem.wapsrv.testscommon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import org.apache.commons.rdf.api.*;
import org.apache.jena.commonsrdf.JenaCommonsRDF;
import org.apache.jena.commonsrdf.JenaRDF;
import org.apache.commons.rdf.jsonldjava.JsonLdGraph;
import org.apache.commons.rdf.jsonldjava.JsonLdRDF;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb2.TDB2Factory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdConsts;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfUtilities;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRdfBackend;
import org.apache.jena.sparql.core.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;

/**
 * ApacheCommonsRdfTests
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@ExtendWith(HoverflyExtension.class)
@HoverflySimulate(source = @HoverflySimulate.Source(value = "w3c_simulation.json", type = HoverflySimulate.SourceType.DEFAULT_PATH))
@Tag("old")
public class ApacheCommonsRdfTests {

    private static final Logger logger = LoggerFactory.getLogger(ApacheCommonsRdfTests.class);

    private static final int SPEED_TEST_COUNT = 100;

    /**
     * Test context load.
     */
    @Test
    public void contextLoads() {
    }

    /**
     * Setup tests.
     */
    @BeforeAll
    static void setupTests() {
        System.setProperty(DocumentLoader.DISALLOW_REMOTE_CONTEXT_LOADING, "false");
    }

    /**
     * Teardown tests.
     */
    @AfterAll
    static void teardownTests() {
        System.setProperty(DocumentLoader.DISALLOW_REMOTE_CONTEXT_LOADING, "false");
    }

    /**
     * Test RDF simple annotation.
     *
     * @throws JsonLdError A JSON LD error exception
     * @throws JsonGenerationException A JSON generation exception
     * @throws IOException A I/O exception
     */
    @Test
    public void rdfSimpleAnnotation() throws JsonLdError, JsonGenerationException {
        JenaRdfBackend rdfLib = new JenaRdfBackend();
        Dataset dataset = rdfLib.readFromFile("src/main/resources/testdata/PAGE2017XML_Tristrant_VD16T1963-008.jsonld",
                Format.JSON_LD);
        IRI iri = rdfLib.getRdf().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        IRI type = rdfLib.getRdf().createIRI("http://www.w3.org/ns/oa#Annotation");
        IRI created = rdfLib.getRdf().createIRI("http://purl.org/dc/terms/created");
        Literal createdString = rdfLib.getRdf().createLiteral("2018-06-5T00:23:00Z");
        BlankNodeOrIRI oldIdIri = null;
        Graph graph = dataset.getGraph();
        for (org.apache.commons.rdf.api.Triple t : graph.iterate(null, iri, type)) {
            Log.info(this, t.getSubject().toString() + " , " + t.getPredicate().toString() + " , "
                    + t.getObject().toString());
            oldIdIri = t.getSubject();
            if (graph.contains(t.getSubject(), created, null)) {
                Log.info(this, "the created triple for this annotation already exists, DELETING...");
                graph.remove(t.getSubject(), created, null);
            }
            graph.add(t.getSubject(), created, createdString);
        }
        IRI newIdIri = rdfLib.getRdf().createIRI("http://wapserver.dem.scc.kit.edu/tristrant/anno1");
        RdfUtilities.renameNodeIri(graph, oldIdIri, newIdIri);
        logger.trace(rdfLib.getOutput(dataset, Format.JSON_LD));
        JsonLdRDF jsonLdRdf = new JsonLdRDF();
        JsonLdGraph jsonLdGraph = jsonLdRdf.createGraph();
        for (Triple t : graph.iterate()) {
            jsonLdGraph.add(t);
        }
        final JsonLdOptions options = new JsonLdOptions();
        options.format = JsonLdConsts.APPLICATION_NQUADS;
        options.setCompactArrays(true);
        logger.trace("Before compact");
        try {
            logger.trace(JsonUtils.toPrettyString(jsonLdGraph));
        } catch (IOException e) {
            logger.trace(e.getMessage());
        }
        final List<String> newContexts = new LinkedList<>();
        newContexts.add("http://www.w3.org/ns/anno.jsonld");
        final Map<String, Object> compacted = JsonLdProcessor.compact(jsonLdGraph, newContexts, options);
        logger.trace("\n\nAfter compact:");
        try {
            logger.trace(JsonUtils.toPrettyString(compacted));
        } catch (IOException e) {
            logger.trace(e.getMessage());
        }
    }

    /**
     * JENA backed speed test.
     *
     * @throws FileNotFoundException File not found exception
     */
    @Test
    public void jenaBackedSpeedTest() throws FileNotFoundException {
        RDF factory = new JenaRDF();
        //org.apache.jena.query.Dataset sourceGraph = DatasetFactory.create();
        //JenaGraph sourceGraph = factory.createGraph();
        //JenaGraph sourceGraph = JenaCommonsRDF.toJena(datasetGraph.getGraph());
        Model model = RDFDataMgr.loadModel("src/main/resources/testdata/PAGE2017XML_Tristrant_VD16T1963-008.jsonld");
        //sourceGraph.asJenaModel().read("src/main/resources/testdata/PAGE2017XML_Tristrant_VD16T1963-008.jsonld");
        Node typeAnnotation = JenaCommonsRDF.toJena(factory.createIRI("http://www.w3.org/ns/oa#Annotation"));
        Node rdfType = JenaCommonsRDF.toJena(factory.createIRI(org.apache.jena.vocabulary.RDF.type.getURI()));
        long timeStartPrep = System.currentTimeMillis();
        //Iterable<Triple> it = sourceGraph.iterate(null, rdfType, typeAnnotation);
        BlankNodeOrIRI node = null;
        org.apache.jena.graph.Graph sourceGraph = model.getGraph();
        Optional<org.apache.jena.graph.Triple> triple = sourceGraph.stream(null, rdfType, typeAnnotation).findFirst();
        node = (BlankNodeOrIRI) (JenaCommonsRDF.fromJena(factory, triple.get().getSubject()));
        logger.info("Anno ROOT: " + triple.get().getSubject().toString() + " Object: " + triple.get().getObject().toString());
        // fill a List with models to add
        ArrayList<Model> modelList = new ArrayList<>();
        for (int i = 0; i < SPEED_TEST_COUNT; i++) {
            Graph newGraph = RdfUtilities.clone(JenaCommonsRDF.fromJena(sourceGraph), factory);
            RdfUtilities.renameNodeIri(newGraph, node,
                    factory.createIRI("http://wapserver.dem.scc.kit.edu/tristrant/anno" + i));
            modelList.add(ModelFactory.createModelForGraph(JenaCommonsRDF.toJena(newGraph)));
        }
        long durationPrep = System.currentTimeMillis() - timeStartPrep;
        logger.info("---------- Prepare of " + SPEED_TEST_COUNT + " Annos in millis: " + durationPrep);
        org.apache.jena.query.Dataset ds = TDB2Factory.connectDataset("temp/tdb2/test.tdb");
        ds.begin(ReadWrite.WRITE);
        ds.getDefaultModel().removeAll();
        ds.commit();
        ds.end();
        long timeStart = System.currentTimeMillis();
        ds.begin(ReadWrite.WRITE);
        for (int i = 0; i < SPEED_TEST_COUNT; i++) {
            ds.getDefaultModel().add(modelList.get(i));
        }
        ds.commit();
        ds.end();
        long duration = System.currentTimeMillis() - timeStart;
        logger.info("---------- Write " + SPEED_TEST_COUNT + " Annos to Database millis: " + duration);
        Model targetModel = ModelFactory.createDefaultModel();
        Model model2 = ds.getDefaultModel();
        ds.begin(ReadWrite.READ);
        logger.info("Triples # in DB: " + model2.listStatements().toList().size());
        ds.end();
        long timeStartRead = System.currentTimeMillis();
        ds.begin(ReadWrite.READ);
        for (int i = 0; i < SPEED_TEST_COUNT; i++) {
            Model foundModel = model2.getResource("http://wapserver.dem.scc.kit.edu/tristrant/anno" + i).getModel();
            targetModel.add(foundModel);
        }
        ds.end();
        long durationRead = System.currentTimeMillis() - timeStartRead;
        logger.info("---------- read " + SPEED_TEST_COUNT + " Annos from Database millis: " + durationRead);
    }
}
