package edu.kit.scc.dem.wapsrv.testscommon;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import edu.kit.scc.dem.wapsrv.model.ModelFactory;

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
public class ModelFactoryMock {
   /**
    * Providing a fake ModelFactory for testing. Separate class to work with spring @autowired.
    * 
    * @return ModelFactory mock
    */
   @Bean
   @Primary
   public ModelFactory modelFactory() {
      return Mockito.mock(ModelFactory.class);
   }
}
