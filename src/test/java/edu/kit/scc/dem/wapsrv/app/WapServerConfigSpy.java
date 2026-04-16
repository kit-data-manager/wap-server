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
public class WapServerConfigSpy {
   /**
    * Providing a fake WapServerConfig for testing. Separate class to work with spring @autowired.
    * 
    * @return WapServerConfig spy
    */
   @Bean
   @Primary
   public WapServerConfig wapServerConfig() {
      WapServerConfig real = new WapServerConfig();
       return Mockito.spy(real);
   }
}
