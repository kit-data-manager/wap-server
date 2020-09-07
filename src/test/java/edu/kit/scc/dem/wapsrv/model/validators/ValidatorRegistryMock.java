package edu.kit.scc.dem.wapsrv.model.validators;

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
public class ValidatorRegistryMock {
   /**
    * Providing a fake ValidatorRegistry for testing. Separate class to work with spring @autowired.
    * 
    * @return ValidatorRegistry mock
    */
   @Bean
   @Primary
   public ValidatorRegistry validatorRegistry() {
      return Mockito.mock(ValidatorRegistry.class);
   }
}
