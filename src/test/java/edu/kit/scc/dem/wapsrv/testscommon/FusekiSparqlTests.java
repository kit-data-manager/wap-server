package edu.kit.scc.dem.wapsrv.testscommon;

import java.io.FileNotFoundException;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.system.JenaSystem;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * FusekiSparqlTests
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@Tag("old")
public class FusekiSparqlTests {
   /**
    * Test context load.
    */
   @Test
   public void contextLoads() {
   }

   /**
    * Test data load.
    *
    * @throws FileNotFoundException
    *                               A FileNotFound exception
    */
   @Test
   public void testdataLoads() throws FileNotFoundException {
   }

   private Resource getTestdata() throws FileNotFoundException {
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
      return res;
   }

   private Dataset createDataset() throws FileNotFoundException {
      JenaSystem.init();
      Resource testdata = this.getTestdata();
      Dataset ds = TDB2Factory.createDataset();
      ds.begin(ReadWrite.WRITE);
      ds.getDefaultModel().add(testdata.getModel());
      // ds.addNamedModel("http://data.dem.scc.kit.edu/wap/persons/person1", readModel);
      ds.commit();
      ds.end();
      return ds;
   }

   /**
    * Test Dataset read.
    *
    * @throws FileNotFoundException
    *                               A FileNotFound exception
    */
   @Test
   public void datasetReads() throws FileNotFoundException {
      JenaSystem.init();
      Dataset ds = this.createDataset();
      ds.begin(ReadWrite.READ);
      Resource model2 = ds.getDefaultModel().getResource("http://data.dem.scc.kit.edu/wap/persons/person1");
      RDFDataMgr.write(System.out, model2.getModel(), RDFFormat.JSONLD_COMPACT_PRETTY);
      ds.end();
   }

   /**
    * Test Fuseki basics.
    *
    * @throws FileNotFoundException
    *                               A FileNotFound exception
    */
   @Test
   public void fusekiBasics() throws FileNotFoundException {
      Dataset ds = this.createDataset();
      /*
       * DatasetGraph dsg = ds.asDatasetGraph(); DataService dataService = new DataService(dsg) ;
       * dataService.addEndpoint(OperationName.Quads_RW, ""); dataService.addEndpoint(OperationName.Query, "");
       * dataService.addEndpoint(OperationName.Update, "");
       */
      FusekiServer server1 = FusekiServer.create().setPort(3339).add("/write", ds, true).build();
      server1.start();
      FusekiServer server2 = FusekiServer.create().setPort(3338).add("/read", ds, false)// false -> read-only
            .build();
      server2.start();
      // Write transaction.
      // Txn.execWrite(dsg, ()->RDFDataMgr.read(dsg, "D.trig")) ;
      // Read transaction.
      /*
       * Txn.execRead(dsg, ()->{ Dataset ds = DatasetFactory.wrap(dsg) ; try (QueryExecution qExec =
       * QueryExecutionFactory.create("SELECT * { ?s  ?o}", ds) ) { ResultSet rs = qExec.execSelect() ;
       * ResultSetFormatter.out(rs) ; } }) ;
       */
      server1.stop();
      server1.join();
      server2.stop();
      server2.join();
   }
}
