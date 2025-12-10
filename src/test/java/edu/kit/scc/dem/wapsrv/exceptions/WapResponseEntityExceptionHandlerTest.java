package edu.kit.scc.dem.wapsrv.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import java.util.Properties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import edu.kit.scc.dem.wapsrv.app.ConfigurationKeys;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;

/**
 * Tests the class WapResponseEntityExceptionHandler
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class WapResponseEntityExceptionHandlerTest {
   private static WapException[] paramWapExceptionList = createWapExceptionList();

   private static WapException[] createWapExceptionList() {
      String paramString = "testing";
      WapException[] paramWapExceptionList = new WapException[18];
      int i = 0;
      paramWapExceptionList[i++] = new ContainerNotEmptyException(paramString); // #1
      paramWapExceptionList[i++] = new EtagDoesntMatchException(paramString); // #2
      paramWapExceptionList[i++] = new FormatException(paramString); // #3
      paramWapExceptionList[i++] = new FormatNotAvailableException(paramString); // #4
      paramWapExceptionList[i++] = new HttpHeaderException(paramString); // #5
      paramWapExceptionList[i++] = new IllegalHttpParameterException(paramString); // #6
      paramWapExceptionList[i++] = new IllegalPageIriException(); // #7
      paramWapExceptionList[i++] = new InternalServerException(paramString); // #8
      paramWapExceptionList[i++] = new InvalidContainerException(paramString); // #9
      paramWapExceptionList[i++] = new InvalidRequestException(paramString); // #10
      paramWapExceptionList[i++] = new MethodNotAllowedException(paramString); // #11
      paramWapExceptionList[i++] = new NotAContainerException(paramString); // #12
      paramWapExceptionList[i++] = new NotAnAnnotationException(paramString); // #13
      paramWapExceptionList[i++] = new NotExistentException(paramString); // #14
      paramWapExceptionList[i++] = new RepositoryException(paramString); // #15
      paramWapExceptionList[i++] = new ResourceDeletedException(paramString); // #16
      paramWapExceptionList[i++] = new ResourceExistsException(paramString); // #17
      paramWapExceptionList[i++] = new UnallowedPropertyChangeException(paramString); // #18
      return paramWapExceptionList;
   }

   /**
    * Test wap response entity exception handler.
    */
   @Test
   final void testWapResponseEntityExceptionHandler() {
      WapResponseEntityExceptionHandler actual;
      actual = null;
      actual = new WapResponseEntityExceptionHandler();
      assertNotNull(actual, "Construction did fail.");
   }

   /**
    * Test handle conflict.
    */
   @Test
   final void testHandleConflict() {
      WapResponseEntityExceptionHandler objWapResponseEntityExceptionHandler;
      ResponseEntity<Object> actual;
      // mock setup for HttpServletRequest
      HttpServletRequest paramHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(paramHttpServletRequest.getRequestURL()).thenReturn(new StringBuffer("getRequestURL"));
      when(paramHttpServletRequest.getQueryString()).thenReturn("getQueryString");
      // mock setup for WebRequest
      WebRequest paramWebRequest = Mockito.mock(WebRequest.class);
      // test for all WapExceptions in paramWapExceptionList
      for (WapException paramWapException : paramWapExceptionList) {
         // test default
         objWapResponseEntityExceptionHandler = new WapResponseEntityExceptionHandler();
         actual = objWapResponseEntityExceptionHandler.handleConflict(paramWapException, paramWebRequest,
               paramHttpServletRequest);
         assertNotNull(actual, "Should not be null.");
      }
      // test special cases
      WapException paramWapException;
      // test message.startsWith("[")
      paramWapException = new InvalidRequestException("[testing]");
      objWapResponseEntityExceptionHandler = new WapResponseEntityExceptionHandler();
      actual = objWapResponseEntityExceptionHandler.handleConflict(paramWapException, paramWebRequest,
            paramHttpServletRequest);
      assertNotNull(actual, "Should not be null.");
      // test message.startsWith("{")
      paramWapException = new InvalidRequestException("{testing}");
      objWapResponseEntityExceptionHandler = new WapResponseEntityExceptionHandler();
      actual = objWapResponseEntityExceptionHandler.handleConflict(paramWapException, paramWebRequest,
            paramHttpServletRequest);
      assertNotNull(actual, "Should not be null.");
      // test queryString == null
      when(paramHttpServletRequest.getQueryString()).thenReturn(null);
      paramWapException = new InvalidRequestException("testing");
      objWapResponseEntityExceptionHandler = new WapResponseEntityExceptionHandler();
      actual = objWapResponseEntityExceptionHandler.handleConflict(paramWapException, paramWebRequest,
            paramHttpServletRequest);
      assertNotNull(actual, "Should not be null.");
      // testing WHEN WapServerConfig.getInstance().shouldAppendStackTraceToErrorMessages == true
      WapServerConfig objWapServerConfig = WapServerConfig.getInstance();
      Properties properties = WapServerConfig.getDefaultProperties();
      properties.setProperty(ConfigurationKeys.ShouldAppendStackTraceToErrorMessages.toString(), "true");
      objWapServerConfig.updateConfig(properties);
      // test default
      paramWapException = new InvalidRequestException("testing");
      objWapResponseEntityExceptionHandler = new WapResponseEntityExceptionHandler();
      actual = objWapResponseEntityExceptionHandler.handleConflict(paramWapException, paramWebRequest,
            paramHttpServletRequest);
      assertNotNull(actual, "Should not be null.");
      // test ex.getCause() == null
      paramWapException = new FormatException("testing", new Throwable());
      objWapResponseEntityExceptionHandler = new WapResponseEntityExceptionHandler();
      actual = objWapResponseEntityExceptionHandler.handleConflict(paramWapException, paramWebRequest,
            paramHttpServletRequest);
      assertNotNull(actual, "Should not be null.");
      // cleaning up: reset WapServerConfig
      objWapServerConfig.updateConfig(WapServerConfig.getDefaultProperties());
   }
}
