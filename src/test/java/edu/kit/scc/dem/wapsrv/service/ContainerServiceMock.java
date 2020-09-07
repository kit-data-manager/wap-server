package edu.kit.scc.dem.wapsrv.service;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Tests with container mockup.
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
public class ContainerServiceMock {
   /**
    * Gets a container Service mock object
    * 
    * @return A container service mock object
    */
   @Bean
   @Primary
   public ContainerService containerService() {
      ContainerService containerServiceMock = Mockito.mock(ContainerService.class);
      return containerServiceMock;
   }
}
