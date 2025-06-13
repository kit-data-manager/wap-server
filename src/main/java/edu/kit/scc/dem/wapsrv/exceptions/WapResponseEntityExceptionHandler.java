package edu.kit.scc.dem.wapsrv.exceptions;

import java.util.Date;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.controller.WapController;
import org.slf4j.LoggerFactory;

/**
 * This class is used as the global exception handler for the application.
 * <p>
 * It converts all instances of {@link WapException} to a specific error format, and leaves the rest up to springs
 * default implementation.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ControllerAdvice
public class WapResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
   /**
    * Constructs a new instance for ExceptionHandling
    */
   public WapResponseEntityExceptionHandler() {
      LoggerFactory.getLogger(getClass()).info("Wap Exception handler activated");
   }

   /**
    * Handles exceptions for all instances of {@link WapException}.
    * 
    * @param  ex
    *                    The exception that occurred
    * @param  webRequest
    *                    The webRequest associated with the originating HTTP request
    * @param  request
    *                    The httpServletRequest associated with the originating HTTP request
    * @return            The ResponseEntity that is used to send the error to the client
    */
   @ExceptionHandler(value = {WapException.class})
   protected ResponseEntity<Object> handleConflict(WapException ex, WebRequest webRequest, HttpServletRequest request) {
      StringBuilder bodyOfResponse = new StringBuilder();
      bodyOfResponse.append("{\n");
      bodyOfResponse.append("   \"timestamp\":\"" + System.currentTimeMillis() + "\",\n");
      bodyOfResponse.append("   \"time\":\"" + new Date() + "\",\n");
      bodyOfResponse.append("   \"status\":\"" + ex.getHttpStatusCode() + "\",\n");
      bodyOfResponse.append("   \"error\":\"" + ex.getClass().getSimpleName() + "\",\n");
      String message = ex.getMessage();
      if (message.startsWith("[") || message.startsWith("{")) {
         // The message is already Json formatted
         bodyOfResponse.append("   \"message\":" + message + ",\n");
      } else {
         bodyOfResponse.append("   \"message\":\"" + message + "\",\n");
      }
      // Append stack trace if set
      if (WapServerConfig.getInstance().shouldAppendStackTraceToErrorMessages()) {
         bodyOfResponse.append("   \"stack trace\": [\n");
         // prefer the one delivered through cause, those are the ones injected when converting exceptions
         StackTraceElement[] trace = ex.getCause() != null ? ex.getCause().getStackTrace() : ex.getStackTrace();
         for (int n = 0;
               n < Math.min(WapServerConfig.getInstance().getMaxNumberOfStackTraceElementsToInclude(), trace.length);
               n++) {
            String comma = (n < Math.min(WapServerConfig.getInstance().getMaxNumberOfStackTraceElementsToInclude(),
                  trace.length) - 1) ? "," : "";
            StackTraceElement element = trace[n];
            bodyOfResponse.append("   \"" + element.toString() + "\"" + comma + "\n");
         }
         bodyOfResponse.append("   ],\n");
      }
      bodyOfResponse.append("   \"iri\":\"" + WapController.extractIri(request) + "\",\n");
      String queryString = WapController.getQueryString(request);
      if (queryString != null) {
         bodyOfResponse.append("   \"query string\":\"" + queryString + "\",\n");
      }
      bodyOfResponse.append("   \"complete url\":\"" + WapController.extractUrl(request) + "\"\n");
      bodyOfResponse.append("}");
      HttpHeaders headers = new HttpHeaders();
      // Customize headers if needed
      headers.setContentType(MediaType.APPLICATION_JSON);
      // Use provided method for actual handling
      return handleExceptionInternal(ex, bodyOfResponse.toString(), headers, HttpStatus.valueOf(ex.getHttpStatusCode()),
            webRequest);
   }
}
