package edu.kit.scc.dem.wapsrv.controller;

import java.util.Hashtable;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jetty.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalHttpParameterException;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidRequestException;
import edu.kit.scc.dem.wapsrv.exceptions.MethodNotAllowedException;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;

/**
 * This controller adds the catch all feature to the server which answers all request that not mapped correctly
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@RestController
@RequestMapping("/")
public class CatchAllController extends BasicController {
   @Autowired
   private WapServerConfig wapServerConfig;

   /**
    * This method implements the endpoint that catches all otherwise not mapped requests to create meaningful error
    * messages
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(value = "**")
   public ResponseEntity<?> catchallRequest(HttpServletRequest request, @RequestHeader HttpHeaders headers)
         throws WapException {
      // The basic idea is that all "real" controllers just intercept those messages
      // that truly fit them and check their parameters, headers and so on there.
      // All request that do not exactly fit one request and cannot be mapped, land here
      final String requestUrl = extractIri(request);
      final HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
      // 1. Process requests that target the WAP REST service
      if (requestUrl.startsWith(wapServerConfig.getBaseUrl() + WapServerConfig.WAP_ENDPOINT_WITHOUT_TRAILING_SLASH)) {
         processInvalidWapRequest(request, headers, requestUrl, httpMethod);
         // This method sould never return
         throw new InternalServerException(ErrorMessageRegistry.INTERAL_WAP_ERROR_PROCESSING_NOT_COMPLETE);
         // 2. Process requests that target the web client
      } else if (requestUrl.startsWith(wapServerConfig.getBaseUrl() + WebClientController.PATH)) {
         if (httpMethod == HttpMethod.GET) {
            throw new InternalServerException(ErrorMessageRegistry.WEBAPP_GET_IN_CATCHALL + " : " + requestUrl);
         } else {
            throw new MethodNotAllowedException(ErrorMessageRegistry.WEBAPP_ONLY_GET_ALLOWED);
         }
         // 3. Process requests that target the integrated javadoc
      } else if (requestUrl.startsWith(wapServerConfig.getBaseUrl() + JavadocController.PATH)) {
         if (httpMethod == HttpMethod.GET) {
            throw new InternalServerException(ErrorMessageRegistry.JAVADOC_GET_IN_CATCHALL + " : " + requestUrl);
         } else {
            throw new MethodNotAllowedException(ErrorMessageRegistry.JAVADOC_ONLY_GET_ALLOWED);
         }
         // 4. Answer all non fitting GET requests with a basic information page
      } else if (httpMethod == HttpMethod.GET) {
         return createInformationMessage();
         // 5. In all other cases, throw a method not allowed exception
      } else {
         throw new MethodNotAllowedException(ErrorMessageRegistry.ALL_UNALLOWED_METHOD);
      }
   }

   /**
    * Returns a basic information page showing links to the supported URLs of the server
    * 
    * @return A basic information page
    */
   private ResponseEntity<?> createInformationMessage() {
      final String wapUrl = wapServerConfig.getBaseUrl() + WapServerConfig.WAP_ENDPOINT;
      final String javadocUrl = wapServerConfig.getBaseUrl() + "/javadoc";
      final String webappUrl = wapServerConfig.getBaseUrl() + "/webapp";
      StringBuilder builder = new StringBuilder();
      builder.append("<html>");
      builder.append("A GET request to the server's base url has been received. ");
      builder.append("To interact with this server, there are a few endpoint you can send requests to :<br>");
      builder.append("<ul>");
      builder.append("<li><a href=\"" + wapUrl + "\">" + wapUrl + "</a>"
            + " for requests to the main functionality, the Web Annotation Protocol Service</li>");
      builder.append("<li><a href=\"" + webappUrl + "\">" + webappUrl + "</a>"
            + " for requests to the Web Annotation Protocol Service Client</li>");
      builder.append("<li><a href=\"" + javadocUrl + "\">" + javadocUrl + "</a>"
            + " for requests to the the integrated javadoc</li>");
      builder.append("</ul>");
      builder.append("</html>");
      return new ResponseEntity<String>(builder.toString(), HttpStatus.OK);
   }

   private void processInvalidWapRequest(final HttpServletRequest request, final HttpHeaders headers,
         final String requestUrl, final HttpMethod httpMethod) {
      final Map<String, String[]> params
            = request.getParameterMap() == null ? new Hashtable<String, String[]>() : request.getParameterMap();
      switch (httpMethod) {
      case POST:
         processWapPost(request, headers, requestUrl, params);
         break;
      case PUT:
         processWapPut(request, headers, requestUrl, params);
         break;
      default:
         throw new MethodNotAllowedException(ErrorMessageRegistry.ALL_UNALLOWED_METHOD);
      }
   }

   private void processWapPut(HttpServletRequest request, HttpHeaders headers, String requestUrl,
         Map<String, String[]> params) {
      final boolean toAnnotationIri = !requestUrl.endsWith("/");
      if (!toAnnotationIri) {
         // We may have put to a container or a page ?. Distinguish via params
         // And since in put no params are allowed at all, this is easy
         if (!params.isEmpty()) {
            throw new IllegalHttpParameterException(ErrorMessageRegistry.ALL_NO_PARAMETERS_IN_PUT);
         }
         // until implemented, PUT to container is not allowed
         throw new MethodNotAllowedException(ErrorMessageRegistry.CONTAINER_PUT_NOT_IMPLEMENTED);
      } else {
         // PUT to an annotation IRI should not land here
         throw new InternalServerException(ErrorMessageRegistry.PUT_ANNOTATION_SHOULD_NOT_LAND_HERE);
      }
   }

   private void processWapPost(HttpServletRequest request, HttpHeaders headers, String requestUrl,
         Map<String, String[]> params) {
      final boolean toAnnotationIri = !requestUrl.endsWith("/");
      if (!toAnnotationIri) {
         // We may have post to a container or a page ?. Distinguish via params
         // And since in post no params are allowed at all, this is easy
         if (!params.isEmpty()) {
            throw new IllegalHttpParameterException(ErrorMessageRegistry.ALL_NO_PARAMETERS_IN_POST);
         }
         // here is the problem : what was the intention ? post anno or post container ?
         if (headers.get("Slug") != null) {
            throw new InvalidRequestException(ErrorMessageRegistry.CONTAINER_LINK_NEEDED_IN_POST);
         }
         // if we have a link header, we assume container, otherwise anno
         if (headers.get(HttpHeaders.LINK) == null) {
            // Anno : as implemeted so far, this case can never happen since the anno controller
            // always gets those without link. This is just a fallback
            throw new InvalidRequestException(ErrorMessageRegistry.ANNOTATION_POST_INVALID);
         } else {
            // container : as implemeted so far, this case can never happen since the container
            // controller always gets those with link. This is just a fallback
            throw new InvalidRequestException(ErrorMessageRegistry.CONTAINER_POST_INVALID);
         }
      } else {
         // POST to an annotation IRI is never allowed
         throw new MethodNotAllowedException(ErrorMessageRegistry.ANNOTATION_POST_TO_NOT_ALLOWED);
      }
   }
}
