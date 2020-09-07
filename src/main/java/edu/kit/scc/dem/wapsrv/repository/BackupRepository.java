package edu.kit.scc.dem.wapsrv.repository;

/**
 * Repository used for creating backups of the database in the running application
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface BackupRepository extends TransactionRepository {
   /**
    * Creates a Backup of the running database and returns the generated backup-filename
    * 
    * @return The filename of the backup
    */
   String backupDatabase();
}
