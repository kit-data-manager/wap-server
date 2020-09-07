package edu.kit.scc.dem.wapsrv.service;

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
public class BackupServiceMock {
   /**
    * Gets a Backup Service mock object
    * 
    * @return A backup service mock object
    */
   @Bean
   @Primary
   public BackupService backupService() {
      BackupService backupServiceMock = Mockito.mock(BackupService.class);
      return backupServiceMock;
   }
}
