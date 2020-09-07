package edu.kit.scc.dem.wapsrv.testscommon;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * FusekiTest
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Tag("old")
class FusekiTest {
   /**
    * Sets the up before class.
    *
    * @throws Exception
    *                   A general exception
    */
   @BeforeAll
   static void setUpBeforeClass() throws Exception {
   }

   /**
    * Tear down after class.
    *
    * @throws Exception
    *                   A general exception
    */
   @AfterAll
   static void tearDownAfterClass() throws Exception {
   }

   /**
    * Sets the up.
    *
    * @throws Exception
    *                   A general exception
    */
   @BeforeEach
   void setUp() throws Exception {
   }

   /**
    * Tear down.
    *
    * @throws Exception
    *                   A general exception
    */
   @AfterEach
   void tearDown() throws Exception {
   }

   /**
    * Test general.
    *
    * @throws InterruptedException
    *                              A interrupted exception
    */
   @Test
   void test() throws InterruptedException {
      // Test moved to external rest tests, where a db to connect to is already started
   }
}
