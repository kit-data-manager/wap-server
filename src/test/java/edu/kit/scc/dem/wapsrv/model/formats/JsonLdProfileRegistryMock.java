package edu.kit.scc.dem.wapsrv.model.formats;

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
public class JsonLdProfileRegistryMock {
   /**
    * Providing a fake JsonLdProfileRegistry for testing. Separate class to work with spring @autowired.
    * 
    * @return JsonLdProfileRegistry mock
    */
   @Bean
   @Primary
   public JsonLdProfileRegistry jsonLdProfileRegistry() {
      return Mockito.mock(JsonLdProfileRegistry.class);
   }
}
