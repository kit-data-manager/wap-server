package edu.kit.scc.dem.wapsrv.testscommon;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.EtagFactory;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.LdpVocab;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ApacheJenaTests
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {EtagFactory.class}) // It will not start without at least on class expressed here
@ActiveProfiles("test")
@Tag("old")
public class ApacheJenaTests {

    private static final Logger logger = LoggerFactory.getLogger(ApacheJenaTests.class);

    private static final int SPEED_TEST_COUNT = 100;

    /**
     * Test context loading.
     */
    @Test
    public void contextLoads() {
        // DOTEST write the test for this method
    }

    /**
     * Test for not existing file.
     *
     * @throws FileNotFoundException File not found
     */
    @Test
    public void jenaBasics() throws FileNotFoundException {
        JenaSystem.init();
        ModelFactory.createDefaultModel();
        Model readModel = RDFDataMgr.loadModel("src/main/resources/testdata/PAGE2017XML_Tristrant_VD16T1963-008.jsonld");
        // RDFDataMgr.write(new FileOutputStream("test.N3"), readModel, RDFFormat.NTRIPLES) ;
        Resource annoRes = readModel.getResource("http://www.w3.org/ns/oa#Annotation");
        ResIterator it = readModel.listSubjectsWithProperty(RDF.type, annoRes);
        Resource res = null;
        // Log.info(this, "Root Count: " + it.toList().size());
        while (it.hasNext()) {
            res = it.next();
            Log.info(this, "Anno ROOT: " + res.getLocalName() + " Object: " + res.getPropertyResourceValue(RDF.type));
            res.removeAll(DCTerms.created);
            res.addLiteral(DCTerms.created, "2018-06-5T00:23:00Z");
            res = ResourceUtils.renameResource(res, "http://wapserver.dem.scc.kit.edu/tristrant/anno1");
        }
        Dataset ds = TDB2Factory.createDataset();
        ds.begin(ReadWrite.WRITE);
        ds.getDefaultModel().add(res.getModel());
        // ds.addNamedModel("http://data.dem.scc.kit.edu/wap/persons/person1", readModel);
        ds.commit();
        ds.end();
        ds.begin(ReadWrite.READ);
        Model model2 = ds.getDefaultModel();
        RDFDataMgr.write(System.out, model2.getGraph(), RDFFormat.JSONLD_PRETTY);
        Log.info(this, "Triples # in DB: " + model2.listStatements().toList().size());
        ds.end();
    }

    /**
     * Test output of JENA
     *
     * @throws FileNotFoundException File not found
     */
    @Test
    public void jenaOutput() throws FileNotFoundException {
        JenaSystem.init();
        ModelFactory.createDefaultModel();
        Model readModel = RDFDataMgr.loadModel("src/main/resources/testdata/PAGE2017XML_Tristrant_VD16T1963-008.jsonld");
        // RDFDataMgr.write(new FileOutputStream("test.N3"), readModel, RDFFormat.NTRIPLES) ;
        Resource annoRes = readModel.getResource("http://www.w3.org/ns/oa#Annotation");
        ResIterator it = readModel.listSubjectsWithProperty(RDF.type, annoRes);
        Resource res = null;
        // Log.info(this, "Root Count: " + it.toList().size());
        while (it.hasNext()) {
            res = it.next();
            Log.info(this, "Anno ROOT: " + res.getLocalName() + " Object: " + res.getPropertyResourceValue(RDF.type));
            res.removeAll(DCTerms.created);
            res.addLiteral(DCTerms.created, "2018-06-5T00:23:00Z");
            res = ResourceUtils.renameResource(res, "http://wapserver.dem.scc.kit.edu/tristrant/anno1");
        }
        RDFDataMgr.write(System.out, readModel, RDFFormat.JSONLD_PRETTY);
    }

    /**
     * Test the performance of JENA.
     *
     * @throws FileNotFoundException File not found
     */
    @Test
    public void jenaSpeedTest() throws FileNotFoundException {
        JenaSystem.init();
        // Model model= ModelFactory.createDefaultModel();
        Model readModel = RDFDataMgr.loadModel("src/main/resources/testdata/PAGE2017XML_Tristrant_VD16T1963-008.jsonld");
        // RDFDataMgr.write(new FileOutputStream("test.N3"), readModel, RDFFormat.NTRIPLES) ;
        Resource annoRes = readModel.getResource("http://www.w3.org/ns/oa#Annotation");
        long timeStartPrep = System.currentTimeMillis();
        ResIterator it = readModel.listSubjectsWithProperty(RDF.type, annoRes);
        Resource res = null;
        // Log.info(this, "Root Count: " + it.toList().size());
        while (it.hasNext()) {
            res = it.next();
            Log.info(this, "Anno ROOT: " + res.getLocalName() + " Object: " + res.getPropertyResourceValue(RDF.type));
        }
        // fill a List with models to add
        List<Model> modelList = new ArrayList<Model>();
        for (int i = 0; i < SPEED_TEST_COUNT; i++) {
            Resource resNew = ResourceUtils.renameResource(res, "http://wapserver.dem.scc.kit.edu/tristrant/anno" + i);
            modelList.add(resNew.getModel());
        }
        long durationPrep = System.currentTimeMillis() - timeStartPrep;
        Log.info(this, "---------- Prepare of " + SPEED_TEST_COUNT + " Annos in millis: " + durationPrep);
        Dataset ds = TDB2Factory.connectDataset("temp/tdb2/test.tdb");
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
        Log.info(this, "---------- Write " + SPEED_TEST_COUNT + " Annos to Database millis: " + duration);
        Model targetModel = ModelFactory.createDefaultModel();
        Model model2 = ds.getDefaultModel();
        ds.begin(ReadWrite.READ);
        Log.info(this, "Triples # in DB: " + model2.listStatements().toList().size());
        ds.end();
        long timeStartRead = System.currentTimeMillis();
        ds.begin(ReadWrite.READ);
        for (int i = 0; i < SPEED_TEST_COUNT; i++) {
            Model foundModel = model2.getResource("http://wapserver.dem.scc.kit.edu/tristrant/anno" + i).getModel();
            targetModel.add(foundModel);
        }
        ds.end();
        long durationRead = System.currentTimeMillis() - timeStartRead;
        Log.info(this, "---------- read " + SPEED_TEST_COUNT + " Annos from Database millis: " + durationRead);
    }

    /**
     * Test uniting of models at JENA DB.
     */
    @Test
    public void jenaDatasetUnionModelTest() {
        JenaSystem.init();
        Dataset dataBase = TDB2Factory.createDataset();
        dataBase.begin(ReadWrite.WRITE);
        // generate root container:
        Model containerRootModel = ModelFactory.createDefaultModel();
        Resource basicContainer = containerRootModel.createResource(LdpVocab.basicContainer.getIRIString());
        String rootContString = "http://www.test.de/wap/";
        Resource containerRoot = containerRootModel.createResource(rootContString, basicContainer);
        Seq rootContSeq = containerRootModel.createSeq(Container.toContainerSeqIriString(containerRoot.getURI()));
        containerRootModel.createSeq(Container.toAnnotationSeqIriString(containerRoot.getURI()));
        // generate root container:
        Model container1Model = ModelFactory.createDefaultModel();
        String container1String = "http://www.test.de/wap/container1/";
        Resource container1 = container1Model.createResource(container1String, basicContainer);
        container1Model.createSeq(Container.toContainerSeqIriString(container1.getURI()));
        container1Model.createSeq(Container.toAnnotationSeqIriString(container1.getURI()));
        rootContSeq.add(container1);
        dataBase.addNamedModel(containerRoot.getURI(), containerRootModel);
        dataBase.addNamedModel(container1.getURI(), container1Model);
        dataBase.commit();
        dataBase.end();
        // reopen and check the result:...
        dataBase.begin(ReadWrite.READ);
        // dump database
        logger.trace(" -------------- union Model ----------- ");
        dataBase.getUnionModel().listStatements().forEachRemaining(s -> {
            logger.trace(s.asTriple().toString());
        });
        logger.trace(" -------------- only root Model ----------- ");
        dataBase.getNamedModel(rootContString).listStatements().forEachRemaining(s -> {
            logger.trace(s.asTriple().toString());
        });
        logger.trace(" -------------- only container1 Model ----------- ");
        dataBase.getNamedModel(container1String).listStatements().forEachRemaining(s -> {
            logger.trace(s.asTriple().toString());
        });
        dataBase.end();
    }
}
