package edu.kit.scc.dem.wapsrv.repository;

import org.apache.commons.rdf.api.Dataset;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfTransactionExecuter;

/**
 * Interface to interact with the repository
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
public interface TransactionRepository{

  public enum Type{
    /**
     * Reading
     */
    Read,
    /**
     * Writing
     */
    Write
  }

  /**
   * Gets the transaction dataset.
   *
   * @return The transaction dataset
   */
  Dataset getTransactionDataset();

  /**
   * Perform a read only transaction on the Dataset of the database. The
   * transaction function is a lambda expression: (Dataset) -&gt; {function;}
   *
   * @param transaction the transaction function
   */
  default void readRdfTransaction(RdfTransactionExecuter transaction){
    doRdfTransaction(TransactionRepository.Type.Read, transaction);
  }

  /**
   * Perform a read/write transaction on the Dataset of the database. The
   * transaction function is a lambda expression: (Dataset) -&gt; {function;}
   *
   * @param transaction the transaction function
   */
  default void writeRdfTransaction(RdfTransactionExecuter transaction){
    doRdfTransaction(TransactionRepository.Type.Write, transaction);
  }

  /**
   * Do RDF transaction.
   *
   * @param type The type
   * @param transaction The transaction
   */
  default void doRdfTransaction(TransactionRepository.Type type, RdfTransactionExecuter transaction){
    //starting transaction synchronized in order to avoid duplicate transactions to be opened
    synchronized(this){
      boolean wasOpend = beginTransaction(type);
      try{
        transaction.execute(getTransactionDataset());
      } catch(WapException e){
        abortTransaction();
        throw e;
      }

      //end transaction only if not aborted before as abort should take care of closing the dataset
      endTransaction(wasOpend);
    }
  }

  /**
   * Begin transaction.
   *
   * @param type The type
   * @return True, if successful
   */
  default boolean beginTransaction(TransactionRepository.Type type){
    return false;
  }

  /**
   * Abort transaction and close dataset.
   */
  default void abortTransaction(){
  }

  /**
   * End transaction.
   *
   * @param wasOpend True, if opened
   */
  default void endTransaction(boolean wasOpend){
  }
}
