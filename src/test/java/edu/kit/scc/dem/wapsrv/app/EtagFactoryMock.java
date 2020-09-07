package edu.kit.scc.dem.wapsrv.app;

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
public class EtagFactoryMock {
   /**
    * Providing a fake EtagFactory for testing. Separate class to work with spring @autowired.
    * 
    * @return EtagFactory mock
    */
   @Bean
   @Primary
   public EtagFactory etagFactory() {
      return Mockito.mock(EtagFactory.class);
   }
}
