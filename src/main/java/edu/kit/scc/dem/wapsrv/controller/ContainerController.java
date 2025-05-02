package edu.kit.scc.dem.wapsrv.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.app.EtagFactory;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.*;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.ContainerPreference;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import edu.kit.scc.dem.wapsrv.model.formats.*;
import edu.kit.scc.dem.wapsrv.service.ContainerService;

/**
 * The WAP Controller used for requests Containers
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@RestController
public class ContainerController extends WapController {
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
   @Autowired
   private ContainerService containerService;
   @Autowired
   private WapServerConfig wapServerConfig;

   @Override
   protected boolean isValidServiceFormat(Format format) {
      return containerService.isValidInputFormat(format);
   }

   /**
    * Returns the link header with exactly one white space between ";" and "rel=*"
    * <p>
    * The header should be : &lt;http://www.w3.org/ns/ldp#BasicContainer&gt;;* rel="type"
    * 
    * @param  header
    *                The header
    * @return        The normalized header
    */
   private String normalizeLinkHeader(String header) {
      String normalizedHeader = header.trim();
      // Check if here "if" would suffice. Update: If would not suffice, the problem is that if 4 spaces are next to
      // each other,
      // the implementation makes one from the first 2 and one from the second 2 and produces a result that has 2 again
      while (normalizedHeader.indexOf("  ") != -1) {
         normalizedHeader = normalizedHeader.replaceAll(Pattern.quote("  "), " ");
      }
      return normalizedHeader.replaceFirst(Pattern.quote(";rel"), "; rel");
   }

   /**
    * This method performs HTTP GET/HEAD/OPTIONS requests to all containers
    * 
    * @param  request
    *                         The request the client sent
    * @param  headers
    *                         The headers of the request
    * @param  isRootContainer
    *                         If the request was sent to the root container, this is true
    * @return                 A response object to sent to the client
    * @throws WapException
    *                         in case any error occurs
    */
   private ResponseEntity<?> getHeadOptionsContainer(HttpServletRequest request, HttpHeaders headers,
         boolean isRootContainer) throws WapException {
      final String httpMethod = request.getMethod();
      final boolean isOptionsRequest = isOptionsRequest(httpMethod);
      logger.info(httpMethod + (isRootContainer ? " root " : " ") + "container");
      // this test now comes before the parameter test so a given iris=x may override the preference via Prefer
      final String preferHeader = headers.getFirst("Prefer");
      Set<Integer> preferences = null;
      if (preferHeader == null) {
         preferences = new HashSet<Integer>();
      } else {
         preferences = ContainerPreference.toSet(preferHeader);
         if (preferences == null) {
            throw new HttpHeaderException(ErrorMessageRegistry.CONTAINER_INVALID_PREFERENCES);
         }
         // If both are contained, prefer IRIs only and prefer full annotations. This does not work.
         if (ContainerPreference.isPreferContainedDescriptions(preferences)
               && ContainerPreference.isPreferContainedIRIs(preferences)) {
            throw new HttpHeaderException(ErrorMessageRegistry.CONTAINER_UNALLOWED_PREFERENCE_COMBINATION);
         }
      }
      // We may get requests that have params ==> return error if it is not the iris param and if it is
      if (!request.getParameterMap().isEmpty()) {
         if (request.getParameter("iris") != null && request.getParameterMap().size() == 1) {
            try {
               int iris = Integer.parseInt(request.getParameter("iris"));
               if (iris == ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS) {
                  // Valid iris param , now override preferences if necessary because of the behavior of sets, we can
                  // just remove and add
                  preferences.add(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS);
                  preferences.remove(ContainerPreference.PREFER_CONTAINED_IRIS);
               } else if (iris == ContainerPreference.PREFER_CONTAINED_IRIS) {
                  // Valid iris param , now override preferences if necessary because of the behavior of sets, we can
                  // just remove and add
                  preferences.add(ContainerPreference.PREFER_CONTAINED_IRIS);
                  preferences.remove(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS);
               } else {
                  throw new IllegalHttpParameterException(
                        ErrorMessageRegistry.CONTAINER_GET_HEAD_OPTIONS_NO_PARAMETERS_ALLOWED_BUT_IRIS);
               }
            } catch (NumberFormatException ex) {
               // Tell the client only iris=0 or iris=1 is allowed
               throw new IllegalHttpParameterException(
                     ErrorMessageRegistry.CONTAINER_GET_HEAD_OPTIONS_NO_PARAMETERS_ALLOWED_BUT_IRIS);
            }
         } else if (request.getParameter("page") != null) {
            // If we get requests that contain iris and page, they are mapped to the page controller at this
            // specific case we may therefore never have an iris parameter, but a apge
            throw new InvalidRequestException(ErrorMessageRegistry.PAGE_WITH_PAGE_BUT_IRIS_MISSING);
         } else {
            // in all other cases we assume invalid container request
            throw new IllegalHttpParameterException(
                  ErrorMessageRegistry.CONTAINER_GET_HEAD_OPTIONS_NO_PARAMETERS_ALLOWED_BUT_IRIS);
         }
      }
      final ContentNegotiator contentNegotiator
            = getContentNegotiator(headers.getFirst("Accept"), Type.CONTAINER, profileRegistry, formatRegistry);
      final Formatter formatter = contentNegotiator.getFormatter();
      final String iri = extractIri(request);
      Container container = containerService.getContainer(iri, preferences);
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.add("Link", ContainerConstants.LINK_TYPE);
      responseHeaders.add("Link", ContainerConstants.LINK_ANNOTATION_PROTOCOL);
      responseHeaders.setVary(ContainerConstants.VARY_LIST);
      // 4.2 end of section : if not redirecting client, the server MUST include Content-Location header
      String containerIri = container.getIriString();
      responseHeaders.set("Content-Location", containerIri);
      responseHeaders
            .setAllow(isRootContainer ? ContainerConstants.ROOT_ALLOWED_METHODS : ContainerConstants.ALLOWED_METHODS);
      if (isOptionsRequest) {
         // According to protocol, ETag is not used in OPTIONS Requests. No body ==> no ContentType header
         return new ResponseEntity<>(responseHeaders,
               HttpStatus.valueOf(ContainerConstants.GET_CONTAINER_SUCCESS_CODE));
      } else {
         responseHeaders.setETag(container.getEtagQuoted());
         responseHeaders.set(HttpHeaders.CONTENT_TYPE, formatter.getContentType());
         String responseBody = "";
         try{
         responseBody = formatter.format(container);
         }catch(Exception e){
             e.printStackTrace();
         }
         return new ResponseEntity<>(responseBody, responseHeaders,
               HttpStatus.valueOf(ContainerConstants.GET_CONTAINER_SUCCESS_CODE));
      }
   }

   /**
    * This method performs HTTP POST requests of new containers
    * 
    * @param  request
    *                         The request the client sent
    * @param  headers
    *                         The headers of the request
    * @param  body
    *                         The serialized container in the body of the request
    * @param  isRootContainer
    *                         If the request was sent to the root container, this is true
    * @return                 A response object to sent to the client
    * @throws WapException
    *                         in case any error occurs
    */
   private ResponseEntity<?> postBothContainers(HttpServletRequest request, HttpHeaders headers, String body,
         boolean isRootContainer) {
      logger.info("POST " + (isRootContainer ? "root " : "") + "container");
      // We may get requests that have params ==> return error
      if (!request.getParameterMap().isEmpty()) {
         throw new IllegalHttpParameterException(ErrorMessageRegistry.CONTAINER_POST_DELETE_NO_PARAMETERS_ALLOWED);
      }
      // Slug and Link exist, or Spring would not have called the two post endpoint methods
      // does not have a constant there
      final String name = headers.getFirst("Slug");
      if (name == null) {
         if (wapServerConfig.isSlugMandatoryInContainerPosts()) {
            throw new InvalidRequestException(ErrorMessageRegistry.CONTAINER_SLUG_IS_SET_MANDATORY);
         } else {
            // Nothing done here, the service recognizes null as ==> create uuid
         }
      }
      if (name != null && name.trim().length() == 0) {
         throw new InvalidRequestException(ErrorMessageRegistry.CONTAINER_NO_EMPTY_SLUG_ALLOWED);
      }
      // we normalize the link header so there is exactly one space character between; and rel
      final String link = normalizeLinkHeader(headers.getFirst(HttpHeaders.LINK));
      if (!ContainerConstants.LINK_TYPE.equals(link)) {
         throw new InvalidContainerException(ErrorMessageRegistry.CONTAINER_ONLY_BASIC_CONTAINER_ALLOWED);
      }
      // ContentType header is needed to know that lies in the body
      final String contentTypeHeader = headers.getFirst(HttpHeaders.CONTENT_TYPE);
      if (contentTypeHeader == null) {
         throw new HttpHeaderException(ErrorMessageRegistry.ALL_CONTENT_TYPE_NEEDED_IN_POST);
      }
      final Format inputFormat
            = determineInputFormat(contentTypeHeader, Type.CONTAINER, profileRegistry, formatRegistry);
      // If this code is reached, there have been no problems determining the format and the format itself
      // is valid for POST usage.
      String baseContainerIri = extractIri(request);
      Container container = containerService.postContainer(baseContainerIri, name, body, inputFormat);
      HttpHeaders responseHeaders = new HttpHeaders();
      try {
         responseHeaders.setLocation(new URI(container.getIriString()));
      } catch (URISyntaxException e) {
         throw new InternalServerException(ErrorMessageRegistry.INTERNAL_IRI_NOT_A_URI + container.getIriString());
      }
      responseHeaders.setETag(container.getEtagQuoted());
      responseHeaders.set("Link", ContainerConstants.LINK_TYPE);
      responseHeaders.add("Link", ContainerConstants.LINK_ANNOTATION_PROTOCOL);
      responseHeaders.setVary(AnnotationConstants.VARY_LIST);
      responseHeaders
            .setAllow(isRootContainer ? ContainerConstants.ROOT_ALLOWED_METHODS : ContainerConstants.ALLOWED_METHODS);
      return new ResponseEntity<>(responseHeaders, HttpStatus.valueOf(ContainerConstants.POST_CONTAINER_SUCCESS_CODE));
   }

   /**
    * This method implements the endpoint for HTTP GET requests to non-root containers
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(
         value = WapServerConfig.WAP_ENDPOINT_WITHOUT_TRAILING_SLASH + WapPathMatcher.CONTAINER_AND_PAGE_PATTERN,
         method = RequestMethod.GET)
   public ResponseEntity<?> getContainer(HttpServletRequest request, @RequestHeader HttpHeaders headers)
         throws WapException {
      return getHeadOptionsContainer(request, headers, false);
   }

   /**
    * This method implements the endpoint for HTTP HEAD requests to non-root containers
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(
         value = WapServerConfig.WAP_ENDPOINT_WITHOUT_TRAILING_SLASH + WapPathMatcher.CONTAINER_AND_PAGE_PATTERN,
         method = RequestMethod.HEAD)
   public ResponseEntity<?> headContainer(HttpServletRequest request, @RequestHeader HttpHeaders headers)
         throws WapException {
      // not needed @PathVariable(WapPathMatcher.CONTAINER_ID) String containerId,
      return getHeadOptionsContainer(request, headers, false);
   }

   /**
    * This method implements the endpoint for HTTP OPTIONS requests to non-root containers
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(
         value = WapServerConfig.WAP_ENDPOINT_WITHOUT_TRAILING_SLASH + WapPathMatcher.CONTAINER_AND_PAGE_PATTERN,
         method = RequestMethod.OPTIONS)
   public ResponseEntity<?> optionsContainer(HttpServletRequest request, @RequestHeader HttpHeaders headers)
         throws WapException {
      return getHeadOptionsContainer(request, headers, false);
   }

   /**
    * This method implements the endpoint for HTTP POST requests of new containers to non-root containers
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @param  body
    *                      The serialized container in the body of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(
         value = WapServerConfig.WAP_ENDPOINT_WITHOUT_TRAILING_SLASH + WapPathMatcher.CONTAINER_AND_PAGE_PATTERN,
         method = RequestMethod.POST, headers = {"Link"})
   public ResponseEntity<?> postContainer(HttpServletRequest request, @RequestBody String body,
         @RequestHeader HttpHeaders headers) throws WapException {
      return postBothContainers(request, headers, body, false);
   }

   /**
    * This method implements the endpoint for HTTP DELETE requests to non-root containers
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(
         value = WapServerConfig.WAP_ENDPOINT_WITHOUT_TRAILING_SLASH + WapPathMatcher.CONTAINER_AND_PAGE_PATTERN,
         method = RequestMethod.DELETE)
   public ResponseEntity<?> deleteContainer(HttpServletRequest request, @RequestHeader HttpHeaders headers)
         throws WapException {
      logger.info("DELETE Container");
      // We may get requests that have params ==> return error
      if (!request.getParameterMap().isEmpty()) {
         throw new IllegalHttpParameterException(ErrorMessageRegistry.CONTAINER_POST_DELETE_NO_PARAMETERS_ALLOWED);
      }
      // ETag needed in DELETE requests
      String etag = headers.getFirst(HttpHeaders.IF_MATCH);
      if (etag == null) {
         throw new HttpHeaderException(ErrorMessageRegistry.ALL_ETAG_NEEDED_FOR_DELETE);
      }
      if (!EtagFactory.isValidEtag(etag)) {
         throw new HttpHeaderException(ErrorMessageRegistry.ALL_INVALID_ETAG_FORMAT);
      }
      String iri = extractIri(request);
      // If Match header contains the Etag value.
      containerService.deleteContainer(iri, stripQuotes(etag));
      // If something went wrong, the code here is never reached, an Exception has been thrown.
      return new ResponseEntity<>(HttpStatus.valueOf(ContainerConstants.DELETE_CONTAINER_SUCCESS_CODE));
   }

   /**
    * This method implements the endpoint for HTTP GET requests to the root container
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(value = WapServerConfig.WAP_ENDPOINT, method = RequestMethod.GET)
   public ResponseEntity<?> getRootContainer(HttpServletRequest request, @RequestHeader HttpHeaders headers)
         throws WapException {
      return getHeadOptionsContainer(request, headers, true);
   }

   /**
    * This method implements the endpoint for HTTP HEAD requests to the root container
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(value = WapServerConfig.WAP_ENDPOINT, method = RequestMethod.HEAD)
   public ResponseEntity<?> headRootContainer(HttpServletRequest request, @RequestHeader HttpHeaders headers)
         throws WapException {
      return getHeadOptionsContainer(request, headers, true);
   }

   /**
    * This method implements the endpoint for HTTP OPTIONS requests to the root container
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(value = WapServerConfig.WAP_ENDPOINT, method = RequestMethod.OPTIONS)
   public ResponseEntity<?> optionsRootContainer(HttpServletRequest request, @RequestHeader HttpHeaders headers)
         throws WapException {
      return getHeadOptionsContainer(request, headers, true);
   }

   /**
    * This method implements the endpoint for HTTP POST requests of new containers to the root container (or misled
    * annotation POSTs)
    * 
    * @param  request
    *                      The request the client sent
    * @param  headers
    *                      The headers of the request
    * @param  body
    *                      The serialized container in the body of the request
    * @return              A response object to sent to the client
    * @throws WapException
    *                      in case any error occurs
    */
   @RequestMapping(value = WapServerConfig.WAP_ENDPOINT, method = RequestMethod.POST)
   public ResponseEntity<?> postRootContainer(HttpServletRequest request, @RequestBody String body,
         @RequestHeader HttpHeaders headers) throws WapException {
      boolean withLink = headers.getFirst("Link") != null;
      boolean withSlug = headers.getFirst("Slug") != null;
      if (withLink) {
         return postBothContainers(request, headers, body, true);
      } else if (withSlug) {
         // with slug and not link, we assume the client tried to post a container
         throw new InvalidRequestException(ErrorMessageRegistry.CONTAINER_LINK_NEEDED_IN_POST);
      } else {
         // no header given, we assume the client tries to post an annotation
         throw new MethodNotAllowedException(ErrorMessageRegistry.ANNOTATION_NO_POST_TO_ROOT_CONTAINER);
      }
   }
}
