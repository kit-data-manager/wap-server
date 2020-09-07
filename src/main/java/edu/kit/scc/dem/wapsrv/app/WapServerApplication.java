package edu.kit.scc.dem.wapsrv.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import edu.kit.scc.dem.wapsrv.installer.WapServerInstaller;
import edu.kit.scc.dem.wapsrv.repository.WapObjectRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main class of the application. It starts the Spring Framework and sets basic
 * parameters like ComponentScan packages.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@SpringBootApplication
@ComponentScan(basePackages = {"edu.kit.scc.dem.wapsrv.service", "edu.kit.scc.dem.wapsrv.controller",
  "edu.kit.scc.dem.wapsrv.model.formats", "edu.kit.scc.dem.wapsrv.model.validators", "edu.kit.scc.dem.wapsrv.model.ext",
  "edu.kit.scc.dem.wapsrv.model.rdf", "edu.kit.scc.dem.wapsrv.exceptions", "edu.kit.scc.dem.wapsrv.repository.jena",
  "edu.kit.scc.dem.wapsrv.app"})
@EnableJpaRepositories(basePackages = {"edu.kit.scc.dem.wapsrv.dao"})
@EntityScan(basePackages = {"edu.kit.scc.dem.wapsrv.model.ext"})
// Otherwise the other beans will not be found in different packages
public class WapServerApplication implements ApplicationContextAware{

  /**
   * When used during testing, we have to wait for spring to launch the app.
   * This is the delay we accept as "normal" startup time
   */
  public static long startUpThreshold = 60000;
  /**
   * The single instance of the application for easy access outside spring
   */
  private static WapServerApplication instance = null;
  /**
   * Lock object for startup phase
   */
  private final Object startupLock = new Object();
  /**
   * Switch that tells the startup state
   */
  private boolean startupDone = false;
  /**
   * The Application context
   */
  private ApplicationContext applicationContext;

  /**
   * This constructor is called by spring and should not be executed manually
   */
  protected WapServerApplication(){
    instance = this;
  }

  /**
   * Waits until the application has finished startup
   *
   * @return Starting state
   */
  public static boolean blockUntilStarted(){
    long now = System.currentTimeMillis();
    while(instance == null){
      try{
        Thread.sleep(50);
      } catch(InterruptedException e){
      }
      if(System.currentTimeMillis() - now > startUpThreshold){
        return false;
      }
    }
    instance.blockUntilStartedInternal();
    return true;
  }

  /**
   * Starts the Spring Framework application.
   *
   * @param args The given command line arguments.
   */
  public static void main(String[] args){
    boolean manualInstall = false;
    // We only intercept the --install argument and ignore the rest
    if(args != null){
      for(String arg : args){
        if("--install".equals(arg)){
          manualInstall = true;
          break;
        }
        if("--create-config".equals(arg)){
          if(!WapServerConfig.isConfigFileExistent()){
            File file = WapServerConfig.createDefaultConfigurationFile();
            if(file != null){
              System.out.println("Default configuration file created, exitting : " + file.getName());
            } else{
              System.out.println("Could not create default config file");
            }
          } else{
            System.out.println("The configuration file already exists : " + WapServerConfig.propertiesFile + "\n"
                    + "Please remove if first and manually merge the settings afterwards.");
          }
          return; // exit the application
        }
      }
    }
    // If we are started in an empty directory besides our own jar file
    // execute the installer, or if manually activated
    if(manualInstall && !WapServerInstaller.performManualInstallation()){
      return;
    } else if(WapServerInstaller.isInstallationNecessary()
            && !new WapServerInstaller().install(new BufferedReader(new InputStreamReader(System.in)))){
      return;
    }
    // create the config if still not existing with default values
    createConfigIfNotExistent();
    // Update the config if old variable names are used
    WapServerConfig.updateConfigFromOldVersions();
    // Checks that everything is accessible and startup possible
    if(!WapServerConfig.checkConfig()){
      return;
    }
    // Register this file as default properties. No PropertySource annotation needed then
    // and we may change the path to e.G. /etc or any other location that Spring would not check
    // do this only if not already overridden through command line, real system properties or test code
    if(System.getProperty("spring.config.location") == null){
      System.setProperty("spring.config.location", WapServerConfig.getWapServerConfigFile().getAbsolutePath());
    }
    // apply http config prior to Spring startup
    WapServerConfig.applyHttpConfigBeforeSpringInit();
    SpringApplication.run(WapServerApplication.class, args);
  }

  @Bean
  public WapObjectRepository repository(){
    System.out.println("Obtaining RDF backend implementation.");
    String rdfBackendName = getRunningApplicationContext().getBean(WapServerConfig.class).getRdfBackendImplementation();
    System.out.println("Using RDF backend named : " + rdfBackendName);
    return (WapObjectRepository) getRunningApplicationContext().getBean(rdfBackendName);
  }

  /**
   * Creates the default config file if non exists already.
   *
   * @return The existent config file or the newly created default one
   */
  public static File createConfigIfNotExistent(){
    if(!WapServerConfig.isConfigFileExistent()){
      return WapServerConfig.createDefaultConfigurationFile();
    } else{
      return new File(WapServerConfig.propertiesFile);
    }
  }

  /**
   * Gets the application context if the app is running, null otherwise
   *
   * @return The application context
   */
  public static ApplicationContext getRunningApplicationContext(){
    if(instance == null){
      return null;
    }
    return instance.applicationContext;
  }

  /**
   * Deinitializes the Wap Server Application
   */
  @EventListener(ContextClosedEvent.class)
  public void deinit(){
    instance = null;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException{
    this.applicationContext = applicationContext;
  }

  /**
   * Is informed on every context refresh
   *
   * @param event The context refreshed event
   */
  @EventListener
  protected void onApplicationEvent(ContextRefreshedEvent event){
    System.out.println("Startup complete");
    Thread delayer = new Thread(){
      public void run(){
        // Some minor stuff is done after receive this event.
        // Until knowing a better way, just wait a few ms. This is only
        // relevant during testing and of no effect to the application.
        try{
          sleep(1000);
        } catch(InterruptedException e){
        }
        synchronized(startupLock){
          startupDone = true;
          startupLock.notifyAll();
        }
      }
    };
    delayer.start();
  }

  /**
   * Waits until the application has finished startup
   */
  private void blockUntilStartedInternal(){
    synchronized(startupLock){
      while(!startupDone){
        try{
          startupLock.wait();
        } catch(InterruptedException e){
        }
      }
    }
  }
}
