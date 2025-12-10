package edu.kit.scc.dem.wapsrv.controller;

import java.net.URI;
import java.net.URISyntaxException;
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
import edu.kit.scc.dem.wapsrv.exceptions.HttpHeaderException;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalHttpParameterException;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.AnnotationList;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import edu.kit.scc.dem.wapsrv.model.formats.ContentNegotiator;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.Formatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdFormatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.service.AnnotationService;

/**
 * The WAP controller used for requests to annotations
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@RestController
@RequestMapping(WapServerConfig.WAP_ENDPOINT)
public class AnnotationController extends WapController {
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
   * The annotation service to use for the requests
   */
  @Autowired
  private AnnotationService annotationService;

  @Override
  protected boolean isValidServiceFormat(Format format){
    return annotationService.isValidInputFormat(format);
  }

  /**
   * Creates a {@link ResponseEntity} to use for GET, HEAD and OPTIONS requests
   *
   * @param request The request to proceed
   * @param headers The headers used for the request
   * @return The response to send to the client
   * @throws WapException in case any error occurs
   */
  private ResponseEntity<?> getHeadOptionsAnnotation(final HttpServletRequest request, final HttpHeaders headers)
          throws WapException{
    final String httpMethod = request.getMethod();
    logger.info(httpMethod + " Annotation");
    final boolean isOptionsRequest = isOptionsRequest(httpMethod);
    // We may get requests that have params ==> return error
    if(!request.getParameterMap().isEmpty()){
      throw new IllegalHttpParameterException(ErrorMessageRegistry.ANNOTATION_NO_PARAMETERS_ALLOWED);
    }
    // Determine the format to use in the response-body
    final ContentNegotiator contentNegotiator
            = getContentNegotiator(request.getHeader("Accept"), Type.ANNOTATION, profileRegistry, formatRegistry);
    final Formatter formatter = contentNegotiator.getFormatter();
    final String annotationIri = extractIri(request);
    final Annotation annotation = annotationService.getAnnotation(annotationIri);
    // If something went wrong fetching the annotation, the code here is never reached, an Exception has
    // been thrown
    // Create Headers for the response
    final HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setAllow(AnnotationConstants.ALLOWED_METHODS);
    for(String link : AnnotationConstants.LINK_HEADER){
      responseHeaders.add("Link", link);
    }
    // As defined in "Pflichtheft"
    responseHeaders.setVary(AnnotationConstants.VARY_LIST);
    if(isOptionsRequest){
      // According to protocol, ETag is not used in OPTIONS Requests
      // No body ==> no ContentType header
      return new ResponseEntity<>(responseHeaders,
              HttpStatus.valueOf(AnnotationConstants.GET_ANNOTATION_SUCCESS_CODE));
    } else{
      // determine these values only when needed, this speeds up OPTIONS
      // HEAD request do need them too (for correct content length) and
      // by spec as they should create the same answer as get without the body
      final String etag = annotation.getEtagQuoted();
      responseHeaders.setETag(etag);
      responseHeaders.set(HttpHeaders.CONTENT_TYPE, formatter.getContentType());
      final String responseBody = formatter.format(annotation);
      // Attention : spring(or jetty) does not add the body to head requests automatically
      // but they need it to calculate corrects length. Therefore add it (and ignore in tests)
      return new ResponseEntity<>(responseBody, responseHeaders,
              HttpStatus.valueOf(AnnotationConstants.GET_ANNOTATION_SUCCESS_CODE));
    }
  }

  /**
   * This method implements the endpoint for HTTP GET requests to annotations
   *
   * @param request The request the client sent
   * @param headers The headers of the request
   * @return A response object to sent to the client
   * @throws WapException in case any error occurs
   */
  @RequestMapping(value = WapPathMatcher.ANNOTATION_PATTERN, method = RequestMethod.GET)
  public ResponseEntity<?> getAnnotation(HttpServletRequest request, @RequestHeader HttpHeaders headers)
          throws WapException{
    return getHeadOptionsAnnotation(request, headers);
  }

  /**
   * This method implements the endpoint for HTTP HEAD requests to annotations
   *
   * @param request The request the client sent
   * @param headers The headers of the request
   * @return A response object to sent to the client
   * @throws WapException in case any error occurs
   */
  @RequestMapping(value = WapPathMatcher.ANNOTATION_PATTERN, method = RequestMethod.HEAD)
  public ResponseEntity<?> headAnnotation(HttpServletRequest request, @RequestHeader HttpHeaders headers)
          throws WapException{
    // Not needed @PathVariable(WapPathMatcher.CONTAINER_ID) String containerId,
    // @PathVariable(WapPathMatcher.ANNOTATION_ID) String annotationId
    return getHeadOptionsAnnotation(request, headers);
  }

  /**
   * This method implements the endpoint for HTTP OPTIONS requests to
   * annotations
   *
   * @param request The request the client sent
   * @param headers The headers of the request
   * @return A response object to sent to the client
   * @throws WapException in case any error occurs
   */
  @RequestMapping(value = WapPathMatcher.ANNOTATION_PATTERN, method = RequestMethod.OPTIONS)
  public ResponseEntity<?> optionsAnnotation(HttpServletRequest request, @RequestHeader HttpHeaders headers)
          throws WapException{
    return getHeadOptionsAnnotation(request, headers);
  }

  /**
   * This method implements the endpoint for HTTP POST requests of annotations
   * to containers.
   *
   * @param request The request the client sent
   * @param headers The headers of the request
   * @param body The body of the request as a string
   * @return A response object to sent to the client
   * @throws WapException in case any error occurs
   */
  @RequestMapping(value = WapPathMatcher.CONTAINER_AND_PAGE_PATTERN, method = RequestMethod.POST)
  public ResponseEntity<?> postAnnotation(HttpServletRequest request, @RequestBody String body,
          @RequestHeader HttpHeaders headers) throws WapException{
    long t = System.currentTimeMillis();
    logger.info("POST Annotation");
    // We may get requests that have params ==> return error
    if(!request.getParameterMap().isEmpty()){
      throw new IllegalHttpParameterException(ErrorMessageRegistry.ANNOTATION_NO_PARAMETERS_ALLOWED);
    }
    // ContentType header is needed to know what lies in the body
    final String contentTypeHeader = headers.getFirst(HttpHeaders.CONTENT_TYPE);
    if(contentTypeHeader == null){
      throw new HttpHeaderException(ErrorMessageRegistry.ALL_CONTENT_TYPE_NEEDED_IN_POST);
    }
    final Format inputFormat
            = determineInputFormat(contentTypeHeader, Type.ANNOTATION, profileRegistry, formatRegistry);
    // If this code is reached, there have been no problems determining the format
    // and the format itself is valid for POST usage
    // Determine the format to use in the response-body
    ContentNegotiator contentNegotiator
            = getContentNegotiator(headers.getFirst("Accept"), Type.ANNOTATION, profileRegistry, formatRegistry);
    Formatter formatter = contentNegotiator.getFormatter();
    String containerIri = extractIri(request);
    AnnotationList annotationList = annotationService.postAnnotation(containerIri, body, inputFormat);
    // If something went wrong, the code here is never reached, an Exception has been thrown
    String iri = annotationList.getIriString();
    HttpHeaders responseHeaders = new HttpHeaders();
    try{
      responseHeaders.setLocation(new URI(iri));
    } catch(URISyntaxException e){
      throw new InternalServerException(ErrorMessageRegistry.INTERNAL_IRI_NOT_A_URI);
    }
    String etag = annotationList.getEtagQuoted();
    responseHeaders.setETag(etag);
    responseHeaders.setAllow(AnnotationConstants.ALLOWED_METHODS);
    responseHeaders.setVary(AnnotationConstants.VARY_LIST);
    for(String link : AnnotationConstants.LINK_HEADER){
      responseHeaders.add("Link", link);
    }
    // determine MultiPost or not and update responseBody + content type accordingly
    String responseBody = null;
    if(annotationList.size() > 1){
      // Replace formatter with JSON-LD formatter
      JsonLdFormatter jsonLdFormatter = new JsonLdFormatter();
      jsonLdFormatter.setProfileRegistry(profileRegistry);
      jsonLdFormatter.setAcceptPart(null, Type.ANNOTATION);
      responseHeaders.set(HttpHeaders.CONTENT_TYPE, jsonLdFormatter.getContentType());
      responseBody = jsonLdFormatter.format(annotationList);
    } else{
      responseHeaders.set(HttpHeaders.CONTENT_TYPE, formatter.getContentType());
      responseBody = formatter.format(annotationList);
    }
    return new ResponseEntity<>(responseBody, responseHeaders,
            HttpStatus.valueOf(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE));
  }

  /**
   * This method implements the endpoint for HTTP PUT requests of annotations
   *
   * @param request The request the client sent
   * @param headers The headers of the request
   * @param body The body of the request as a string
   * @return A response object to sent to the client
   * @throws WapException in case any error occurs
   */
  @RequestMapping(value = WapPathMatcher.ANNOTATION_PATTERN, method = RequestMethod.PUT)
  public ResponseEntity<?> putAnnotation(HttpServletRequest request, @RequestBody String body,
          @RequestHeader HttpHeaders headers) throws WapException{
    logger.info("PUT Annotation");
    // We may get requests that have params ==> return error
    if(!request.getParameterMap().isEmpty()){
      throw new IllegalHttpParameterException(ErrorMessageRegistry.ANNOTATION_NO_PARAMETERS_ALLOWED);
    }
    // ContentType header is needed to know that lies in the body
    final String contentTypeHeader = headers.getFirst(HttpHeaders.CONTENT_TYPE);
    if(contentTypeHeader == null){
      throw new HttpHeaderException(ErrorMessageRegistry.ALL_CONTENT_TYPE_NEEDED_IN_PUT);
    }
    // ETag needed in PUT requests
    String etag = headers.getFirst(HttpHeaders.IF_MATCH);
    if(etag == null){
      throw new HttpHeaderException(ErrorMessageRegistry.ALL_ETAG_NEEDED_FOR_PUT);
    }
    if(!EtagFactory.isValidEtag(etag)){
      throw new HttpHeaderException(ErrorMessageRegistry.ALL_INVALID_ETAG_FORMAT);
    }
    final Format inputFormat
            = determineInputFormat(contentTypeHeader, Type.ANNOTATION, profileRegistry, formatRegistry);
    // If this code is reached, there have been no problems determining the format
    // and the format itself is valid for PUT usage
    // Determine the format to use in the response-body
    ContentNegotiator contentNegotiator
            = getContentNegotiator(headers.getFirst("Accept"), Type.ANNOTATION, profileRegistry, formatRegistry);
    Formatter formatter = contentNegotiator.getFormatter();
    final String iri = extractIri(request);
    final Annotation annotation = annotationService.putAnnotation(iri, stripQuotes(etag), body, inputFormat);
    // If-Match header contains the Etag value
    // If something went wrong, the code here is never reached, an Exception has
    // been thrown
    HttpHeaders responseHeaders = new HttpHeaders();
    etag = annotation.getEtagQuoted();
    responseHeaders.setETag(etag);
    responseHeaders.setAllow(AnnotationConstants.ALLOWED_METHODS);
    responseHeaders.set(HttpHeaders.CONTENT_TYPE, formatter.getContentType());
    for(String link : AnnotationConstants.LINK_HEADER){
      responseHeaders.add("Link", link);
    }
    responseHeaders.setVary(AnnotationConstants.VARY_LIST);
    String responseBody = formatter.format(annotation);
    return new ResponseEntity<>(responseBody, responseHeaders,
            HttpStatus.valueOf(AnnotationConstants.PUT_ANNOTATION_SUCCESS_CODE));
  }

  /**
   * This method implements the endpoint for HTTP DELETE requests of annotations
   *
   * @param request The request the client sent
   * @param headers The headers of the request
   * @return A response object to sent to the client
   * @throws WapException in case any error occurs
   */
  @RequestMapping(value = WapPathMatcher.ANNOTATION_PATTERN, method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteAnnotation(HttpServletRequest request, @RequestHeader HttpHeaders headers)
          throws WapException{
    logger.info("DELETE Annotation");
    // may get requests that have params ==> return error
    if(!request.getParameterMap().isEmpty()){
      throw new IllegalHttpParameterException(ErrorMessageRegistry.ANNOTATION_NO_PARAMETERS_ALLOWED);
    }
    // ETag needed in DELETE requests
    String etag = headers.getFirst(HttpHeaders.IF_MATCH);
    if(etag == null){
      throw new HttpHeaderException(ErrorMessageRegistry.ALL_ETAG_NEEDED_FOR_DELETE);
    }
    if(!EtagFactory.isValidEtag(etag)){
      throw new HttpHeaderException(ErrorMessageRegistry.ALL_INVALID_ETAG_FORMAT);
    }
    String iri = extractIri(request);
    annotationService.deleteAnnotation(iri, stripQuotes(etag));
    // If-Match header contains the Etag value
    // If something went wrong, the code here is never reached, an Exception has been thrown.
    return new ResponseEntity<>(HttpStatus.valueOf(AnnotationConstants.DELETE_ANNOTATION_SUCCESS_CODE));
  }
}
