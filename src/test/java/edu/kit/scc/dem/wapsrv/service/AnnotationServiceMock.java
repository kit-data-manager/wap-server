package edu.kit.scc.dem.wapsrv.service;

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
public class AnnotationServiceMock {
   /**
    * Gets a annotation Service mock object
    * 
    * @return A annotation service mock object
    */
   @Bean
   @Primary
   public AnnotationService annotationService() {
      AnnotationService annotationServiceMock = Mockito.mock(AnnotationService.class);
      return annotationServiceMock;
   }
}
