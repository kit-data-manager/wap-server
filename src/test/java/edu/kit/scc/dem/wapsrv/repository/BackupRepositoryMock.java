package edu.kit.scc.dem.wapsrv.repository;

import static org.mockito.Mockito.when;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Configuration
@Profile("test")
public class BackupRepositoryMock {
   /**
    * The filename returned by backupDatabase
    */
   public static final String BACKUP_STRING = "backup.dat";

   /**
    * Gets a Backup Repository mock object
    * 
    * @return A backup Repository mock object
    */
   @Bean
   @Primary
   public BackupRepository backupRepository() {
      BackupRepository backupRepositoryMock = Mockito.mock(BackupRepository.class);
      when(backupRepositoryMock.backupDatabase()).thenReturn(BACKUP_STRING);
      return backupRepositoryMock;
   }
}
