package edu.kit.scc.dem.wapsrv.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.kit.scc.dem.wapsrv.repository.BackupRepository;

/**
 * The service used to create backups of the running database
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Service
public class BackupService {
   /**
    * The backup repository
    */
   @Autowired
   BackupRepository repository;

   /**
    * Creates a Backup of the running database and returns the generated backup-filename
    * 
    * @return The filename of the backup
    */
   public String backupDatabase() {
      String[] retPath = new String[1];
      repository.readRdfTransaction(ds -> {
         retPath[0] = repository.backupDatabase();
      });
      return retPath[0];
   }
}
