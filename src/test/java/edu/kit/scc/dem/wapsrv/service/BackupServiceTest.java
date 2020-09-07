package edu.kit.scc.dem.wapsrv.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfTransactionExecuter;
import edu.kit.scc.dem.wapsrv.repository.BackupRepository;
import edu.kit.scc.dem.wapsrv.repository.BackupRepositoryMock;
import static edu.kit.scc.dem.wapsrv.repository.BackupRepositoryMock.BACKUP_STRING;
import edu.kit.scc.dem.wapsrv.repository.TransactionRepository;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

/**
 * Tests the class BackupService
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {BackupService.class, BackupRepositoryMock.class})
@ActiveProfiles("test")
class BackupServiceTest{

  @Autowired
  private BackupRepository objBackupRepository;
  @Autowired
  private BackupService objBackupService;

  /**
   * Test to get a data backup of DB.
   */
  @Test
  final void testBackupDatabase(){
    String actual;
    //check dependency injection
    assertNotNull(objBackupService, "Construction did fail.");
    // mock setup for repository
    doCallRealMethod().when(objBackupRepository).readRdfTransaction(any(RdfTransactionExecuter.class));
    doCallRealMethod().when(objBackupRepository).writeRdfTransaction(any(RdfTransactionExecuter.class));
    doCallRealMethod().when(objBackupRepository).doRdfTransaction(any(TransactionRepository.Type.class), any(RdfTransactionExecuter.class));
    objBackupService.repository = objBackupRepository;
    // test default
    actual = "not null";
    actual = objBackupRepository.backupDatabase();
    assertNotNull(actual, "Could not get backup database String.");
    assertEquals(BackupRepositoryMock.BACKUP_STRING, actual);
  }
}
