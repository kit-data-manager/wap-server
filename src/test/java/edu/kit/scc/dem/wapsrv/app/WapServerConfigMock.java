package edu.kit.scc.dem.wapsrv.app;

import static org.mockito.Mockito.when;
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
public class WapServerConfigMock {
   /**
    * Providing a fake WapServerConfig for testing. Separate class to work with spring @autowired.
    * 
    * @return WapServerConfig mock
    */
   @Bean
   @Primary
   public WapServerConfig wapServerConfig() {
      WapServerConfig wapServerConfigMock = Mockito.mock(WapServerConfig.class);
      when(wapServerConfigMock.getRootContainerIri()).thenReturn("http://www.example.org/wap/");
      when(wapServerConfigMock.getDataBasePath()).thenReturn("temp/");
      when(wapServerConfigMock.getJsonLdProfileFolder()).thenReturn("temp/");
      when(wapServerConfigMock.getSparqlReadIp()).thenReturn("localhost");
      when(wapServerConfigMock.getSparqlWriteIp()).thenReturn("localhost");
      when(wapServerConfigMock.getSparqlReadPort()).thenReturn(3330);
      when(wapServerConfigMock.getSparqlWritePort()).thenReturn(3331);
      when(wapServerConfigMock.getJsonLdProfileFile()).thenReturn("temp/");
      return wapServerConfigMock;
   }
}
