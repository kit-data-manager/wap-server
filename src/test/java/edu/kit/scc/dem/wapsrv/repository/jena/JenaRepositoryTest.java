package edu.kit.scc.dem.wapsrv.repository.jena;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import java.util.List;
import org.apache.commons.rdf.api.RDF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.NotExistentException;
import edu.kit.scc.dem.wapsrv.model.WapObject;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfBackend;
import edu.kit.scc.dem.wapsrv.repository.TransactionRepository;

/**
 * Tests the class JenaRepository
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {JenaRepository.class, WapServerConfig.class, JenaDataBase.class, JenaRdfBackend.class})
@ActiveProfiles("test")
class JenaRepositoryTest {
   @Autowired
   private JenaRepository objJenaRepository;
   @Autowired
   private WapServerConfig objWapServerConfig;
   @Autowired
   private JenaDataBase objBaseSource;
   @Autowired
   private RdfBackend objRdfBackend;

   /**
    * Assert that JenaRepository instance is in transaction mode.
    */
   @BeforeEach
   final void beginTransaction() {
      objJenaRepository.beginTransaction(TransactionRepository.Type.Read);
   }

   /**
    * Clean up by abort transaction.
    */
   @AfterEach
   final void abortTransaction() {
      objJenaRepository.abortTransaction();
   }

   /**
    * Assert that @Autowired from Spring did work.
    */
   @Test
   final void testContextLoads() {
      assertNotNull(objJenaRepository);
      assertNotNull(objWapServerConfig);
      assertNotNull(objBaseSource);
      assertNotNull(objRdfBackend);
   }

   /**
    * Test set WAP server configuration.
    */
   @Test
   final void testSetWapServerConfig() {
      objJenaRepository.setWapServerConfig(objWapServerConfig);
   }

   /**
    * Test get WAP object.
    */
   @Test
   final void testGetWapObject() {
      String paramIri;
      org.apache.commons.rdf.api.Dataset actual;
      // test root container
      paramIri = objWapServerConfig.getRootContainerIri();
      actual = null;
      actual = objJenaRepository.getWapObject(paramIri);
      assertNotNull(actual, "Could not get Dataset WapObject for: " + paramIri);
      // test invalid container
      assertThrows(NotExistentException.class, () -> {
         objJenaRepository.getWapObject(objWapServerConfig.getRootContainerIri() + "invalidContainer9815/");
      });
   }

   /**
    * Test backup database.
    */
   @Test
   final void testBackupDatabase() {
      String actual;
      actual = null;
      actual = objJenaRepository.backupDatabase();
      assertNotNull(actual, "Could not get backupDatabase as String.");
   }

   /**
    * Test get data base.
    */
   @Test
   final void testGetDataBase() {
      org.apache.jena.query.Dataset actual;
      actual = null;
      actual = objJenaRepository.getDataBase();
      assertNotNull(actual, "Could not get dataBase as jena.query.Dataset.");
   }

   /**
    * Test set data base.
    */
   @Test
   final void testSetDataBase() {
      org.apache.jena.query.Dataset actual;
      org.apache.jena.query.Dataset expected;
      // set expected to current database
      expected = null;
      expected = objJenaRepository.getDataBase();
      assertNotNull(expected, "Could not set up expected dataBase");
      // set current database to NULL pointer
      objJenaRepository.setDataBase(null);
      actual = expected;
      actual = objJenaRepository.getDataBase();
      assertNull(actual, "Could not set dataBase to NULL.");
      // reset current database back to expected
      objJenaRepository.setDataBase(expected);
      actual = null;
      actual = objJenaRepository.getDataBase();
      assertNotNull(actual, "Could not set dataBase.");
   }

   /**
    * Test begin transaction
    */
   @Test
   final void testBeginTransaction() {
      boolean actual;
      // BeforeEach already began transaction
      // test to begin again
      actual = objJenaRepository.beginTransaction(TransactionRepository.Type.Read);
      assertFalse(actual, "Beginning twice a transaction should return false.");
      // test to begin WRITE transaction
      objJenaRepository.abortTransaction();
      actual = objJenaRepository.beginTransaction(TransactionRepository.Type.Write);
      assertTrue(actual, "Beginning a WRITE transaction should be true.");
      objJenaRepository.abortTransaction();
   }

   /**
    * Test abort transaction
    */
   @Test
   final void testAbortTransaction() {
      boolean actual;
      // BeforeEach already began transaction
      // test aborting transaction
      objJenaRepository.abortTransaction();
      // test to abort transaction when not in transaction
      objJenaRepository.abortTransaction();
      // test to begin transaction after aborting it.
      actual = objJenaRepository.beginTransaction(TransactionRepository.Type.Read);
      assertTrue(actual, "Could not begin transaction after aborting it twice.");
      objJenaRepository.abortTransaction();
   }

   /**
    * Test end transaction
    */
   @Test
   final void testEndTransaction() {
      boolean actual;
      // BeforeEach already began transaction
      // test end transaction false
      objJenaRepository.endTransaction(false);
      // test end transaction true
      objJenaRepository.endTransaction(true);
      // test end transaction WRITE
      actual = objJenaRepository.beginTransaction(TransactionRepository.Type.Write);
      assertTrue(actual, "Could not begin WRITE transaction after ending it.");
      objJenaRepository.endTransaction(true);
   }

   /**
    * Test get RDF.
    */
   @Test
   final void testGetRdf() {
      RDF actual;
      actual = null;
      actual = objJenaRepository.getRdf();
      assertNotNull(actual, "Could not get RDF.");
   }

   /**
    * Test add element to RDF sequential.
    */
   @Test
   final void testAddElementToRdfSeq() {
      String paramModelIri;
      String paramSeqIri;
      String paramObjIri;
      boolean actual;
      // begin WRITE transaction
      objJenaRepository.abortTransaction();
      actual = objJenaRepository.beginTransaction(TransactionRepository.Type.Write);
      assertTrue(actual, "Could not begin WRITE transaction.");
      // test add ghosttest03 container in root container
      paramModelIri = "http://localhost:8080/wap/";
      paramSeqIri = "http://localhost:8080/wap/#containers";
      paramObjIri = "http://localhost:8080/wap/ghosttest03/";
      objJenaRepository.addElementToRdfSeq(paramModelIri, paramSeqIri, paramObjIri);
      // test remove that element again
      objJenaRepository.removeElementFromRdfSeq(paramModelIri, paramSeqIri, paramObjIri);
      // test to remove a invalid container
      paramObjIri = "http://localhost:8080/wap/ghosttest9815/";
      objJenaRepository.removeElementFromRdfSeq(paramModelIri, paramSeqIri, paramObjIri);
      // clean up by aborting transaction
      objJenaRepository.abortTransaction();
   }

   /**
    * Test remove element from RDF sequential.
    */
   @Test
   final void testRemoveElementFromRdfSeq() {
      // tested already in testAddElementToRdfSeq
   }

   /**
    * Test count elements in sequence.
    */
   @Test
   final void testCountElementsInSeq() {
      String paramModelIri = "http://localhost:8080/wap/";
      String paramSeqIri = "http://localhost:8080/wap/#annotations";
      int actual;
      // test default, no items in sequence.
      actual = -1;
      actual = objJenaRepository.countElementsInSeq(paramModelIri, paramSeqIri);
      assertTrue(actual > -1,
            "Could not count elements in seq for modeIri: " + paramModelIri + ", seqIri: " + paramSeqIri);
   }

   /**
    * Test write object to database.
    */
   @Test
   final void testWriteObjectToDatabase() {
      String paramIri;
      org.apache.commons.rdf.api.Dataset paramWapObjectDataset;
      boolean actual;
      // begin WRITE transaction
      objJenaRepository.abortTransaction();
      actual = objJenaRepository.beginTransaction(TransactionRepository.Type.Write);
      assertTrue(actual, "Could not begin WRITE transaction.");
      // get WapObject from root container
      paramIri = objWapServerConfig.getRootContainerIri();
      paramWapObjectDataset = null;
      paramWapObjectDataset = objJenaRepository.getWapObject(paramIri);
      assertNotNull(paramWapObjectDataset, "Could not get Dataset WapObject for: " + paramIri);
      // mock setup for WapObject
      WapObject paramWapObjectMock = Mockito.mock(WapObject.class);
      when(paramWapObjectMock.getDataset()).thenReturn(paramWapObjectDataset);
      when(paramWapObjectMock.getIriString()).thenReturn(paramIri);
      // test write WapObject
      objJenaRepository.writeObjectToDatabase(paramWapObjectMock);
      // clean up by aborting transaction
      objJenaRepository.abortTransaction();
   }

   /**
    * Test get range of object IRIs from sequence.
    */
   @Test
   final void testGetRangeOfObjectIrisFromSeq() {
      // already tested in testGetAllObjectIrisOfSeq
   }

   /**
    * Test get all object IRIs of sequence.
    */
   @Test
   final void testGetAllObjectIrisOfSeq() {
      String paramModelIri;
      String paramSeqIri;
      List<String> actual;
      // test get containers from root container
      paramModelIri = "http://localhost:8080/wap/";
      paramSeqIri = "http://localhost:8080/wap/#containers";
      actual = null;
      actual = objJenaRepository.getAllObjectIrisOfSeq(paramModelIri, paramSeqIri);
      assertNotNull(actual, "Could not get all object iris of seq.");
   }

   /**
    * Test get transaction dataset.
    */
   @Test
   final void testGetTransactionDataset() {
      org.apache.commons.rdf.api.Dataset actual;
      actual = null;
      actual = objJenaRepository.getTransactionDataset();
      assertNotNull(actual, "Could not get trasaction dataset.");
   }
}
