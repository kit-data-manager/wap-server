package edu.kit.scc.dem.wapsrv.controller;

import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.exceptions.FormatNotAvailableException;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import edu.kit.scc.dem.wapsrv.model.formats.ContentNegotiator;
import edu.kit.scc.dem.wapsrv.model.formats.ContentTypeParser;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.Formatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.app.EtagFactory;

/**
 * Base class for all Wap Controllers where common behavior is implemented.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public abstract class WapController extends BasicController {
   /**
    * Checks if the given format is usable for PUT/POST requests to the controllers service. This may be different
    * services for different implementations.
    * 
    * @param  format
    *                The format to check
    * @return        true if supported, false otherwise
    */
   protected abstract boolean isValidServiceFormat(Format format);

   /**
    * Strips the quotes from the start and the end of a given ETag.
    * 
    * @param  quotedString
    *                      The quoted ETag
    * @return              The ETag without quotes
    */
   protected String stripQuotes(String quotedString) {
      // Only valid ETags land here, the exception should therefore never
      // been thrown.
      if (quotedString == null || !EtagFactory.isValidEtag(quotedString)) {
         throw new InternalServerException(
               "Invalid ETag used in WapController.stripQuotes(String), this should never happen");
      }
      return quotedString.substring(1, quotedString.length() - 1);
   }

   /**
    * Determines the format provided by the client in the body of the request.<br>
    * An exception is thrown if format is invalid or not usable in PUT/POST requests.
    * 
    * @param  contentTypeHeader
    *                           The value of the Content-Type header field
    * @param  type
    *                           Type of formattable object
    * @param  profileRegistry
    *                           The JSON-LD profile registry to use
    * @param  formatRegistry
    *                           The format registry
    * @return                   The format if it could be determined and is valid for usage in PUT/POST requests
    */
   protected Format determineInputFormat(String contentTypeHeader, Type type, JsonLdProfileRegistry profileRegistry,
         FormatRegistry formatRegistry) {
      if (contentTypeHeader == null) {
         // This code should never be reached, we ensure contentType!=null prior to call this method
         throw new InternalServerException(ErrorMessageRegistry.INTERNAL_CONTENT_TYPE_NULL_WHERE_IT_SHOULD_NOT_BE);
      }
      ContentTypeParser contentTypeParser = new ContentTypeParser(contentTypeHeader, type, formatRegistry);
      Formatter formatter = contentTypeParser.getFormatter();
      if (formatter == null) {
         throw new FormatException(ErrorMessageRegistry.ALL_UNKNOWN_INPUT_FORMAT + " : " + contentTypeHeader);
      }
      if (!formatter.isValid()) {
         throw new FormatException(ErrorMessageRegistry.ALL_INVALID_INPUT_FORMAT_HEADER + " : \"" + contentTypeHeader
               + "\" not a valid header for " + formatter.getFormatString());
      }
      if (!isValidServiceFormat(formatter.getFormat())) {
         throw new FormatNotAvailableException(
               ErrorMessageRegistry.ALL_UNALLOWED_INPUT_FORMAT + " : " + formatter.getFormatString());
      }
      return formatter.getFormat();
   }

   /**
    * Creates a content negotiator used to determine the response format.
    * <p>
    * If acceptHeader is null or if content negotiation is disabled in {@link WapServerConfig} an instance is returned
    * that will use the default formatter
    * 
    * @param  acceptHeader
    *                         The accept header field, may be null
    * @param  type
    *                         Type of formattable object
    * @param  profileRegistry
    *                         The JSON-LD profile registry to use
    * @param  formatRegistry
    *                         The format registry
    * @return                 The content negotiator to use
    */
   protected ContentNegotiator getContentNegotiator(String acceptHeader, Type type,
         JsonLdProfileRegistry profileRegistry, FormatRegistry formatRegistry) {
      if (WapServerConfig.getInstance().isContentNegotiationEnabled()) {
         return new ContentNegotiator(acceptHeader, type, profileRegistry, formatRegistry);
      } else {
         return new ContentNegotiator(null, type, profileRegistry, formatRegistry);
      }
   }
}
