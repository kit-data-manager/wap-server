package edu.kit.scc.dem.wapsrv.app;

import java.util.EnumSet;
import java.util.List;
import java.util.Vector;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration.Dynamic;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.servlets.CrossOriginFilter;
import org.apache.jena.tdb2.TDB2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import edu.kit.scc.dem.wapsrv.repository.jena.JenaDataBase;

/**
 * Runs the Fuseki Servers as soon as the Application Context of Spring is fully started
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Component
public class FusekiRunner {
   /**
    * The prefix used before the endpoint provided by Fuseki. When using /ds on host:3332 this leads to :<br>
    * SPARQL Query http://host:3332/ds/query<br>
    * SPARQL Query http://host:3332/ds/sparql<br>
    * GSP read-only http://host:3332/ds/data<br>
    * GET quads http://host:3332/ds
    */
   public static final String ENDPOINT_PREFIX = "/wap"; // It does not work without a prefix ("" or "/")
   // Therefore we use the same as on the wap endpoint so far
   /**
    * Helper object for tests only, so running servers can be detected and shut down
    */
   private static List<FusekiServer> runningServers = new Vector<FusekiServer>();
   @Autowired
   private JenaDataBase dataBase;
   private FusekiServer server1;
   private FusekiServer server2;
   @Autowired
   private WapServerConfig config;
   /**
    * The logger to use
    */
   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   /**
    * Initializes the Fuseki servers
    */
   @EventListener(ApplicationReadyEvent.class)
   public void init() {
      int readPort = config.getSparqlReadPort();
      int writePort = config.getSparqlWritePort();
      boolean readLoopback = config.getSparqlReadIp().equalsIgnoreCase("localhost")
            || config.getSparqlReadIp().equalsIgnoreCase("loopback");
      boolean writeLoopback = config.getSparqlWriteIp().equalsIgnoreCase("localhost")
            || config.getSparqlWriteIp().equalsIgnoreCase("loopback");
      if (readPort > 0 || writePort > 0) {
         if (!runningServers.isEmpty()) {
            for (FusekiServer server : runningServers) {
               server.stop();
               server.join();
               System.out.println("@@@@@@@@@@@@@@@@@Unexpected Fuseki Server SHUTDOWN @@@@@@@@@@@@@@");
            }
            runningServers.clear();
         }
         System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@ Application ready for Fuseki start @@@@@@@@@@@@@@@@@@@@@@");
         dataBase.getDataBase().getContext().set(TDB2.symUnionDefaultGraph, true);
         // true == read-write
         if (writePort > 0) {
            server1 = FusekiServer.create().port(writePort).add(ENDPOINT_PREFIX, dataBase.getDataBase(), true)
                  .loopback(writeLoopback).build();
            addCorsFilter(server1, true);
            server1.start();
            System.out.println("@ Fuseki running on Port: " + writePort + " at endpoint: " + ENDPOINT_PREFIX
                  + " with full access @");
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@ Fuseki Server1 is running @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            runningServers.add(server1);
         }
         // false == read-only
         if (readPort > 0) {
            server2 = FusekiServer.create().port(readPort).add(ENDPOINT_PREFIX, dataBase.getDataBase(), false)
                  .loopback(readLoopback).build();
            addCorsFilter(server2, false);
            server2.start();
            System.out.println(
                  "@ Fuseki running on Port: " + readPort + " at endpoint: " + ENDPOINT_PREFIX + " with read access @");
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@ Fuseki Server2 is running @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            runningServers.add(server2);
         }
      } else {
         System.out.println("SPARQL disabled");
      }
   }

   /**
    * Add the CORS filters to the fuseki server, if configured
    * 
    * @param fusekiServer
    *                     The server to add CORS functionality to
    * @param writeable
    *                     If the server is writable (true) or read-only (false)
    */
   private void addCorsFilter(FusekiServer fusekiServer, boolean writeable) {
      CorsConfiguration corsConfig = config.getCorsConfiguration();
      // if CORS config is not existing or disabled, return
      if (corsConfig == null || !corsConfig.isCorsEnabled()) {
         return;
      }
      String[] allowedOriginsArray = corsConfig.getAllowedOrigins();
      // If CORS is enabled, apply the settings to the underlying jetty server
      final String allowedOrigins = CorsConfiguration.buildCommaSeparatedString(allowedOriginsArray);
      final String allowedHeaders = CorsConfiguration.buildCommaSeparatedString(corsConfig.getAllowedHeaders());
      final String exposedHeaders = CorsConfiguration.buildCommaSeparatedString(corsConfig.getExposedHeaders());
      final int maxAge = corsConfig.getMaxAgeInSeconds();
      final boolean allowCredentials = corsConfig.areCredentialsAllowed();
      logger.info("FUSEKI :   CORS allowed origins = " + allowedOrigins);
      logger.info("FUSEKI :   CORS allowed methods = always copied from original Allow and requested-methods header");
      logger.info("FUSEKI :   CORS allowed headers = " + allowedHeaders);
      logger.info("FUSEKI :   CORS exposed headers = " + exposedHeaders);
      logger.info("FUSEKI : CORS preflight max age = " + maxAge);
      logger.info("FUSEKI : CORS allow credentials = " + allowCredentials);
      // We need to add additional functionality regarding Access-Control- headers ommitted by default implementation
      CrossOriginFilter crossOriginFilter = createCrossOriginFilter(maxAge, allowedHeaders, exposedHeaders);
      Dynamic context = fusekiServer.getServletContext().addFilter("cors", crossOriginFilter);
      context.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, allowedOrigins);
      context.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, allowedHeaders);
      context.setInitParameter(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM, maxAge + "");
      context.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, Boolean.toString(allowCredentials));
      context.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
   }

   /**
    * Create a cross origin filter to use with the jetty/fuseki endpoints
    * 
    * @param  maxAge
    *                        The maximum age (validity) for preflight requests
    * @param  allowedHeaders
    *                        The headers that should be allowed in requests
    * @param  exposedHeaders
    *                        The headers that should be accessible in responses
    * @return                A cross origin filter that enforces the given parameters
    */
   private CrossOriginFilter createCrossOriginFilter(final int maxAge, final String allowedHeaders,
                                                     final String exposedHeaders) {
      // The jena/jetty implementation has some flaws we have to work around
      // Details are found within the CorsFilter class
      return new CorsFilter(maxAge, allowedHeaders, exposedHeaders);
   }

   /**
    * Deinitializes the Fuseki server
    */
   @EventListener(ContextClosedEvent.class)
   public void deinit() {
      if (server1 != null) {
         server1.stop();
         server1.join();
         System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Fuseki Server 1 SHUTDOWN @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
         server1 = null;
      }
      if (server2 != null) {
         server2.stop();
         server2.join();
         System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Fuseki Server 2 SHUTDOWN @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
         server2 = null;
      }
   }
}
