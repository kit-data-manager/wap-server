package edu.kit.scc.dem.wapsrv.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;

/**
 * The Controller used to serve the JavaDoc
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@RestController
@RequestMapping(JavadocController.PATH)
public class JavadocController extends SimpleFolderServerController {
   /**
    * The endpoint for JavaDoc requests
    */
   public static final String PATH = "/javadoc";

   /**
    * Creates the web client controller instance
    * 
    * @param wapServerConfig
    *                        The config to use
    */
   @Autowired
   protected JavadocController(WapServerConfig wapServerConfig) {
      super(wapServerConfig.getBaseUrl() + PATH, wapServerConfig.getJavaDocFolder());
   }

   /**
    * This method implements the endpoint for HTTP GET requests to the JavaDoc
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(value = "**", method = {RequestMethod.GET})
   public ResponseEntity<?> getFileResponse(HttpServletRequest request, @RequestHeader HttpHeaders headers)
         throws WapException {
      return super.getFileResponse(request, headers);
   }

   @Override
   protected String getFilenameForRootRequests() {
      return "index.html";
   }
}
