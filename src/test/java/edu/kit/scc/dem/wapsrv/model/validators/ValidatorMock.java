package edu.kit.scc.dem.wapsrv.model.validators;

import static org.mockito.Mockito.when;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

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
public class ValidatorMock {
   /**
    * Providing a fake Validator for testing. Separate class to work with spring @autowired.
    * 
    * @return Validator mock
    */
   @Bean
   @Primary
   public Validator validator() {
      Validator validatorMock = Mockito.mock(Validator.class);
      when(validatorMock.getFormat()).thenReturn(Format.JSON_LD);
      // DOTEST write the test for this method
      return validatorMock;
   }
}
