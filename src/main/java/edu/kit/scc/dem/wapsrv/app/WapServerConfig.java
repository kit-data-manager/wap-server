package edu.kit.scc.dem.wapsrv.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import edu.kit.scc.dem.wapsrv.controller.WapPathMatcher;
import java.util.Arrays;

/**
 * Contains the configuration and the global constants that affect the entire
 * application.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@Configuration
public class WapServerConfig extends WebMvcConfigurationSupport{

  /**
   * The endpoint to use for all requests relating to the Web Annotation
   * Protocol. This version is without the slash "/" at the end
   * <p>
   * If no prefix shall be used, which means the root container is the base URL,
   * use a slash "/". Also the endpoint cannot be empty and cannot have white
   * spaces within or before/after.
   */
  public static final String WAP_ENDPOINT_WITHOUT_TRAILING_SLASH = "/wap";
  // no "/" at the end
  /**
   * The endpoint to use for all requests relating to the Web Annotation
   * Protocol. It has to end with a slash "/".
   * <p>
   * If no prefix shall be used, which means the root container is the base URL,
   * use a slash "/". Also the endpoint cannot be empty and cannot have white
   * spaces within or before/after.
   */
  public static final String WAP_ENDPOINT = WAP_ENDPOINT_WITHOUT_TRAILING_SLASH + "/";
  /**
   * The SSLConfig file to use
   */
  public static String sslConfigFile = "./ssl/ssl.conf";
  /**
   * The relative path to the configuration file needed for conventional file
   * interactions (this value may be overridden in tests)
   */
  public static String propertiesFile = "application.properties";
  // Default values to use if non existent
  private static final String HOSTNAME_DEFAULT = "localhost";
  private static final int WAP_PORT_DEFAULT = 8080;
  private static final int SPARQL_READ_PORT_DEFAULT = 3330;
  private static final int SPARQL_WRITE_PORT_DEFAULT = 3331;
  private static final boolean ENABLE_HTTPS_DEFAULT = false;
  private static final String WAP_IP_DEFAULT = "*";
  private static final String SPARQL_READ_IP_DEFAULT = "*";
  private static final String SPARQL_WRITE_IP_DEFAULT = "localhost";
  private static final String JSONLD_VALIDATOR_SCHEMAFOLDER_DEFAULT = "./schemas";
  private static final boolean ENABLE_VALIDATION_DEFAULT = true;
  private static final boolean ENABLE_MANDATORY_LABEL_IN_CONTAINERS_DEFAULT = false;
  private static final boolean ENABLE_MANDATORY_SLUG_IN_CONTAINER_POSTS_DEFAULT = false;
  private static final boolean ENABLE_CONTENT_NEGOTIATION_DEFAULT = true;
  private static final String JSONLD_PROFILE_FOLDER_DEFAULT = "./profiles";
  // for one day profiles are kept in cache before they get an update
  private static final long JSONLD_CACHED_PROFILE_VALIDITY_IN_MS_DEFAULT = 24 * 60 * 60 * 1000;
  private static final String DATABASE_PATH_DEFAULT = "./production_db";
  private static final String WEBCLIENT_FOLDER_DEFAULT = "./webcontent";
  private static final String JAVADOC_FOLDER_DEFAULT = "./doc";
  private static final String JSON_LD_FRAME_FOLDER_DEFAULT = JSONLD_PROFILE_FOLDER_DEFAULT;
  private static final int PAGE_SIZE_DEFAULT = 20;
  private static final boolean SHOULD_APPEND_STACKTRACE_TO_ERROR_MESSAGES_DEFAULT = false;
  private static final boolean MULTIPLE_ANNOTATION_POST_DEFAULT = true;
  private static final String SIMPLE_FORMATTERS_DEFAULT
          = "NTRIPLES*application/n-triples" + "|RDF_JSON*application/rdf+json";
  private static final String CORS_ALLOWED_ORIGINS_PATH_DEFAULT = "./cors_allowed_origins.conf";
  private static final boolean FALLBACK_VALIDATION_DEFAULT = true;
  private static final String RDF_BACKEND_IMPLEMENTATION_DEFAULT = "jena";

  /**
   * The single instance of the configuration
   */
  private static WapServerConfig instance;
  private final int stackTraceElements = 20;
  /**
   * The logger to use
   */
  private final Logger logger = LoggerFactory.getLogger(WapServerConfig.class);
  /**
   * Enables or disables https
   */
  @Value("${EnableHttps:" + ENABLE_HTTPS_DEFAULT + "}")
  private boolean enableHttps;
  /**
   * The hostname to use
   */
  @Value("${Hostname:" + HOSTNAME_DEFAULT + "}")
  private String hostname;
  /**
   * The port for the WAP REST interface.
   */
  @Value("${WapPort:" + WAP_PORT_DEFAULT + "}")
  private int wapPort;
  /**
   * The IP for the WAP REST interface
   * <ul>
   * <li>Not specified ==> Listen an all available ips
   * <li>localhost or any specific ip like 192.168.2.11 to listen only on them
   * </ul>
   */
  @Value("${WapIp:" + WAP_IP_DEFAULT + "}")
  private String wapIp;
  /**
   * The port for the SPARQL READ interface
   */
  @Value("${SparqlReadPort:" + SPARQL_READ_PORT_DEFAULT + "}")
  private int sparqlReadPort;
  /**
   * The IP for the SPARQL READ interface All existent IPs == * Either localhost
   * or loopback as words, everything else == all ips
   */
  @Value("${SparqlReadIp:" + SPARQL_READ_IP_DEFAULT + "}")
  private String sparqlReadIp = SPARQL_READ_IP_DEFAULT;
  /**
   * The port for the SPARQL WRITE interface
   */
  @Value("${SparqlWritePort:" + SPARQL_WRITE_PORT_DEFAULT + "}")
  private int sparqlWritePort;
  /**
   * The IP for the SPARQL WRITE interface Either localhost or loopback as
   * words, everything else == all ips
   */
  @Value("${SparqlWriteIp:" + SPARQL_WRITE_IP_DEFAULT + "}")
  private String sparqlWriteIp;
  /**
   * Path to the JSON-LD schema folder
   */
  @Value("${JsonLdValidator_SchemaFolder:" + JSONLD_VALIDATOR_SCHEMAFOLDER_DEFAULT + "}")
  private String jsonLdValidatorSchemaFolder;
  /**
   * Enables or disables schema validation for PUT / POST requests.
   */
  @Value("${EnableValidation:" + ENABLE_VALIDATION_DEFAULT + "}")
  private boolean enableValidation;
  /**
   * Enables or disables required label property when posting containers. If
   * disabled and missing, the name is copied there. If enabled and missing, the
   * server declines the post request.
   */
  @Value("${EnableMandatoryLabelInContainers:" + ENABLE_MANDATORY_LABEL_IN_CONTAINERS_DEFAULT + "}")
  private boolean enableMandatoryLabelsInContainers;
  /**
   * Enables or disables required label property when posting containers. If
   * disabled and missing, the name is copied there. If enabled and missing, the
   * server declines the post request.
   */
  @Value("${EnableMandatorySlugInContainerPost:" + ENABLE_MANDATORY_SLUG_IN_CONTAINER_POSTS_DEFAULT + "}")
  private boolean enableMandatorySlugInContainerPost;
  /**
   * Enables or disables content negotiation for all read (GET, HEAD and
   * OPTIONS) requests
   */
  @Value("${EnableContentNegotiation:" + ENABLE_CONTENT_NEGOTIATION_DEFAULT + "}")
  private boolean enableContentNegotiation;
  /**
   * The folder where the JavaDoc files reside
   */
  @Value("${JavaDocFolder:" + JAVADOC_FOLDER_DEFAULT + "}")
  private String javaDocFolder;
  /**
   * The folder where the web client files reside
   */
  @Value("${WebClientFolder:" + WEBCLIENT_FOLDER_DEFAULT + "}")
  private String webClientFolder;
  // /**
  // * The folder where database backups will be created
  // */
  // @Value("${DataBaseBackupPath:" + DATABASE_BACKUPPATH_DEFAULT + "}")
  // private String dataBaseBackupPath;
  /**
   * The folder where the database files reside
   */
  @Value("${DataBasePath:" + DATABASE_PATH_DEFAULT + "}")
  private String dataBasePath;
  /**
   * The default validity in ms before cached profiles will be renewed
   */
  @Value("${JsonLdCachedProfileValidityInMs:" + JSONLD_CACHED_PROFILE_VALIDITY_IN_MS_DEFAULT + "}")
  private long jsonLdCachedProfileValidityInMs;
  /**
   * The folder where the cached JSON-LD profiles reside
   */
  @Value("${JsonLdProfileFolder:" + JSONLD_PROFILE_FOLDER_DEFAULT + "}")
  private String jsonLdProfileFolder;
  /**
   * The folder where the cached JSON-LD frames reside
   */
  @Value("${JsonLdFrameFolder:" + JSON_LD_FRAME_FOLDER_DEFAULT + "}")
  private String jsonLdFrameFolder;
  /**
   * The page size used
   */
  @Value("${PageSize:" + PAGE_SIZE_DEFAULT + "}")
  private int pageSize;
  /**
   * The page size used
   */
  @Value("${ShouldAppendStackTraceToErrorMessages:" + SHOULD_APPEND_STACKTRACE_TO_ERROR_MESSAGES_DEFAULT + "}")
  private boolean shouldAppendStackTraceToErrorMessages;
  /**
   * Are mulit anno posts allowed
   */
  @Value("${MultipleAnnotationPost:" + MULTIPLE_ANNOTATION_POST_DEFAULT + "}")
  private boolean multipleAnnotationPost;
  /**
   * The additional simple formats to register
   */
  @Value("${SimpleFormatters:" + SIMPLE_FORMATTERS_DEFAULT + "}")
  private String simpleFormatters;
  /**
   * The path where cors allowed origins are stored
   */
  @Value("${CorsAllowedOriginsPath:" + CORS_ALLOWED_ORIGINS_PATH_DEFAULT + "}")
  private String corsAllowedOriginsPath;
  /**
   * The fallback validation setting
   */
  @Value("${FallbackValidation:" + FALLBACK_VALIDATION_DEFAULT + "}")
  private boolean fallbackValidation;

  @Value("${RdfBackendImplementation:" + RDF_BACKEND_IMPLEMENTATION_DEFAULT + "}")
  private String rdfBackendImplementation;

  /**
   * The cors configuration to use
   */
  private CorsConfiguration corsConfig;

  /**
   * Creates the WapServerConfig singleton
   */
  protected WapServerConfig(){
    super();
    logger.info("WapServerConfig created");
    // Set instance only during spring usage, not unit testing
    if(isUnitTest()){
      if(isSpringBeanGeneration()){
        // we might be run by junit, but using spring during it
        instance = this;
      }
    } else{
      instance = this;
    }
  }

  /**
   * Gets the File where the config is loaded from by Spring
   *
   * @return The config file for Spring
   */
  public static File getWapServerConfigFile(){
    return new File(propertiesFile);
  }

  /**
   * Sets the File where the config is loaded from by Spring
   *
   * @param configFile The file for Spring to load the config from
   */
  public static void setWapServerConfigFile(File configFile){
    propertiesFile = configFile.getAbsolutePath();
  }

  /**
   * Checks whether the configuration file exists
   *
   * @return If the configuration file exists
   */
  public static boolean isConfigFileExistent(){
    return new File(propertiesFile).exists();
  }

  /**
   * Creates the default configuration file
   *
   * @return The default configuration file, null on any error
   */
  public static File createDefaultConfigurationFile(){
    // This is a hack to help tests to not manipulate live config. it is of no effect in the real application.
    if(isUnitTest()){
      if(new File(propertiesFile).equals(new File("application.properties"))){
        System.err.println("Manipulating production config during Testing, exit at once");
        Thread.dumpStack();
        System.exit(1); // Not only fail, hard exit. This is a no go. Better recognize at once
        // as tests may eventually destroy the live config or db.
      }
    }
    try{
      Properties props = getDefaultProperties();
      FileOutputStream out = new FileOutputStream(propertiesFile);
      props.store(out, "Default Wap Server Configuration");
      out.flush();
      out.close();
      LoggerFactory.getLogger(WapServerConfig.class).info("Default configuration file created");
      return new File(propertiesFile);
    } catch(IOException ex){
      return null;
    }
  }

  /**
   * Gets the default configuration as {@link Properties}
   *
   * @return The default configuration
   */
  public static Properties getDefaultProperties(){
    // Attention: Keep this method in sync with die @value Annotations and the
    // ConfigurationLeys enum
    Properties props = new Properties();
    props.put(ConfigurationKeys.EnableHttps.toString(), ENABLE_HTTPS_DEFAULT + "");
    props.put(ConfigurationKeys.Hostname.toString(), HOSTNAME_DEFAULT);
    props.put(ConfigurationKeys.WapPort.toString(), WAP_PORT_DEFAULT + "");
    props.put(ConfigurationKeys.WapIp.toString(), WAP_IP_DEFAULT);
    props.put(ConfigurationKeys.SparqlReadPort.toString(), SPARQL_READ_PORT_DEFAULT + "");
    props.put(ConfigurationKeys.SparqlReadIp.toString(), SPARQL_READ_IP_DEFAULT);
    props.put(ConfigurationKeys.SparqlWritePort.toString(), SPARQL_WRITE_PORT_DEFAULT + "");
    props.put(ConfigurationKeys.SparqlWriteIp.toString(), SPARQL_WRITE_IP_DEFAULT);
    props.put(ConfigurationKeys.JsonLdValidator_SchemaFolder.toString(), JSONLD_VALIDATOR_SCHEMAFOLDER_DEFAULT);
    props.put(ConfigurationKeys.EnableValidation.toString(), ENABLE_VALIDATION_DEFAULT + "");
    props.put(ConfigurationKeys.EnableMandatoryLabelInContainers.toString(),
            ENABLE_MANDATORY_LABEL_IN_CONTAINERS_DEFAULT + "");
    props.put(ConfigurationKeys.EnableMandatorySlugInContainerPost.toString(),
            ENABLE_MANDATORY_SLUG_IN_CONTAINER_POSTS_DEFAULT + "");
    props.put(ConfigurationKeys.EnableContentNegotiation.toString(), ENABLE_CONTENT_NEGOTIATION_DEFAULT + "");
    props.put(ConfigurationKeys.JsonLdProfileFolder.toString(), JSONLD_PROFILE_FOLDER_DEFAULT);
    props.put(ConfigurationKeys.JsonLdCachedProfileValidityInMs.toString(),
            JSONLD_CACHED_PROFILE_VALIDITY_IN_MS_DEFAULT + "");
    props.put(ConfigurationKeys.DataBasePath.toString(), DATABASE_PATH_DEFAULT);
    // props.put(ConfigurationKeys.DataBaseBackupPath.toString(),
    // DATABASE_BACKUPPATH_DEFAULT);
    props.put(ConfigurationKeys.WebClientFolder.toString(), WEBCLIENT_FOLDER_DEFAULT);
    props.put(ConfigurationKeys.JavaDocFolder.toString(), JAVADOC_FOLDER_DEFAULT);
    props.put(ConfigurationKeys.JsonLdFrameFolder.toString(), JSON_LD_FRAME_FOLDER_DEFAULT);
    props.put(ConfigurationKeys.PageSize.toString(), PAGE_SIZE_DEFAULT + "");
    props.put(ConfigurationKeys.ShouldAppendStackTraceToErrorMessages.toString(),
            SHOULD_APPEND_STACKTRACE_TO_ERROR_MESSAGES_DEFAULT + "");
    props.put(ConfigurationKeys.MultipleAnnotationPost.toString(), MULTIPLE_ANNOTATION_POST_DEFAULT + "");
    props.put(ConfigurationKeys.SimpleFormatters.toString(), SIMPLE_FORMATTERS_DEFAULT);
    props.put(ConfigurationKeys.CorsAllowedOriginsPath.toString(), CORS_ALLOWED_ORIGINS_PATH_DEFAULT);
    props.put(ConfigurationKeys.FallbackValidation.toString(), FALLBACK_VALIDATION_DEFAULT + "");
    if(ConfigurationKeys.values().length != props.size()){
      throw new RuntimeException("Default properties and the ConfigurationKeys enum not in sync");
    }
    return props;
  }

  /**
   * Updates the config file if it has been created with an older version of the
   * application
   *
   * @return true if changed, false if everything up to date
   */
  public static boolean updateConfigFromOldVersions(){
    // This is a hack to help tests to not manipulate live config. it is of no effect in the real application.
    if(isUnitTest()){
      if(new File(propertiesFile).equals(new File("application.properties"))){
        System.err.println("Manipulating production config during Testing, exit at once");
        Thread.dumpStack();
        System.exit(1); // Not only fail, hard exit. This is a no go. Better recognize at once
        // as tests may eventually destroy the live config or db.
      }
    }
    try{
      // Logger logger = LoggerFactory.getLogger(WapServerConfig.class);
      File configFile = new File(propertiesFile);
      FileInputStream inStream = new FileInputStream(configFile);
      Properties props = new Properties();
      props.load(inStream);
      inStream.close();
      boolean changed = false;
      // Add new properties that might not exists
      changed = addNewProperties(props);
      if(props.containsKey("server.address")){
        changed = true;
        if(props.containsKey(ConfigurationKeys.WapIp.toString())){
          // remove the old key
          props.remove("server.address");
        } else{
          // put in again under the new key
          props.put(ConfigurationKeys.WapIp.toString(), props.remove("server.address"));
        }
      }
      if(props.containsKey("server.port")){
        changed = true;
        if(props.containsKey(ConfigurationKeys.WapPort.toString())){
          // remove the old key
          props.remove("server.port");
        } else{
          // put in again under the new key
          props.put(ConfigurationKeys.WapPort.toString(), props.remove("server.port"));
        }
      }
      // After moving of folders to final locations, modify eventually existing values
      if(props.containsKey("JsonLdValidator_SchemaFile")){
        props.put(ConfigurationKeys.JsonLdValidator_SchemaFolder.toString(), JSONLD_VALIDATOR_SCHEMAFOLDER_DEFAULT);
        props.remove("JsonLdValidator_SchemaFile");
        changed = true;
      }
      if(props.containsKey("EnableAnnotationValidation")){
        props.put(ConfigurationKeys.EnableValidation.toString(), props.remove("EnableAnnotationValidation"));
        changed = true;
      }
      if(props.getProperty(ConfigurationKeys.WebClientFolder.toString()).startsWith("WebContent")){
        changed = true;
        props.put(ConfigurationKeys.WebClientFolder.toString(), "webcontent");
      }
      if(!props.containsKey(ConfigurationKeys.SimpleFormatters.toString())){
        props.put(ConfigurationKeys.SimpleFormatters.toString(), SIMPLE_FORMATTERS_DEFAULT);
        changed = true;
      }
      if(changed){
        FileOutputStream out = new FileOutputStream(propertiesFile);
        props.store(out, "Updated Wap Server Configuration");
        out.flush();
        out.close();
        LoggerFactory.getLogger(WapServerConfig.class)
                .info("The configuration file has been updated from an older version");
      }
      return changed;
    } catch(IOException e){
      throw new RuntimeException("Error updating config : " + e.getMessage());
    }
  }

  private static boolean addNewProperties(Properties props){
    Properties defaults = getDefaultProperties();
    boolean added = false;
    for(Object key : defaults.keySet()){
      if(!props.containsKey(key)){
        props.put(key, defaults.get(key));
        added = true;
      }
    }
    return added;
  }

  private static boolean isSpringBeanGeneration(){
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for(StackTraceElement element : stackTrace){
      if(element.getClassName().startsWith("org.springframework.beans.factory.")){
        return true;
      }
    }
    return false;
  }

  private static boolean isUnitTest(){
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for(StackTraceElement element : stackTrace){
      if(element.getClassName().startsWith("org.junit.")){
        return true;
      }
    }
    return false;
  }

  /**
   * This method has to be called before the spring framework is started. The
   * configuration related to the embedded HTTP Server is set to the respective
   * system properties.
   */
  public static void applyHttpConfigBeforeSpringInit(){
    try{
      File configFile = new File(propertiesFile);
      FileInputStream inStream = new FileInputStream(configFile);
      Properties props = new Properties();
      props.load(inStream);
      inStream.close();
      String ipString = props.getProperty(ConfigurationKeys.WapIp.toString());
      if("localhost".equalsIgnoreCase(ipString) || "loopback".equalsIgnoreCase(ipString)){
        System.setProperty("server.address", "localhost");
      } else if("*".equals(ipString) || ipString == null){
        System.getProperties().remove("server.address");
      } else{
        System.setProperty("server.address", ipString);
      }
      String portString = props.getProperty(ConfigurationKeys.WapPort.toString());
      if(portString == null){
        System.getProperties().remove("server.port");
      } else{
        System.setProperty("server.port", portString);
      }
      // Sometime (on CENTOS testmachine) the hostname was overridden by java.
      // therefore we take care of that fact even if not necessary on a given machine
      // Another strange thing: Hostname needs to be updated lower and upper case to 100% work
      System.setProperty("hostname", props.getProperty(ConfigurationKeys.Hostname.toString()));
      System.setProperty("Hostname", props.getProperty(ConfigurationKeys.Hostname.toString()));
      boolean withHttps = Boolean
              .parseBoolean(props.getProperty(ConfigurationKeys.EnableHttps.toString(), ENABLE_HTTPS_DEFAULT + ""));
      if(withHttps){
        applyHttpsConfig();
      }
    } catch(IOException e){
      throw new RuntimeException("Error applying HTTP config : " + e.getMessage());
    }
  }

  /**
   * Apply the https config to jetty
   *
   * @throws IOException On every I/O error
   */
  private static void applyHttpsConfig() throws IOException{
    // SSL is configured via values under ssl/
    // their existence has already been verified
    File configFile = new File(sslConfigFile);
    FileInputStream inStream = new FileInputStream(configFile);
    Properties sslProps = new Properties();
    sslProps.load(inStream);
    inStream.close();
    System.setProperty("server.ssl.enabled", "true");
    System.setProperty("server.ssl.alias", sslProps.getProperty("server.ssl.alias"));
    System.setProperty("server.ssl.key-store", "./ssl/" + sslProps.getProperty("key-store-file"));
    System.setProperty("server.ssl.key-store-password", sslProps.getProperty("key-store-password"));
    System.setProperty("server.ssl.key-password", sslProps.getProperty("key-password"));
    LoggerFactory.getLogger(WapServerConfig.class).info("SSL configuration applied");
  }

  /**
   * Checks the actual configuration file. Are all paths accessible, is nothing
   * set that contradicts each other and similar tests.
   *
   * @return true if configuration is ok
   */
  public static boolean checkConfig(){
    Logger logger = LoggerFactory.getLogger(WapServerConfig.class);
    try{
      File configFile = new File(propertiesFile);
      FileInputStream inStream = new FileInputStream(configFile);
      Properties props = new Properties();
      props.load(inStream);
      inStream.close();
      // check all relevant folders / data. A missing database is not a problem, as it will get autocreated
      if(!checkFolder(ConfigurationKeys.WebClientFolder, props)){
        return false;
      }
      if(!checkFolder(ConfigurationKeys.JsonLdProfileFolder, props)){
        return false;
      }
      if(!checkFolder(ConfigurationKeys.JavaDocFolder, props)){
        return false;
      }
      if(!checkFolder(ConfigurationKeys.JsonLdFrameFolder, props)){
        return false;
      }
      if(!checkFolder(ConfigurationKeys.JsonLdValidator_SchemaFolder, props)){
        return false;
      }
      int wapPort = Integer.parseInt(props.getProperty(ConfigurationKeys.WapPort.toString()));
      if(wapPort < 80 || wapPort > 65535){
        logger.error("Wap Port invalid (>=80 && <=65535) : " + wapPort);
        return false;
      }
      long profileCacheValidity
              = Long.parseLong(props.getProperty(ConfigurationKeys.JsonLdCachedProfileValidityInMs.toString()));
      if(profileCacheValidity < 60000){
        logger.error("Minimum cache validy is 1 minute (60000 ms) : " + profileCacheValidity);
        return false;
      }
      if(profileCacheValidity > 24 * 60 * 60 * 1000){
        logger.warn("Minimum cache validy is longer than one day, is this intended ? " + profileCacheValidity);
      }
      // check that no port is used twice
      Set<Integer> portsUsed = new HashSet<Integer>();
      if(!checkPort(ConfigurationKeys.WapPort, portsUsed, props)){
        return false;
      }
      if(!checkPort(ConfigurationKeys.SparqlReadPort, portsUsed, props)){
        return false;
      }
      if(!checkPort(ConfigurationKeys.SparqlWritePort, portsUsed, props)){
        return false;
      }
      // We may have invalid ips, which will result in spring not starting. This is already logged by spring
      // therefore we do not test it here
      // the other variables are not check for consistency so far as they are orthogonal and do not interfere
      return true;
    } catch(NumberFormatException | IOException ex){
      logger.error(ex.getMessage());
      return false;
    }
  }

  private static boolean checkPort(ConfigurationKeys key, Set<Integer> portsUsed, Properties props){
    String value = props.getProperty(key.toString());
    Logger logger = LoggerFactory.getLogger(WapServerConfig.class);
    if(value == null){
      logger.error("No config parameter for " + key.toString() + " exists");
      return false;
    }
    int port = Integer.parseInt(value);
    if(port == -1){
      return true; // Disabled sparql is ok
    }
    if(portsUsed.contains(port)){
      return false; // Port is already used
    }
    if(port < 80 || port > 65535){
      logger.error(key.toString() + " port invalid (>=80 && <=65535) : " + port);
      return false;
    }
    portsUsed.add(port);
    return true;
  }

  private static boolean checkFolder(ConfigurationKeys key, Properties props){
    String value = props.getProperty(key.toString());
    Logger logger = LoggerFactory.getLogger(WapServerConfig.class);
    if(value == null){
      logger.error("No config parameter for " + key.toString() + " exists");
      return false;
    }
    boolean exists = new File(value).exists();
    if(!exists){
      logger.error("Folder " + key.toString() + "=" + value + " does not exist");
    }
    return exists;
  }

  /**
   * Returns the single instance of the application configuration. If used in
   * tests or situations where Spring autowiring is not in use the configuration
   * has to be manually updated afterwards.
   *
   * @return The application configuration
   */
  public static WapServerConfig getInstance(){
    // No instantiation outside spring anymore
    return instance;
  }

  /**
   * Checks whether an instance already exists
   *
   * @return true if instance exists, false otherwise
   */
  public static boolean isInstantiated(){
    return instance != null;
  }

  /**
   * Checks whether the configuration has been initialized (via Spring
   * autowiring or manually). If not, it has to be updated with
   * {@link #updateConfig(Properties)} prior to usage.
   *
   * @return true if initialized and usable
   */
  public boolean isConfigInitialized(){
    return hostname != null;
  }

  /**
   * Updates the values in the configuration with the ones provided in the given
   * configuration. (Attention: Keep this method in sync with die @value
   * Annotations and the ConfigurationLeys enum)
   *
   * @param props The properties to extract the updated configuration from
   */
  public void updateConfig(Properties props){
    enableHttps = getProperty(props, ConfigurationKeys.EnableHttps, ENABLE_HTTPS_DEFAULT);
    hostname = getProperty(props, ConfigurationKeys.Hostname, HOSTNAME_DEFAULT);
    wapIp = getProperty(props, ConfigurationKeys.WapIp, WAP_IP_DEFAULT);
    wapPort = getProperty(props, ConfigurationKeys.WapPort, WAP_PORT_DEFAULT);
    sparqlReadIp = getProperty(props, ConfigurationKeys.SparqlReadIp, SPARQL_READ_IP_DEFAULT);
    sparqlReadPort = getProperty(props, ConfigurationKeys.SparqlReadPort, SPARQL_READ_PORT_DEFAULT);
    sparqlWriteIp = getProperty(props, ConfigurationKeys.SparqlWriteIp, SPARQL_WRITE_IP_DEFAULT);
    sparqlWritePort = getProperty(props, ConfigurationKeys.SparqlWritePort, SPARQL_WRITE_PORT_DEFAULT);
    jsonLdValidatorSchemaFolder
            = getProperty(props, ConfigurationKeys.JsonLdValidator_SchemaFolder, JSONLD_VALIDATOR_SCHEMAFOLDER_DEFAULT);
    enableValidation = getProperty(props, ConfigurationKeys.EnableValidation, ENABLE_VALIDATION_DEFAULT);
    enableMandatoryLabelsInContainers = getProperty(props, ConfigurationKeys.EnableMandatoryLabelInContainers,
            ENABLE_MANDATORY_LABEL_IN_CONTAINERS_DEFAULT);
    enableMandatorySlugInContainerPost = getProperty(props, ConfigurationKeys.EnableMandatorySlugInContainerPost,
            ENABLE_MANDATORY_SLUG_IN_CONTAINER_POSTS_DEFAULT);
    enableContentNegotiation
            = getProperty(props, ConfigurationKeys.EnableContentNegotiation, ENABLE_CONTENT_NEGOTIATION_DEFAULT);
    jsonLdProfileFolder = getProperty(props, ConfigurationKeys.JsonLdProfileFolder, JSONLD_PROFILE_FOLDER_DEFAULT);
    jsonLdCachedProfileValidityInMs = getProperty(props, ConfigurationKeys.JsonLdCachedProfileValidityInMs,
            JSONLD_CACHED_PROFILE_VALIDITY_IN_MS_DEFAULT);
    dataBasePath = getProperty(props, ConfigurationKeys.DataBasePath, DATABASE_PATH_DEFAULT);
    webClientFolder = getProperty(props, ConfigurationKeys.WebClientFolder, WEBCLIENT_FOLDER_DEFAULT);
    javaDocFolder = getProperty(props, ConfigurationKeys.JavaDocFolder, JAVADOC_FOLDER_DEFAULT);
    jsonLdFrameFolder = getProperty(props, ConfigurationKeys.JsonLdFrameFolder, JSON_LD_FRAME_FOLDER_DEFAULT);
    pageSize = getProperty(props, ConfigurationKeys.PageSize, PAGE_SIZE_DEFAULT);
    shouldAppendStackTraceToErrorMessages
            = getProperty(props, ConfigurationKeys.ShouldAppendStackTraceToErrorMessages,
                    SHOULD_APPEND_STACKTRACE_TO_ERROR_MESSAGES_DEFAULT);
    multipleAnnotationPost
            = getProperty(props, ConfigurationKeys.MultipleAnnotationPost, MULTIPLE_ANNOTATION_POST_DEFAULT);
    simpleFormatters = getProperty(props, ConfigurationKeys.SimpleFormatters, SIMPLE_FORMATTERS_DEFAULT);
    corsAllowedOriginsPath
            = getProperty(props, ConfigurationKeys.CorsAllowedOriginsPath, CORS_ALLOWED_ORIGINS_PATH_DEFAULT);
    fallbackValidation = getProperty(props, ConfigurationKeys.FallbackValidation, FALLBACK_VALIDATION_DEFAULT);
  }

  private String getProperty(Properties newProps, ConfigurationKeys key, String defaultValue){
    if(newProps.containsKey(key.toString())){
      return newProps.getProperty(key.toString());
    } else{
      return defaultValue;
    }
  }

  private int getProperty(Properties newProps, ConfigurationKeys key, int defaultValue){
    if(newProps.containsKey(key.toString())){
      return Integer.parseInt(newProps.getProperty(key.toString()));
    } else{
      return defaultValue;
    }
  }

  private long getProperty(Properties newProps, ConfigurationKeys key, long defaultValue){
    if(newProps.containsKey(key.toString())){
      return Long.parseLong(newProps.getProperty(key.toString()));
    } else{
      return defaultValue;
    }
  }

  private boolean getProperty(Properties newProps, ConfigurationKeys key, boolean defaultValue){
    if(newProps.containsKey(key.toString())){
      return Boolean.parseBoolean(newProps.getProperty(key.toString()));
    } else{
      return defaultValue;
    }
  }

  /**
   * Returns the path to the JSON-LD schema folder
   *
   * @return The annotation schema file
   */
  public String getJsonLdValidatorSchemaFolder(){
    return jsonLdValidatorSchemaFolder;
  }

  /**
   * Returns whether annotation schema validation is enabled
   *
   * @return true if enabled, false otherwise
   */
  public boolean isValidationEnabled(){
    return enableValidation;
  }

  /**
   * Returns whether the existence of a label property is mandatory when posting
   * containers
   *
   * @return true if needed, false otherwise
   */
  public boolean isLabelMandatoryInContainers(){
    return enableMandatoryLabelsInContainers;
  }

  /**
   * Returns whether the existence of a slug header is mandatory when posting
   * containers
   *
   * @return true if needed, false otherwise
   */
  public boolean isSlugMandatoryInContainerPosts(){
    return enableMandatorySlugInContainerPost;
  }

  /**
   * Returns whether content negotiation is enabled
   *
   * @return true if enabled
   */
  public boolean isContentNegotiationEnabled(){
    return enableContentNegotiation;
  }

  /**
   * Checks whether the given URL is the root WAP URL (=root container IRI)
   *
   * @param url The URL to check
   * @return true if root WAP URL, false if not
   */
  public boolean isRootWapUrl(String url){
    return (getBaseUrl() + WAP_ENDPOINT).equals(url);
  }

  /**
   * Gets the applications base url without trailing slash "/" (e.g.
   * http://localhost:8080)
   *
   * @return The base url
   */
  public String getBaseUrl(){
    if(enableHttps){
      if(wapPort == 443){
        return "https://" + hostname;
      } else{
        return "https://" + hostname + ":" + wapPort;
      }
    } else{
      if(wapPort == 80){
        return "http://" + hostname;
      } else{
        return "http://" + hostname + ":" + wapPort;
      }
    }
  }

  /**
   * Returns the default Q value to use during content negotiation if none is
   * explicitly specified. This is always 1.0, see http spec
   *
   * @return The default Q value
   */
  public double getDefaultQValue(){
    return 1.0;
  }

  /**
   * Gets the folder where JSON-LD Profiles are cached
   *
   * @return The profile cache folder
   */
  public String getJsonLdProfileFolder(){
    return jsonLdProfileFolder;
  }

  /**
   * Gets the validity in ms of cached JSON-LD profiles. Local copies are
   * updated after this time elapsed.
   *
   * @return The cache validity in ms
   */
  public long getJsonLdCachedProfileValidityInMs(){
    return jsonLdCachedProfileValidityInMs;
  }

  /**
   * Gets the path where the local database is stored
   *
   * @return The database path
   */
  public String getDataBasePath(){
    // This is a hack to help tests to not manipulate live db. it is of no effect in the real application.
    if(isUnitTest()){
      if(dataBasePath.indexOf("temp/") == -1 && dataBasePath.indexOf("temp\\") == -1){
        System.err.println("Getting production DB " + dataBasePath + " during Testing, exit at once");
        Thread.dumpStack();
        System.exit(1); // Not only fail, hard exit. This is a no go. Better recognize at once
        // as tests may eventually destroy the live db.
      }
    }
    return dataBasePath;
  }

  /**
   * Gets the IRI of the root container in this installation
   *
   * @return The root container IRI
   */
  public String getRootContainerIri(){
    return getBaseUrl() + WAP_ENDPOINT;
  }

  /**
   * Gets the folder where the web client is stored
   *
   * @return The web client folder
   */
  public String getWebClientFolder(){
    return webClientFolder;
  }

  /**
   * Gets the folder where the javadoc is stored
   *
   * @return The javadoc folder
   */
  public String getJavaDocFolder(){
    return javaDocFolder;
  }

  /**
   * Returns the filename to use for the profile registry database
   *
   * @return The profile registry database file
   */
  public String getJsonLdProfileFile(){
    return "profiles.xml";
  }

  /**
   * Checks whether fallback validation is active. This means on formats for
   * which a specific validator is not implemented, the validation is performed
   * on JSON-ld. This involves format conversion!
   *
   * @return true if fallback validation is active
   */
  public boolean isFallbackValidationActive(){
    return fallbackValidation;
  }

  /**
   * Returns the RDF backend implementation name used to persist annotations.
   *
   * @return The backend implementation name, which is either the classname with
   * lowercase first letter or the assigned name.
   */
  public String getRdfBackendImplementation(){
    return rdfBackendImplementation;
  }

  @PostConstruct
  private void postConstruct(){
    corsConfig = new CorsConfiguration(getCorsAllowedOriginsPath());
  }

  /**
   * This method applies the cors configuration to all existent controllers
   * globally
   */
  @Override
  public void addCorsMappings(CorsRegistry registry){
    // INFO : There is a default CORS handler for preflight requests that always, no matter what is configured,
    // INFO : intercepts preflight requests. It also answers them even if cors itself is disabled. You cannot
    // INFO : change that in springboot 2.0.2. This means the invalid allowed-methods in preflight requests
    // INFO : cannot be prevented. The original OPTIONS controller is not called. The actual requests is then
    // INFO : calling the real methods.
    // This method is called by spring after all autowired variables have already been set
    // and any bean creation methods have been called. cors config is already created via post construct
    if(!corsConfig.isCorsEnabled()){
      // According to spec : Either do not add headers or reject using 403
      // We cannot prevent spring from answering preflight requests with ok (see INFO above)
      // Or better: We do not know how so far.
      // If we do not configure CORS, the real requests will then contain no headers. We can either
      // add no headers (as SPARQL does), or reject the real requests. To reject, uncomment the lines.
      /*
          * //The only way to get spring to reject CORS requests //is by allowing it only for a never used origin
          * //therefore we create one that should never exist in real requests. registry.addMapping(WAP_ENDPOINT + "**")
          * //Enable CORS for all WAP-endpoints .allowedOrigins(new String[] { "http://invalid_cors_origin_" +
          * System.currentTimeMillis() + ".com/" }); //==> reject all
       */
    } else{
      // CORS is enabled, apply the settings to the underlying jetty server
      final String[] allowedOrigins = corsConfig.getAllowedOrigins();
      final String[] allowedMethods = corsConfig.getAllowedMethods();
      final String[] allowedHeaders = corsConfig.getAllowedHeaders();
      String[] exposedHeaders = corsConfig.getExposedHeaders();
      final int maxAge = corsConfig.getMaxAgeInSeconds();
      final boolean allowCredentials = corsConfig.areCredentialsAllowed();
      if(exposedHeaders.length == 1 && "*".equals(exposedHeaders[0])){
        // This is not supported by spring (and most browsers too). We therefore use a value that lists
        // all headers we might use. Preflight requests do not land in the actual controller implementations
        // and therefore we cannot add the existing headers there. When a value is listed that does not exist
        // this should be the same as if the value just doesn't exist...
        exposedHeaders = new String[]{"Link", "Vary", "Allow", "Date", "Content-Location", "Content-Length",
          "ETag", "Location", "Connection", "Content-Type"};
      }
      logger.info("WAP :   CORS allowed origins = " + Arrays.asList(allowedOrigins));
      logger.info("WAP :   CORS allowed methods = " + Arrays.asList(allowedMethods));
      logger.info("WAP :   CORS allowed headers = " + Arrays.asList(allowedHeaders));
      logger.info("WAP :   CORS exposed headers = " + Arrays.asList(exposedHeaders));
      logger.info("WAP : CORS preflight max age = " + maxAge);
      logger.info("WAP : CORS allow credentials = " + allowCredentials);
      registry.addMapping(WAP_ENDPOINT + "**") // Enable CORS for all WAP-endpoints
              .allowedMethods(allowedMethods).allowedHeaders(allowedHeaders).exposedHeaders(exposedHeaders)
              .maxAge(maxAge).allowCredentials(allowCredentials) // Spring ignores the false if set here and also if
              // not set
              // at all (which should result in no allow credentials header). Seems like a bug.
              .allowedOrigins(allowedOrigins); // And for all origins in the get allowed origins list
    }
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer){
    // The superclass does nothing in this method. We can use all values as is and
    // apply
    // only individual pathMatcher and request the the trailing slashes will not be
    // tested
    PathMatcher pathMatcher = new WapPathMatcher();
    configurer.setPathMatcher(pathMatcher).setUseTrailingSlashMatch(false).setUseSuffixPatternMatch(true);
  }

  /**
   * Gets the folder where JSON-LD Frames are stored
   *
   * @return The folder where frame are stored
   */
  public String getJsonLdFrameFolder(){
    return jsonLdFrameFolder;
  }

  /**
   * Get the page size used to construct page objects
   *
   * @return The page size
   */
  public int getPageSize(){
    return pageSize;
  }

  /**
   * Checks whether posting of multiple annotations in one request is allowed or
   * not
   *
   * @return true if multiple annotations can be added in one request
   */
  public boolean isMultipleAnnotationPostAllowed(){
    return multipleAnnotationPost;
  }

  /**
   * Checks whether the ldp.jsonld (only to containers) and anno.jsonld profile
   * (to all) should be added to every JSON-LD request, even if (intentionally)
   * not requested by the client
   *
   * @return true if default profiles will always be added to JSON-LD requests
   */
  public boolean shouldAlwaysAddDefaultProfilesToJsonLdRequests(){
    return false;
  }

  /**
   * Checks whether a detailed stack trace should be added to error messages
   *
   * @return true if errors should include stack traces
   */
  public boolean shouldAppendStackTraceToErrorMessages(){
    return shouldAppendStackTraceToErrorMessages;
  }

  /**
   * Get stack trace max depth
   *
   * @return The max depth of stack trace elements to include
   */
  public int getMaxNumberOfStackTraceElementsToInclude(){
    return stackTraceElements;
  }

  /**
   * Gets the hostname (used for WAP Iries)
   *
   * @return The hostname
   */
  public String getHostname(){
    return hostname;
  }

  /**
   * Gets the port to use for the WAP REST endpoint
   *
   * @return The WAP port
   */
  public int getWapPort(){
    return wapPort;
  }

  /**
   * Gets the IP to use for the WAP REST endpoint
   *
   * @return The WAP IP
   */
  public String getWapIp(){
    return wapIp;
  }

  /**
   * Gets the port to use for the SPARQL READ endpoint
   *
   * @return The SPARQL READ port
   */
  public int getSparqlReadPort(){
    return sparqlReadPort;
  }

  /**
   * Gets the IP to use for the SPARQL READ endpoint
   *
   * @return The SPARQL READ IP
   */
  public String getSparqlReadIp(){
    return sparqlReadIp;
  }

  /**
   * Gets the port to use for the SPARQL WRITE endpoint
   *
   * @return The SPARQL WRITE port
   */
  public int getSparqlWritePort(){
    return sparqlWritePort;
  }

  /**
   * Gets the IP to use for the SPARQL WRITE endpoint
   *
   * @return The SPARQL WRITE IP
   */
  public String getSparqlWriteIp(){
    return sparqlWriteIp;
  }

  /**
   * Checks whether https is enabled or not
   *
   * @return true if https is enabled
   */
  public boolean isHttpsEnabled(){
    return enableHttps;
  }

  /**
   * Gets the String representation of the additional simple formats to register
   *
   * @return The simple formats String
   */
  public String getSimpleFormatters(){
    return simpleFormatters;
  }

  /**
   * Gets the path to the CORS allowed origins file
   *
   * @return The path to the CORS allowed origins file
   */
  public String getCorsAllowedOriginsPath(){
    return corsAllowedOriginsPath;
  }

  /**
   * Returns the actual Cors Configuration to use
   *
   * @return the cors configuration
   */
  public CorsConfiguration getCorsConfiguration(){
    return corsConfig;
  }
}
