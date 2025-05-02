package edu.kit.scc.dem.wapsrv.testscommon;

import static org.hamcrest.MatcherAssert.assertThat;
import java.util.Calendar;
import org.apache.commons.rdf.api.IRI;
import org.apache.jena.commonsrdf.JenaCommonsRDF;
import org.apache.jena.commonsrdf.impl.JenaDataset;
import org.apache.jena.commonsrdf.JenaRDF;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.DatasetGraph;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.EtagFactory;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.ModelFactory;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfModelFactory;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AnnoVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.DcTermsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.LdpVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.WapVocab;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaDataBase;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRdfBackend;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TestJenaRespository.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WapServerConfig.class, JenaRepository.class, EtagFactory.class, RdfModelFactory.class,
    JenaRdfBackend.class, JenaDataBase.class})
@EnableConfigurationProperties
@Tag("old")
@ActiveProfiles("test")
@Disabled
public class TestJenaRespository {

    private static final Logger logger = LoggerFactory.getLogger(TestJenaRespository.class);

    private String rootContainerIri;
    private Dataset dataBase;
    @Autowired
    private JenaRepository repository;
    @Autowired
    private EtagFactory etagFactory;
    @Autowired
    private ModelFactory modelFactory;

    @Autowired
    private void setWapServerConfig(WapServerConfig config) {
        rootContainerIri = config.getRootContainerIri();
    }

    /**
     * Set up test.
     *
     * @throws Exception A general exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        JenaDataBase jenaDb = new JenaDataBase();
        jenaDb.init(null, rootContainerIri); // in memory
        dataBase = jenaDb.getDataBase();
        repository.setDataBase(dataBase);
        dataBase.begin(ReadWrite.WRITE);
        // fill database with some test data
        Model modelRootContainer = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        modelRootContainer.createResource(rootContainerIri,
                modelRootContainer.createResource(LdpVocab.basicContainer.getIRIString()));
        modelRootContainer.addLiteral(modelRootContainer.getResource(rootContainerIri),
                modelRootContainer.createProperty(WapVocab.etag.getIRIString()),
                modelRootContainer.createLiteral("INITIAL-ROOT-ETAG"));
        modelRootContainer.addLiteral(modelRootContainer.getResource(rootContainerIri),
                modelRootContainer.createProperty(DcTermsVocab.modified.getIRIString()),
                modelRootContainer.createTypedLiteral(Calendar.getInstance()));
        // sub container
        Model mc1 = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        Model mc2 = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        Model mc3 = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        Model modelContainerDeleted = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        String cs1 = rootContainerIri + "container1/";
        String cs2 = rootContainerIri + "container2/";
        String cs3 = rootContainerIri + "container3/";
        String containerDelectedString = rootContainerIri + "containerDeleted/";
        addTriple(mc1, cs1, RdfVocab.type.getIRIString(), LdpVocab.basicContainer.getIRIString());
        addTriple(mc2, cs2, RdfVocab.type.getIRIString(), LdpVocab.basicContainer.getIRIString());
        addTriple(mc3, cs3, RdfVocab.type.getIRIString(), LdpVocab.basicContainer.getIRIString());
        mc1.addLiteral(mc1.getResource(cs1), mc1.createProperty(WapVocab.etag.getIRIString()),
                mc1.createLiteral(etagFactory.generateEtag()));
        mc2.addLiteral(mc2.getResource(cs2), mc2.createProperty(WapVocab.etag.getIRIString()),
                mc2.createLiteral("TEST-ETAG-1234"));
        mc3.addLiteral(mc3.getResource(cs3), mc3.createProperty(WapVocab.etag.getIRIString()),
                mc3.createLiteral("TEST-ETAG-CONT3"));
        mc1.addLiteral(mc1.getResource(cs1), mc1.createProperty(DcTermsVocab.modified.getIRIString()),
                mc1.createTypedLiteral(Calendar.getInstance()));
        mc2.addLiteral(mc2.getResource(cs2), mc2.createProperty(DcTermsVocab.modified.getIRIString()),
                mc2.createTypedLiteral(Calendar.getInstance()));
        mc3.addLiteral(mc3.getResource(cs3), mc3.createProperty(DcTermsVocab.modified.getIRIString()),
                mc3.createTypedLiteral(Calendar.getInstance()));
        Resource deletedContainer = modelContainerDeleted.createResource(rootContainerIri + "containerDeleted/",
                modelContainerDeleted.createResource(LdpVocab.basicContainer.getIRIString()));
        // put container into root container sequence
        Seq seq = modelRootContainer.createSeq(rootContainerIri + "#containers");
        seq.add(modelRootContainer.getResource(cs1));
        seq.add(modelRootContainer.getResource(cs2));
        seq.add(modelRootContainer.getResource(cs3));
        deletedContainer.addLiteral(modelContainerDeleted.createProperty(WapVocab.deleted.getIRIString()), true);
        // container to block a delete
        Model mcBD = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        String csBD = rootContainerIri + "container3/bockDelete/";
        addTriple(mcBD, csBD, RdfVocab.type.getIRIString(), LdpVocab.basicContainer.getIRIString());
        mcBD.addLiteral(mcBD.getResource(csBD), mcBD.createProperty(WapVocab.etag.getIRIString()),
                mcBD.createLiteral("TEST-ETAG-BLDE"));
        mcBD.addLiteral(mcBD.getResource(csBD), mcBD.createProperty(DcTermsVocab.modified.getIRIString()),
                mcBD.createTypedLiteral(Calendar.getInstance()));
        Seq seqContainer3 = mc3.createSeq(Container.toContainerSeqIriString(cs3));
        seqContainer3.add(mc3.getResource(csBD));
        // annotation
        Model modelAnnotation1 = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        String annotation1String = rootContainerIri + "container3/anno1";
        addTriple(modelAnnotation1, annotation1String, RdfVocab.type.getIRIString(), AnnoVocab.annotation.getIRIString());
        modelAnnotation1.addLiteral(modelAnnotation1.getResource(annotation1String),
                modelAnnotation1.createProperty(WapVocab.etag.getIRIString()),
                modelAnnotation1.createLiteral("TEST-ETAG-ANNO1"));
        modelAnnotation1.addLiteral(modelAnnotation1.getResource(annotation1String),
                modelAnnotation1.createProperty(DcTermsVocab.modified.getIRIString()),
                modelAnnotation1.createTypedLiteral(Calendar.getInstance()));
        Seq cont3AnnoSeq = mc3.createSeq(Container.toAnnotationSeqIriString(cs3));
        cont3AnnoSeq.add(mc3.createResource(annotation1String));
        dataBase.addNamedModel(rootContainerIri, modelRootContainer);
        dataBase.addNamedModel(cs1, mc1);
        dataBase.addNamedModel(cs2, mc2);
        dataBase.addNamedModel(cs3, mc3);
        dataBase.addNamedModel(csBD, mcBD);
        dataBase.addNamedModel(containerDelectedString, modelContainerDeleted);
        dataBase.addNamedModel(annotation1String, modelAnnotation1);
        dataBase.commit();
        dataBase.end();
    }

    private Model addTriple(Model model, String s, String p, String o) {
        Resource subject = model.createResource(s);
        Property predicate = model.createProperty(p);
        Resource object = model.createResource(o);
        return model.add(subject, predicate, object);
    }

    /**
     * Tear down test.
     *
     * @throws Exception A general exception
     */
    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Test initialize the DB.
     */
    @Test
    public void testInitDb() {
        dataBase.begin(ReadWrite.READ);
        dumpDatabase("InitDb Content:");
        assertThat(dataBase.getUnionModel().listStatements().toList().size(), Is.is(28));
        dataBase.end();
    }

    private void dumpDatabase(String description) {
        logger.trace("************* " + description + " *************");
        StmtIterator iterator = dataBase.getUnionModel().listStatements();
        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            logger.trace(statement.asTriple().toString());
        }
    }

    /**
     * Test delete annotation.
     */
    @Test
    public void testDeleteAnnotation() {
        // DOTEST write the test for this method
    }

    /**
     * Test get annotation.
     */
    @Test
    public void testGetAnnotation() {
        // DOTEST write the test for this method
    }

    /**
     * Test get page.
     */
    @Test
    public void testGetPage() {
        dataBase.begin(ReadWrite.READ);
        // Page page = repository.getPage(containerIri, 0, false);
        logger.trace("--------------- RDF -------------------");
        // String nquads = page.toString(Format.NQUADS);
        // logger.trace(nquads);
        logger.trace("--------------- Framed JSONLD -------------------");
        // String result = generateJSONLD(nquads, FRAME_ANNOTATIONPAGE);
        // logger.trace(result);
        dataBase.end();
        // DOTEST write the test for this method
    }

    /**
     * Test post container container already exist.
     */
    @Test
    public void testPostContainerContainerAlreadyExist() {
        dataBase.begin(ReadWrite.WRITE);
        JenaRDF rdf = new JenaRDF();
        DatasetGraph containerDs = DatasetFactory.create().asDatasetGraph();
        //JenaDataset containerDs = rdf.createDataset();
        Node containerNode = JenaCommonsRDF.toJena(rdf.createIRI(rootContainerIri + "container1/"));
        Node etagNode = NodeFactory.createLiteral(etagFactory.generateEtag());
        containerDs.add(null, containerNode, JenaCommonsRDF.toJena(RdfVocab.type), JenaCommonsRDF.toJena(LdpVocab.basicContainer));
        containerDs.add(null, containerNode, JenaCommonsRDF.toJena(RdfVocab.type), JenaCommonsRDF.toJena(AsVocab.orderedCollection));
        containerDs.add(null, containerNode, JenaCommonsRDF.toJena(WapVocab.etag), etagNode);
        modelFactory.createContainer(JenaCommonsRDF.fromJena(containerDs));
        // Assertions.assertThrows(ResourceExistsException.class, () -> {repository.postContainer(rootContainerIri,
        // container);});
        logger.trace("************* After Post Container: *************");
        dataBase.commit();
        dataBase.end();
    }

    /**
     * Test update annotation.
     */
    @Test
    public void testUpdateAnnotation() {
        // DOTEST write the test for this method
    }

    /**
     * Test if IRI is deleted.
     */
    @Test
    public void testIsIriDeleted() {
        dataBase.begin(ReadWrite.READ);
        // assertThat(repository.isIriDeleted(rootContainerIri+"container1/"), Is.is(false));
        // assertThat(repository.isIriDeleted(rootContainerIri+"containerDeleted/"), Is.is(true));
        dataBase.end();
        // DOTEST write the test for this method
    }

    /**
     * Test backup database.
     */
    @Test
    public void testBackupDatabase() {
        // DOTEST write the test for this method
    }

    /**
     * Test if there are subcontainers.
     */
    @Test
    public void testHasSubcontainers() {
        // DOTEST write the test for this method
    }
}
