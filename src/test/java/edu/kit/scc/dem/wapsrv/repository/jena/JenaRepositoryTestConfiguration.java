package edu.kit.scc.dem.wapsrv.repository.jena;

import static org.mockito.Mockito.when;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import edu.kit.scc.dem.wapsrv.app.EtagFactory;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.model.validators.ValidatorRegistry;
import edu.kit.scc.dem.wapsrv.service.ContainerServiceImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@Configuration
@SpringBootApplication(scanBasePackages = {"edu.kit.scc.dem.wapsrv.model.validators"}, scanBasePackageClasses = {
  ContainerServiceImpl.class, JenaRdfBackend.class, EtagFactory.class, JenaDataBase.class, ValidatorRegistry.class})
@EnableJpaRepositories(basePackages = {"edu.kit.scc.dem.wapsrv.dao"})
@EntityScan(basePackages = {"edu.kit.scc.dem.wapsrv.model.ext"})
@Profile("test")
public class JenaRepositoryTestConfiguration{

  /**
   * Providing a fake WapServerConfig for testing. Separate class to work with
   * spring @autowired.
   *
   * @return WapServerConfig mock
   */
  @Bean
  @Primary
  public WapServerConfig wapServerConfig(){
    WapServerConfig wapServerConfigMock = Mockito.mock(WapServerConfig.class);
    when(wapServerConfigMock.getRootContainerIri()).thenReturn("http://www.example.org/wap/");
    when(wapServerConfigMock.getDataBasePath()).thenReturn("temp/");
    when(wapServerConfigMock.getJsonLdProfileFolder()).thenReturn("temp/");
    return wapServerConfigMock;
  }
}
