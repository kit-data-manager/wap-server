package edu.kit.scc.dem.wapsrv.controller;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.exceptions.MethodNotAllowedException;
import edu.kit.scc.dem.wapsrv.exceptions.NotExistentException;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;

/**
 * Simple folder controllers are simple http servers which serve a given folder in the filesystem. Only GET, HEAD and
 * OPTIONS is usable. The files have to be simple text files of various types like html, css... and the format needs to
 * be utf-8 or character encoding might not be correct .Requests are only served if the file requested lies within the
 * given base folder, symlinks and similar stuff that redirect the target to something outside the basefolder are
 * rejected. No security measures have been taken beyond that. As long as the file is readable, it will be served.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public abstract class SimpleFolderServerController extends BasicController {

   /**
    * The logger to use
    */
   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   /**
    * The base url where request are issued against
    */
   private final String baseUrl;

   /**
    * The base folder where requests relative to base url are served from
    */
   private final String baseFolder;

   /**
    * Creates a new instance serving request relative to baseUrl from baseFolder.
    * 
    * @param baseUrl
    *                   The baseUrl
    * @param baseFolder
    *                   The baseFolder
    */
   protected SimpleFolderServerController(String baseUrl, String baseFolder) {
      this.baseUrl = baseUrl;
      this.baseFolder = baseFolder;
      logger.info("Started simple folder server for " + baseUrl + " ==> " + baseFolder);
   }

   /**
    * Gets the mime type for a given filename
    * 
    * @param  filename
    *                  The filename
    * @return          The string to use in Content-Type headers
    */
   protected String getContentTypeString(String filename) {
      if (filename == null) {
         throw new InternalServerException("Null file request not allowed");
      }

      final String name = filename.toLowerCase().trim();

      if (name.endsWith(".jsonld")) {
         return "application/ld+json; charset=utf-8";
      }
      if (name.endsWith(".json")) {
         return "application/json; charset=utf-8";
      } else if (name.endsWith(".js")) {
         return "text/javascript; charset=utf-8";
      } else if (name.endsWith(".html")) {
         return "text/html; charset=utf-8";
      } else if (name.endsWith(".css")) {
         return "text/css; charset=utf-8";
      } else {
         return "text/plain; charset=utf-8";
      }
   }

   /**
    * Gets the response created after reading in the file denoted by the given baseUrl / baseFolder
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   protected ResponseEntity<?> getFileResponse(HttpServletRequest request, @RequestHeader HttpHeaders headers)
         throws WapException {
      String url = request.getRequestURL().toString();
      if (!url.startsWith(baseUrl)) {
         // Should never happen if correctly used by subclasses
         throw new InternalServerException(ErrorMessageRegistry.INTERAL_FOLDER_SERVER_UNEXPECTED_MAPPING
               + " : Responsible for " + baseUrl + " but requested " + url);
      }

      // logger.info("Folder request to : " + url);
      String relativePath = url.substring(baseUrl.length()).trim();

      // Remove and eventually existing / at the front
      if (relativePath.startsWith("/")) {
         relativePath = relativePath.substring(1);
      }
      // logger.info("relative path : " + relativePath);

      if (relativePath.length() == 0) {
         String redirectFile = getFilenameForRootRequests();
         if (redirectFile == null) {
            throw new NotExistentException(ErrorMessageRegistry.FOLDER_SERVER_NOT_EXISTENT_FILE);
         }

         final HttpHeaders responseHeaders = new HttpHeaders();
         try {
            responseHeaders.setLocation(new URI(baseUrl + "/" + redirectFile));
         } catch (URISyntaxException e) {
            throw new InternalServerException(
                  ErrorMessageRegistry.INTERAL_FOLDER_SERVER_REDIRECT_ERROR + " " + baseUrl + "/" + redirectFile);
         }
         return new ResponseEntity<>(responseHeaders, HttpStatus.PERMANENT_REDIRECT);
      }

      String webClientFolder = baseFolder;
      if (!webClientFolder.endsWith("/")) {
         webClientFolder = webClientFolder + "/";
      }

      File fileToServe = getFile(webClientFolder, relativePath);

      if (!fileToServe.exists() || !fileToServe.canRead()) {
         throw new NotExistentException(ErrorMessageRegistry.FOLDER_SERVER_NOT_EXISTENT_FILE);
      }

      if (!fileToServe.isFile()) {
         throw new MethodNotAllowedException(ErrorMessageRegistry.FOLDER_SERVER_NOT_A_FILE);
      }

      // security enhancement, do not allow access to anything outside base folder (via symlinks and so
      // on)
      checkNoAccessOutsideBaseFolder(fileToServe, baseFolder);

      String fileContent;
      try {
         fileContent = new String(Files.readAllBytes(fileToServe.toPath()), "UTF-8");
      } catch (IOException e) {
         throw new InternalServerException(ErrorMessageRegistry.FOLDER_SERVER_FILE_NOT_READBLE);
      }

      final HttpHeaders responseHeaders = new HttpHeaders();

      String contentType = getContentTypeString(fileToServe);
      if (contentType != null) {
         responseHeaders.set("Content-Type", contentType);
      }

      responseHeaders.setAllow(new HashSet<HttpMethod>(
            Arrays.asList(new HttpMethod[] {HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS})));

      return new ResponseEntity<>(fileContent, responseHeaders, HttpStatus.OK);
   }

   /**
    * Returns the file to read
    * 
    * @param  folder
    *                      The folder
    * @param  relativePath
    *                      The relative path
    * @return              The file
    */
   protected File getFile(String folder, String relativePath) {
      return new File(folder + relativePath);
   }

   /**
    * Returns the file to serve if requests to the root url are received Return null if not redirecting should occur
    * 
    * @return The file to serve on root requests, may be null
    */
   protected abstract String getFilenameForRootRequests();

   /**
    * Check whether access is allowed. Allowed means no symlink to anything outside of baseFolder
    * 
    * @param fileToServe
    *                    The file to server
    * @param baseFolder
    *                    The base folder from which to server
    */
   private void checkNoAccessOutsideBaseFolder(File fileToServe, String baseFolder) {
      Path basePath = new File(baseFolder).toPath();
      Path filePath = fileToServe.toPath();

      try {
         if (Files.isSameFile(basePath, filePath)) {
            return; // That is ok
         }
         if (!filePath.toAbsolutePath().toString().startsWith(basePath.toAbsolutePath().toString())) {
            // System.err.println(basePath.toAbsolutePath());
            // System.err.println(filePath.toAbsolutePath());
            throw new InternalServerException("Path not allowed");
         }
         if (Files.isSymbolicLink(filePath)) {
            Path link = Files.readSymbolicLink(filePath);
            if (!link.toAbsolutePath().toString().startsWith(basePath.toAbsolutePath().toString())) {
               // System.err.println(basePath.toAbsolutePath());
               // System.err.println(filePath.toAbsolutePath());
               throw new InternalServerException("Links not allowed");
            }
         }
      } catch (IOException e) {
         throw new InternalServerException("Error checking rights : " + e.getMessage());
      }
   }

   /**
    * Gets the mimetype for a given file
    * 
    * @param  file
    *              The file
    * @return      The string to use in Content-Type headers
    */
   private String getContentTypeString(File file) {
      return getContentTypeString(file.getName());
   }
}
