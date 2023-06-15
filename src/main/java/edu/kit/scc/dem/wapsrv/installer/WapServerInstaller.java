package edu.kit.scc.dem.wapsrv.installer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.kit.scc.dem.wapsrv.app.ConfigurationKeys;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;

/**
 * This class is responsible for detecting that installation is needed and for installing the application afterwards
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class WapServerInstaller {
   private static enum State {
      INITIAL_STATE, EXIT_FALSE, EXIT_TRUE, SAVE_CONFIG, INPUT_HOSTNAME, INPUT_PROTOCOL, INPUT_IP, INPUT_PORT,
      INPUT_DATABASE_PATH, SHOW_SUMMARY, SELECT_DIRECT_START, OK
   }

   private static final int YES = 1;
   private static final int NO = 0;
   private static final String QUIT_STRING = "quit";
   private static final String RESET_STRING = "reset";
   private static final String MESSAGE_STARTING_INSTALLATION = "Starting installation";
   private static final String ERROR_FOLDER_NOT_READABLE = "Directory with Application Jar not readable";
   private static final String MESSAGE_CONFIG_FILE_CREATED
         = "Created configuration file : " + WapServerConfig.propertiesFile;
   private static final String MESSAGE_INITIAL_MESSAGE = "The WAP Server Application is going to be installed now.\n"
         + "The first step is to ask for some basic settings needed.\n"
         + "These are related to root container IRI generation and cannot be easily changed afterwards.\n"
         + "The process can always be ended by typing \"" + QUIT_STRING + "\" to exit installation, or \""
         + RESET_STRING + "\" to return\n"
         + "to this message and start over again. If no input is provided for a given question,\n"
         + "the default values will be used. Defaults are shown in [ ] brackets.\n"
         + "All input is not case-sensitive.";
   private static final String MESSAGE_INPUT_HOSTNAME = "Please enter the hostname to use";
   private static final String MESSAGE_INPUT_IP = "Please enter the IP to use ( * = listen on all)";
   private static final String MESSAGE_INPUT_PORT = "Please enter the port to use";
   private static final String MESSAGE_INPUT_PROTOCOL = "Please enter the protocoll (http or https)";
   private static final String MESSAGE_INPUT_DATABASE_PATH
         = "Please enther the path to the database " + "(relative path ==> prefix with \"./\"";
   private static final String MESSAGE_SUMMARY_OK = "Summary ok ?  Enter \"ok\" to confirm";
   private static final String MESSAGE_PREPARE_HTTPS = "The selected protocol https does not allow a direct startup.\n"
         + "You have to provide some data needed for https-initialization:\n" + "  - pkcs12 Key-store file\n"
         + "  - ssl/ssl.conf has been created, fill the values as needed\n\n"
         + "A guide to create this data can be found under howtos/ssl.txt\n"
         + "After you have provided these in the folder \"ssl/\", restart the server.\n\n"
         + "!!!  This data is confidential, especially the clear text passwords within ssl.conf. "
         + "Restrict access to this folder  !!!\n";
   private static final String MESSAGE_DIRECT_STARTUP_INFO = "The config file has been created with provided values.\n"
         + "The rest has been set to default. The server can now either be started with these defaults\n"
         + "or it can be closed down. This enables you to change the settings prior to startup.";
   private static final String MESSAGE_DIRECT_STARTUP_QUESTION = "Should server start immediately (\"yes\" or \"no\")?";
   private static final String MESSAGE_IP_NOT_EXISTENT = "Selected IP is a valid IP, but does not exist on this host.";
   private static final String MESSAGE_GO_ON = "Should I go on anyway (\"yes\" or \"no\")?";
   private static final String MESSAGE_DATABASE_PATH_ALREADY_EXISTS = "The database path already exists.\n"
         + "If a valid database exists there, the server will reuse it. This may be the intention, but also a fault.\n"
         + "If the database found does not exactly fit the root container IRI shown later,\n"
         + "the application will be rendered unusable. Should I continue?";
   private static final String ERROR_SSL_FOLDER_NOT_CREATEABLE = "Cannot create ssl folder";
   @SuppressWarnings("unused")
   private static final String ERROR_SSL_FOLDER_NOT_WRITEABLE = "Cannot write to ssl folder";
   /**
    * The logger to use
    */
   private static Logger logger = LoggerFactory.getLogger(WapServerInstaller.class);
   /**
    * The state
    */
   private State state = State.INITIAL_STATE;
   /**
    * The properties to finally save and until then the source of default values
    */
   private final Properties configurationProperties = WapServerConfig.getDefaultProperties();
   private String hostname;
   private int port;
   private String ip;
   private String databasePath;
   private String protocol;
   /**
    * The jar file running
    */
   private final File jarFile;
   /**
    * The jar file running as URL
    */
   private final URL jarUrl;

   /**
    * Constructs a new installer
    */
   public WapServerInstaller() {
      jarUrl = JarUtilities.getCurrentlyRunningJarUrl();
      jarFile = JarUtilities.getCurrentlyRunningJarFile();
   }

   /**
    * Constructs a new installer
    * 
    * @param jarFile
    *                The jar file to use
    * @param jarUrl
    *                The url of the jar file
    */
   public WapServerInstaller(File jarFile, URL jarUrl) {
      this.jarUrl = jarUrl;
      this.jarFile = jarFile;
   }

   /**
    * Called to initialize a manual installation if either the need to install is not recognized or a repair/refresh
    * should be performed (then only inform)
    * 
    * @return true if no problems occured, false otherwise
    */
   public static boolean performManualInstallation() {
      // Check that the folder is empty
      File dir = new File("./");
      int jarCounter = 0;
      File[] files = dir.listFiles();
      if (files == null) {
         System.err.println(ERROR_FOLDER_NOT_READABLE);
         return false;
      }
      // Check only one jar (that must be the actual jar then) exists
      boolean dirsExist = false;
      for (File file : files) {
         if (file.isDirectory()) {
            dirsExist = true;
            break;
         }
         boolean jar = file.getName().toLowerCase().endsWith(".jar");
         if (jar) {
            jarCounter++;
         }
      }
      // Create console reader
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      boolean installPossible = jarCounter == 1 && files.length == 1;
      if (dirsExist || !installPossible) {
         if (!shouldInstallAnyway(files, reader)) {
            return false;
         }
      }
      URL jarUrl = JarUtilities.generateUrl(files[0]);
      if (jarUrl == null) {
         System.out.println("");
         System.out.println("##################");
         System.out.println("Installation not possible, could not create URL for the installation jar file");
         System.out.println("##################");
         System.out.println("");
         return false;
      }
      // If nothing but the jar there, installation may be performed
      return new WapServerInstaller(files[0], jarUrl).install(reader);
   }

   private static boolean shouldInstallAnyway(File[] files, BufferedReader reader) {
      System.out.println("");
      System.out.println("##################");
      System.out.println("Installation not possible without explicit OK:");
      System.out.println("The folder must not contain any files or subfolders except");
      System.out.println("the single jarfile of the Wap Server Application.");
      System.out.println("");
      System.out.println("These files exist:");
      for (File file : files) {
         System.out.println("   - " + file.getName());
      }
      System.out.println("");
      String choice = readString(reader, "Should I continue anyway then enter \"ok\" ? ", (String) null);
      return "OK".equalsIgnoreCase(choice);
   }

   /**
    * Checks whether the application needs to be installed. That is the case if the actual directory containts nothing
    * but the executable jar file.
    * 
    * @return true if installation necessary
    */
   public static boolean isInstallationNecessary() {
      File jarFile = JarUtilities.getCurrentlyRunningJarFile();
      if (jarFile == null) {
         logger.info("Not running from jar file, installation will never trigger within workspace");
         return false;
      }
      File dir = new File("./");
      File[] files = dir.listFiles(new FileFilter() {
         @Override
         public boolean accept(File pathname) {
            return !pathname.getName().toLowerCase().endsWith(".jar");
         }
      });
      if (files == null) {
         System.err.println(ERROR_FOLDER_NOT_READABLE);
         return false;
      }
      // If nothing but the jar there, installation needed
      return files.length == 0;
   }

   /**
    * Prints a message surround by some cosmetic characters to emphasize it
    * 
    * @param message
    *                The message to print
    */
   private static void printPhase(String message) {
      System.out.println("#################################");
      System.out.println("### " + message);
      System.out.println("#################################\n");
   }

   /**
    * Prints a simple message
    * 
    * @param message
    *                The message
    */
   private static void log(String message) {
      System.out.println(message + "\n");
   }

   private static String readString(BufferedReader in, String question, String defaultValue) {
      System.out.print(question + (defaultValue != null ? " [" + defaultValue + "] : " : ""));
      try {
         String input = in.readLine();
         if (input != null) {
            input = input.trim();
            if (input.length() == 0) {
               if (defaultValue == null) {
                  System.out.print("No default value given, please enter something not empty");
                  return readString(in, question, defaultValue);
               } else {
                  return defaultValue;
               }
            } else {
               return input;
            }
         } else {
            // There is no official way to get readLine() to return null besides closing the stream
            return QUIT_STRING;
         }
      } catch (IOException e) {
         // readLine() should not throw an IOException if everything is ok with the stream
         return QUIT_STRING;
      }
   }

   /**
    * Install the application.
    * 
    * @param  in
    *            The system in reader
    * @return    true to start of app directly, false to shutdown
    */
   public boolean install(BufferedReader in) {
      printPhase(MESSAGE_STARTING_INSTALLATION);
      log("Running jar file : " + jarFile.getName());
      log("     jar file url: " + jarUrl.toString());
      while (state != State.EXIT_FALSE && state != State.EXIT_TRUE) {
         switch (state) {
         case INITIAL_STATE:
            state = executeInitialState(in);
            break;
         case INPUT_PROTOCOL:
            state = selectProtocol(in);
            break;
         case INPUT_HOSTNAME:
            state = inputHostname(in);
            break;
         case INPUT_PORT:
            state = inputPort(in);
            break;
         case INPUT_IP:
            state = inputIp(in);
            break;
         case INPUT_DATABASE_PATH:
            state = inputDatabasePath(in);
            break;
         case SHOW_SUMMARY:
            state = showSummary(in);
            break;
         case SAVE_CONFIG:
            state = saveConfig(in);
            break;
         case SELECT_DIRECT_START:
            state = startDirectly(in);
            break;
         default:
            throw new RuntimeException("Unknown state in loop");
         }
         if (state == null) {
            throw new RuntimeException("Unallowed null as new state in loop");
         }
      }
      // we cannot close the console reader, or we would otherwise close system.in
      // but if we never read again, nothing unwanted will happen during later application
      // execution as side effect for other threads eventually reading from System.in
      // System.out.println("We would start the server ? " + (state == State.EXIT_TRUE));
      // state = State.EXIT_FALSE;
      return state == State.EXIT_TRUE;
   }

   private String readString(BufferedReader in, String question, ConfigurationKeys key) {
      return readString(in, question, configurationProperties.getProperty(key.toString()));
   }

   private State selectProtocol(BufferedReader in) {
      boolean enableHttps
            = Boolean.parseBoolean(configurationProperties.getProperty(ConfigurationKeys.EnableHttps.toString()));
      String defaultProtocol = enableHttps ? "https" : "http";
      String protocol = readString(in, MESSAGE_INPUT_PROTOCOL, defaultProtocol);
      if (RESET_STRING.equalsIgnoreCase(protocol)) {
         return State.INITIAL_STATE;
      }
      if (QUIT_STRING.equalsIgnoreCase(protocol)) {
         return State.EXIT_FALSE;
      }
      while (!("https".equalsIgnoreCase(protocol) || "http".equalsIgnoreCase(protocol))) {
         log("Invalid input, valid are : empty = empty for default, http, https, " + QUIT_STRING + ", " + RESET_STRING);
         protocol = readString(in, MESSAGE_INPUT_PROTOCOL, defaultProtocol);
         if (RESET_STRING.equalsIgnoreCase(protocol)) {
            return State.INITIAL_STATE;
         }
         if (QUIT_STRING.equalsIgnoreCase(protocol)) {
            return State.EXIT_FALSE;
         }
      }
      protocol = protocol.toLowerCase();
      log("Selected protocol : " + protocol);
      this.protocol = protocol;
      return State.INPUT_HOSTNAME;
   }

   private State inputHostname(BufferedReader in) {
      String hostname = readString(in, MESSAGE_INPUT_HOSTNAME, ConfigurationKeys.Hostname);
      if (RESET_STRING.equalsIgnoreCase(hostname)) {
         return State.INITIAL_STATE;
      }
      if (QUIT_STRING.equalsIgnoreCase(hostname)) {
         return State.EXIT_FALSE;
      }
      hostname = hostname.toLowerCase();
      log("Selected hostname : " + hostname);
      this.hostname = hostname;
      return State.INPUT_PORT;
   }

   private State inputPort(BufferedReader in) {
      String port = readString(in, MESSAGE_INPUT_PORT, ("https".equals(protocol) ? "443" : "80"));
      if (RESET_STRING.equalsIgnoreCase(port)) {
         return State.INITIAL_STATE;
      }
      if (QUIT_STRING.equalsIgnoreCase(port)) {
         return State.EXIT_FALSE;
      }
      while (!isValidPort(port)) {
         port = readString(in, MESSAGE_INPUT_PORT, ("https".equals(protocol) ? "443" : "80"));
         if (RESET_STRING.equalsIgnoreCase(port)) {
            return State.INITIAL_STATE;
         }
         if (QUIT_STRING.equalsIgnoreCase(port)) {
            return State.EXIT_FALSE;
         }
      }
      port = port.toLowerCase();
      log("Selected port : " + port);
      this.port = Integer.parseInt(port);
      return State.INPUT_IP;
   }

   private boolean isValidPort(String portString) {
      final int minPort = 1;
      final int maxPort = 65535;
      try {
         int port = Integer.parseInt(portString);
         return port >= minPort && port <= maxPort;
      } catch (NumberFormatException ex) {
         return false;
      }
   }

   private State inputIp(BufferedReader in) {
      String ip = readString(in, MESSAGE_INPUT_IP, ConfigurationKeys.WapIp);
      if (RESET_STRING.equalsIgnoreCase(ip)) {
         return State.INITIAL_STATE;
      }
      if (QUIT_STRING.equalsIgnoreCase(ip)) {
         return State.EXIT_FALSE;
      }
      while (!isValidIp(ip)) {
         log("Not a valid IP or address does not exist " + ip);
         ip = readString(in, MESSAGE_INPUT_IP, ConfigurationKeys.WapIp);
         if (RESET_STRING.equalsIgnoreCase(ip)) {
            return State.INITIAL_STATE;
         }
         if (QUIT_STRING.equalsIgnoreCase(ip)) {
            return State.EXIT_FALSE;
         }
      }
      ip = ip.toLowerCase();
      log("Selected IP : " + ip);
      if (!isLocalIp(ip)) {
         log(MESSAGE_IP_NOT_EXISTENT);
         int choice = getYesOrNo(in, MESSAGE_GO_ON);
         if (choice == NO) {
            return inputIp(in);
         } else {
            // ok, leave this method and done
         }
      }
      this.ip = ip;
      return State.INPUT_DATABASE_PATH;
   }

   private boolean isValidIp(String ip) {
      if ("*".equals(ip)) {
         return true;
      }
      try {
         InetAddress.getByName(ip);
         return true;
      } catch (UnknownHostException e) {
         e.printStackTrace();
         return false;
      }
   }

   private boolean isLocalIp(String ip) {
      if ("*".equals(ip)) {
         return true;
      }
      try {
         InetAddress ipAddress = InetAddress.getByName(ip);
         Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
         while (nets.hasMoreElements()) {
            NetworkInterface netIf = nets.nextElement();
            Enumeration<InetAddress> addresses = netIf.getInetAddresses();
            while (addresses.hasMoreElements()) {
               InetAddress address = addresses.nextElement();
               if (address.equals(ipAddress)) {
                  return true;
               }
            }
         }
         return false; // not found
      } catch (SocketException e) {
         System.err.println(e.getMessage());
         return false;
      } catch (UnknownHostException e) {
         // This exception cannot occur, the address has been verified before
         System.err.println(e.getMessage());
         return false;
      }
   }

   private State inputDatabasePath(BufferedReader in) {
      String path = readString(in, MESSAGE_INPUT_DATABASE_PATH, "./" + ConfigurationKeys.DataBasePath);
      if (RESET_STRING.equalsIgnoreCase(path)) {
         return State.INITIAL_STATE;
      }
      if (QUIT_STRING.equalsIgnoreCase(path)) {
         return State.EXIT_FALSE;
      }
      while (!isValidDbPath(path)) {
         path = readString(in, MESSAGE_INPUT_DATABASE_PATH, "./" + ConfigurationKeys.DataBasePath);
         if (RESET_STRING.equalsIgnoreCase(path)) {
            return State.INITIAL_STATE;
         }
         if (QUIT_STRING.equalsIgnoreCase(path)) {
            return State.EXIT_FALSE;
         }
      }
      path = path.toLowerCase();
      log("Selected database path : " + path);
      if (new File(path).exists()) {
         log(MESSAGE_DATABASE_PATH_ALREADY_EXISTS);
         int choice = getYesOrNo(in, MESSAGE_GO_ON);
         if (choice == NO) {
            return inputDatabasePath(in);
         } else {
            // ok, leave this method and done
         }
      }
      this.databasePath = path;
      return State.SHOW_SUMMARY;
   }

   private boolean isValidDbPath(String path) {
      // valid means it either exists or can be created parent folder is writable
      File file = new File(path);
      if (file.exists()) {
         return file.isDirectory();
      } else {
         File parent = file.getParentFile();
         while (parent != null && !parent.exists()) {
            // We might specify somthing that needs a bunch of folders to be created...
            parent = file.getParentFile();
         }
         if (parent == null)
            return false;
         else {
            return parent.canWrite();
         }
      }
   }

   private State showSummary(BufferedReader in) {
      printPhase("Summary");
      System.out.println("Protocol = " + protocol);
      System.out.println("Hostname = " + hostname);
      System.out.println("      IP = " + ip);
      System.out.println("    Port = " + port);
      System.out.println(" DB Path = " + databasePath);
      System.out.println();
      String portString = getPortString(protocol, port);
      String rootIri = protocol + "://" + hostname + portString + WapServerConfig.WAP_ENDPOINT;
      System.out.println(" ==> Root container IRI = " + rootIri);
      System.out.println();
      State state = requestOk(in, MESSAGE_SUMMARY_OK);
      if (state == State.OK)
         return State.SAVE_CONFIG;
      else
         return state;
   }

   private State requestOk(BufferedReader in, String message) {
      String choice = readString(in, message + " : ", (String) null);
      if (RESET_STRING.equalsIgnoreCase(choice)) {
         return State.INITIAL_STATE;
      }
      if (QUIT_STRING.equalsIgnoreCase(choice)) {
         return State.EXIT_FALSE;
      }
      while (!"ok".equalsIgnoreCase(choice)) {
         log("Invalid input, valid are : ok,  " + QUIT_STRING + ", " + RESET_STRING);
         choice = readString(in, message + " : ", (String) null);
         if (RESET_STRING.equalsIgnoreCase(choice)) {
            return State.INITIAL_STATE;
         }
         if (QUIT_STRING.equalsIgnoreCase(choice)) {
            return State.EXIT_FALSE;
         }
      }
      return State.OK;
   }

   private String getPortString(String protocol, int port) {
      if (protocol.equals("https") && port == 443) {
         return "";
      }
      if (protocol.equals("http") && port == 80) {
         return "";
      }
      return ":" + port;
   }

   private State saveConfig(BufferedReader in) {
      configurationProperties.setProperty(ConfigurationKeys.Hostname.toString(), hostname);
      configurationProperties.setProperty(ConfigurationKeys.EnableHttps.toString(),
            Boolean.toString("https".equals(protocol)));
      configurationProperties.setProperty(ConfigurationKeys.WapIp.toString(), ip);
      configurationProperties.setProperty(ConfigurationKeys.WapPort.toString(), port + "");
      configurationProperties.setProperty(ConfigurationKeys.DataBasePath.toString(), databasePath);
      try {
         FileOutputStream out = new FileOutputStream(WapServerConfig.propertiesFile);
         configurationProperties.store(out, "Wap Server Configuration");
         out.flush();
         out.close();
         // We may create the database folder here, but we leave this up to the database class
         // Default jena behavior : Non existing folder ==> create it and a new database within
         if ("https".equals(protocol)) {
            createSslFolder();
         }
         // extract javadoc, webcontent, schemas ...
         extractUtilityData();
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
         return State.EXIT_FALSE;
      }
      log(MESSAGE_CONFIG_FILE_CREATED);
      return State.SELECT_DIRECT_START;
   }

   private void extractUtilityData() throws IOException {
      File schemaFolder = new File("./schemas");
      if (!schemaFolder.mkdir() || !JarUtilities.extractFolder(jarUrl, "schemas/", schemaFolder)) {
         throw new IOException("Could not extract JSON-LD schemas");
      }
      File webFolder = new File(configurationProperties.getProperty(ConfigurationKeys.WebClientFolder.toString()));
      if (!webFolder.mkdir() || !JarUtilities.extractFolder(jarUrl, "webcontent/", webFolder)) {
         throw new IOException("Could not extract webcontent");
      }
      File docFolder = new File(configurationProperties.getProperty(ConfigurationKeys.JavaDocFolder.toString()));
      if (!docFolder.mkdir() || !JarUtilities.extractFolder(jarUrl, "doc/", docFolder)) {
         throw new IOException("Could not extract javadoc");
      }
      File frameFolder = new File(configurationProperties.getProperty(ConfigurationKeys.JsonLdFrameFolder.toString()));
      if (!frameFolder.mkdir() || !JarUtilities.extractFolder(jarUrl, "profiles/", frameFolder)) {
         throw new IOException("Could not extract JSON-LD frames");
      }
   }

   private void createSslFolder() throws IOException {
      File folder = new File("./ssl");
      if (!folder.mkdirs()) {
         throw new IOException(ERROR_SSL_FOLDER_NOT_CREATEABLE);
      }
      String conf = "key-store-file=wap.pkcs12" + System.lineSeparator() + "key-store-password=KeyStorePw"
            + System.lineSeparator() + "key-password=PrivateKeyPw" + System.lineSeparator() + "server.ssl.alias=1";
      FileOutputStream out = new FileOutputStream(new File(folder, "ssl.conf"));
      out.write(conf.getBytes());
      out.flush();
      out.close();
   }

   private State startDirectly(BufferedReader in) {
      if ("https".equals(protocol)) {
         log(MESSAGE_PREPARE_HTTPS);
         return State.EXIT_FALSE;
      }
      printPhase("Direct startup");
      log(MESSAGE_DIRECT_STARTUP_INFO);
      int choice = getYesOrNo(in, MESSAGE_DIRECT_STARTUP_QUESTION);
      return YES == choice ? State.EXIT_TRUE : State.EXIT_FALSE;
   }

   private int getYesOrNo(BufferedReader in, String message) {
      String choice = readString(in, message + " ", (String) null);
      while (!"yes".equalsIgnoreCase(choice) && !"no".equalsIgnoreCase(choice)) {
         log("Invalid input, valid are ONLY \"yes\" or \"no\"");
         choice = readString(in, message + " ", (String) null);
      }
      return "yes".equalsIgnoreCase(choice) ? YES : NO;
   }

   private State executeInitialState(BufferedReader in) {
      // reset values
      hostname = null;
      port = -1;
      ip = null;
      databasePath = null;
      protocol = null;
      // Show greeting message
      log(MESSAGE_INITIAL_MESSAGE);
      pressEnterToContinue(in);
      return State.INPUT_PROTOCOL;
   }

   private void pressEnterToContinue(BufferedReader in) {
      System.out.print("<Press enter to continue>");
      try {
         in.readLine();
         System.out.println();
      } catch (IOException e) {
         // nothing to do here, we cannot recover from any I/O error here
      }
   }
}
