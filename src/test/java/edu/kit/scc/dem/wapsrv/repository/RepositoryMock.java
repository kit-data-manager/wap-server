package edu.kit.scc.dem.wapsrv.repository;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfTransactionExecuter;

/**
 * Tests with repository mockup.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Configuration
@Profile("test")
public class RepositoryMock {
   /**
    * Providing a fake WapObjectRepository for testing. Separate class to work with spring @autowired.
    * 
    * @return WapObjectRepository mock
    */
   @Bean
   @Primary
   public WapObjectRepository wapObjectRepository() {
      WapObjectRepository wapObjectRepository = Mockito.mock(CollectedRepository.class);
      doCallRealMethod().when(wapObjectRepository).readRdfTransaction(any(RdfTransactionExecuter.class));
      doCallRealMethod().when(wapObjectRepository).writeRdfTransaction(any(RdfTransactionExecuter.class));
      doCallRealMethod().when(wapObjectRepository).doRdfTransaction(any(TransactionRepository.Type.class),
            any(RdfTransactionExecuter.class));
      return wapObjectRepository;
   }
}
