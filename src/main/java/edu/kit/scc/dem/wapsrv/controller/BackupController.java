package edu.kit.scc.dem.wapsrv.controller;

import java.util.Arrays;
import java.util.HashSet;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.service.BackupService;

/**
 * The WAP Controller used for requests to generate Backups of the Database during normal operation.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@RestController
@RequestMapping("/backup")
public class BackupController {
   /**
    * The logger to use
    */
   private final Logger logger = LoggerFactory.getLogger(this.getClass());
   /**
    * The backup service
    */
   @Autowired
   private BackupService backupService;

   /**
    * This method implements the endpoint for HTTP GET requests to generate Backups
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(method = {RequestMethod.GET})
   public ResponseEntity<?> getBackup(HttpServletRequest request, @RequestHeader HttpHeaders headers)
         throws WapException {
      logger.info("create backup");
      String filename = backupService.backupDatabase();
      final HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders
            .setAllow(new HashSet<HttpMethod>(Arrays.asList(new HttpMethod[] {HttpMethod.GET, HttpMethod.OPTIONS})));
      return new ResponseEntity<>(filename, responseHeaders, HttpStatus.OK);
   }

   /**
    * This method implements the endpoint for HTTP HEAD requests to generate Backups. These request do not make sense
    * and are not allowed.
    * 
    * @param  request
    *                 The request the client sent
    * @param  headers
    *                 The headers of the request
    * @return         A response object to sent to the client
    */
   @RequestMapping(method = {RequestMethod.HEAD})
   public ResponseEntity<?> headBackup(HttpServletRequest request, @RequestHeader HttpHeaders headers) {
      final HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders
            .setAllow(new HashSet<HttpMethod>(Arrays.asList(new HttpMethod[] {HttpMethod.GET, HttpMethod.OPTIONS})));
      return new ResponseEntity<>(responseHeaders, HttpStatus.METHOD_NOT_ALLOWED);
   }

   /**
    * This method implements the endpoint for HTTP OPTIONS requests to generate backup
    * 
    * @param  request
    *                 The request the client sent
    * @param  headers
    *                 The headers of the request
    * @return         A response object to sent to the client
    */
   @RequestMapping(method = {RequestMethod.OPTIONS})
   public ResponseEntity<?> optionsBackup(HttpServletRequest request, @RequestHeader HttpHeaders headers) {
      final HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders
            .setAllow(new HashSet<HttpMethod>(Arrays.asList(new HttpMethod[] {HttpMethod.GET, HttpMethod.OPTIONS})));
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
   }
}
