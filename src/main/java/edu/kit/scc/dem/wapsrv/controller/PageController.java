package edu.kit.scc.dem.wapsrv.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalHttpParameterException;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalPageIriException;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.ContainerPreference;
import edu.kit.scc.dem.wapsrv.model.Page;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import edu.kit.scc.dem.wapsrv.model.formats.ContentNegotiator;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.Formatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.service.ContainerService;

/**
 * The WAP Controller used for requests to Pages
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@RestController
@RequestMapping(WapServerConfig.WAP_ENDPOINT)
public class PageController extends WapController {
   /**
    * The JSON-LD profile registry
    */
   @Autowired
   JsonLdProfileRegistry profileRegistry;
   /**
    * The format registry
    */
   @Autowired
   FormatRegistry formatRegistry;
   /**
    * The logger to use
    */
   private final Logger logger = LoggerFactory.getLogger(this.getClass());
   /**
    * The container service
    */
   @Autowired
   private ContainerService containerService;

   @Override
   protected boolean isValidServiceFormat(Format format) {
      // Not write to a page
      return false;
   }

   /**
    * Called by all endpoints to generate the response
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @param  iris
    *                      The value of the iris query parameter
    * @param  pageNr
    *                      The value of the page query parameter
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   private ResponseEntity<?> getHeadOptionsPage(HttpServletRequest request, HttpHeaders headers, int iris, int pageNr)
         throws WapException {
      final String httpMethod = request.getMethod();
      logger.info(httpMethod + " Page");
      final boolean isOptionsRequest = isOptionsRequest(httpMethod);
      // We may get requests that have more the page and iris params ==> return error
      if (request.getParameterMap().size() > 2) {
         throw new IllegalHttpParameterException(ErrorMessageRegistry.PAGE_INVALID_GIVEN_PARAMETERS);
      }
      // Check for valid parameter values
      if ((iris != ContainerPreference.PREFER_CONTAINED_IRIS
            && iris != ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS) || pageNr < 0) {
         throw new IllegalPageIriException();
      }
      final ContentNegotiator contentNegotiator
            = getContentNegotiator(request.getHeader("Accept"), Type.PAGE, profileRegistry, formatRegistry);
      final Formatter formatter = contentNegotiator.getFormatter();
      final String containerIri = extractIri(request);
      final int containerPreference = iris;
      final Page page = containerService.getPage(containerIri, containerPreference, pageNr);
      // If something went wrong fetching the page, the code here is never reached, an
      // Exception has been thrown
      // Create Headers for the response
      final HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setAllow(PageConstants.ALLOWED_METHODS);
      responseHeaders.setVary(PageConstants.VARY_LIST);
      if (isOptionsRequest) {
         // According to protocol, ETag is not used in OPTIONS Requests
         // No body ==> no ContentType header
         return new ResponseEntity<>(responseHeaders, HttpStatus.valueOf(PageConstants.GET_PAGE_SUCCESS_CODE));
      } else {
         // determine these values only when needed, this speeds up OPTIONS
         // HEAD request do need them too (for correct content length) and
         // by spec as they should create the same answer as get without the body
         responseHeaders.set(HttpHeaders.CONTENT_TYPE, formatter.getContentType());
         final String responseBody = formatter.format(page);
         // Attention : spring(or jetty) does not add the body to head requests automatically
         // but they need it to calculate corrects length. Therefore add it (and ignore in tests)
         return new ResponseEntity<>(responseBody, responseHeaders,
               HttpStatus.valueOf(PageConstants.GET_PAGE_SUCCESS_CODE));
      }
   }

   /**
    * This method implements the endpoint for HTTP GET requests to pages
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @param  iris
    *                      The value of the IRIs query parameter
    * @param  pageNr
    *                      The value of the page query parameter
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(value = WapPathMatcher.CONTAINER_AND_PAGE_PATTERN, params = {"iris", "page"},
         method = {RequestMethod.GET})
   public ResponseEntity<?> getPage(HttpServletRequest request, @RequestHeader HttpHeaders headers,
         @RequestParam("iris") int iris, @RequestParam("page") int pageNr) throws WapException {
      return getHeadOptionsPage(request, headers, iris, pageNr);
   }

   /**
    * This method implements the endpoint for HTTP HEAD requests to pages
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @param  iris
    *                      The value of the IRIs query parameter
    * @param  pageNr
    *                      The value of the page query parameter
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(value = WapPathMatcher.CONTAINER_AND_PAGE_PATTERN, params = {"iris", "page"},
         method = {RequestMethod.HEAD})
   public ResponseEntity<?> headPage(HttpServletRequest request, @RequestHeader HttpHeaders headers,
         @RequestParam("iris") int iris, @RequestParam("page") int pageNr) throws WapException {
      return getHeadOptionsPage(request, headers, iris, pageNr);
   }

   /**
    * This method implements the endpoint for HTTP OPTIONS requests to pages
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @param  iris
    *                      The value of the IRIs query parameter
    * @param  pageNr
    *                      The value of the page query parameter
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(value = WapPathMatcher.CONTAINER_AND_PAGE_PATTERN, params = {"iris", "page"},
         method = {RequestMethod.OPTIONS})
   public ResponseEntity<?> optionsPage(HttpServletRequest request, @RequestHeader HttpHeaders headers,
         @RequestParam("iris") int iris, @RequestParam("page") int pageNr) throws WapException {
      return getHeadOptionsPage(request, headers, iris, pageNr);
   }
}
