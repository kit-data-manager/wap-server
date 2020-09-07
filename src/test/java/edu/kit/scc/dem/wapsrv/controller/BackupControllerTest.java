package edu.kit.scc.dem.wapsrv.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static edu.kit.scc.dem.wapsrv.controller.ControllerTestHelper.*;
import edu.kit.scc.dem.wapsrv.repository.BackupRepositoryMock;
import edu.kit.scc.dem.wapsrv.service.BackupService;
import edu.kit.scc.dem.wapsrv.service.BackupServiceMock;

/**
 * Tests the class BackupController
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {BackupController.class, BackupServiceMock.class, BackupRepositoryMock.class})
@ActiveProfiles("test")
class BackupControllerTest {
   /**
    * The filename returned by backupDatabase
    */
   public static final String BACKUP_STRING = "backup.dat";
   @Autowired
   private BackupController controller;
   @Autowired
   private BackupService backupServiceMock;

   /**
    * Test get backup.
    */
   @Test
   final void testGetBackup() {
      when(backupServiceMock.backupDatabase()).thenReturn(BACKUP_STRING);
      // no arguments needed
      ResponseEntity<?> response = controller.getBackup(null, null);
      assertEquals(BACKUP_STRING, response.getBody(), "Filename not as expected");
      assertEquals(HttpStatus.OK, response.getStatusCode(), "Unexcepected status code");
      checkAllowHeader(response, HttpMethod.GET, HttpMethod.OPTIONS);
   }

   /**
    * Test head backup.
    */
   @Test
   final void testHeadBackup() {
      when(backupServiceMock.backupDatabase()).thenReturn(BACKUP_STRING);
      // no arguments needed
      ResponseEntity<?> response = controller.headBackup(null, null);
      assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode(), "Unexcepected status code");
      checkAllowHeader(response, HttpMethod.GET, HttpMethod.OPTIONS);
   }

   /**
    * Test options backup.
    */
   @Test
   final void testOptionsBackup() {
      when(backupServiceMock.backupDatabase()).thenReturn(BACKUP_STRING);
      // no arguments needed
      ResponseEntity<?> response = controller.optionsBackup(null, null);
      assertEquals(HttpStatus.OK, response.getStatusCode(), "Unexcepected status code");
      checkAllowHeader(response, HttpMethod.GET, HttpMethod.OPTIONS);
   }
}
