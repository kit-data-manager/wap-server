package edu.kit.scc.dem.wapsrv.app;

import static org.mockito.Mockito.when;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaDataBase;

/**
 * Tests the class FusekiRunner
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {FusekiRunner.class, JenaDataBase.class, WapServerConfigMock.class})
@ActiveProfiles("test")
class FusekiRunnerTest {
   @Autowired
   private WapServerConfig wapServerConfig;
   @Autowired
   private FusekiRunner objFusekiRunner;

   /**
    * Inits the test.
    */
   @BeforeEach
   void initTest() {
      // deinit in case of spring init
      objFusekiRunner.deinit();
   }

   /**
    * Deinit test.
    */
   @AfterEach
   void deinitTest() {
      objFusekiRunner.deinit();
   }

   /**
    * Test init.
    */
   @Test
   final void testInit() {
      WapServerConfig mockWapServerConfig = wapServerConfig;
      // test default
      objFusekiRunner.init();
      objFusekiRunner.deinit();
      // test config.getSparqlReadIp().equalsIgnoreCase("localhost")
      // || config.getSparqlReadIp().equalsIgnoreCase("loopback")
      // test false || true branch
      when(mockWapServerConfig.getSparqlReadIp()).thenReturn("loopback");
      when(mockWapServerConfig.getSparqlWriteIp()).thenReturn("loopback");
      objFusekiRunner.init();
      objFusekiRunner.deinit();
      // test false || false branch
      when(mockWapServerConfig.getSparqlReadIp()).thenReturn("invalid");
      when(mockWapServerConfig.getSparqlWriteIp()).thenReturn("invalid");
      objFusekiRunner.init();
      objFusekiRunner.deinit();
      // set back to default
      when(wapServerConfig.getSparqlReadIp()).thenReturn("localhost");
      when(wapServerConfig.getSparqlWriteIp()).thenReturn("localhost");
      // test readPort > 0 || writePort > 0
      // test true || false
      when(wapServerConfig.getSparqlReadPort()).thenReturn(3330);
      when(wapServerConfig.getSparqlWritePort()).thenReturn(-1);
      objFusekiRunner.init();
      objFusekiRunner.deinit();
      // test false || true
      when(wapServerConfig.getSparqlReadPort()).thenReturn(-1);
      when(wapServerConfig.getSparqlWritePort()).thenReturn(3331);
      objFusekiRunner.init();
      objFusekiRunner.deinit();
      // test false || false
      when(wapServerConfig.getSparqlReadPort()).thenReturn(-1);
      when(wapServerConfig.getSparqlWritePort()).thenReturn(-1);
      objFusekiRunner.init();
      objFusekiRunner.deinit();
      // set back to default
      when(wapServerConfig.getSparqlReadPort()).thenReturn(3330);
      when(wapServerConfig.getSparqlWritePort()).thenReturn(3331);
   }

   /**
    * Test deinit.
    */
   @Test
   final void testDeinit() {
      // Already tested in testInit.
   }
}
