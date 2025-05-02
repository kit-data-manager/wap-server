package edu.kit.scc.dem.wapsrv.controller;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Properties;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.ConfigurationKeys;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import static edu.kit.scc.dem.wapsrv.controller.ControllerTestHelper.*;
import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.exceptions.FormatNotAvailableException;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import edu.kit.scc.dem.wapsrv.model.formats.ContentNegotiator;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.Formatter;
import edu.kit.scc.dem.wapsrv.model.formats.InvalidFormatterForTests;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdFormatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.NquadsFormatter;
import edu.kit.scc.dem.wapsrv.model.formats.TurtleFormatter;

/**
 * Tests the class WapController
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {JsonLdProfileRegistry.class, FormatRegistry.class, JsonLdFormatter.class,
      TurtleFormatter.class, WapServerConfig.class, InvalidFormatterForTests.class})
@ExtendWith(HoverflyExtension.class)
@HoverflySimulate(source = @HoverflySimulate.Source(value = "w3c_simulation.json", type = HoverflySimulate.SourceType.DEFAULT_PATH))
@ActiveProfiles("test")
class WapControllerTest {
   @Autowired
   private JsonLdProfileRegistry profileRegistry;
   @Autowired
   private FormatRegistry formatRegistry;
   @Autowired
   private WapServerConfig wapServerConfig;
   private final WapController wapController = new WapController() {
      @Override
      protected boolean isValidServiceFormat(Format format) {
         return format == Format.JSON_LD;
      }
   };

   // no test for abstract isValidServiceFormat needed
   /**
    * Test strip quotes.
    */
   @Test
   final void testStripQuotes() {
      String paramQuotedString;
      String actual;
      String expected;
      boolean expectedThrow;
      // test empty String
      paramQuotedString = "";
      expectedThrow = true;
      helpTestStripQuotes(paramQuotedString, expectedThrow);
      // test String quoted at begin only;
      paramQuotedString = "\"dghf";
      expectedThrow = true;
      helpTestStripQuotes(paramQuotedString, expectedThrow);
      // test String quoted at end only;
      paramQuotedString = "dghf\"";
      expectedThrow = true;
      helpTestStripQuotes(paramQuotedString, expectedThrow);
      // test String quoted in the middle;
      paramQuotedString = "dg\"hf";
      expectedThrow = true;
      helpTestStripQuotes(paramQuotedString, expectedThrow);
      // test String quoted normal;
      paramQuotedString = "\"dghf\"";
      expectedThrow = false;
      helpTestStripQuotes(paramQuotedString, expectedThrow);
      actual = wapController.stripQuotes(paramQuotedString);
      expected = "dghf";
      assertEquals(actual, expected, "Expected: " + expected + ", but was: " + actual);
   }

   /**
    * Test determine input format.
    */
   @Test
   final void testDetermineInputFormat() {
      // Check contentTypeHeader == null
      checkException(InternalServerException.class,
            ErrorMessageRegistry.INTERNAL_CONTENT_TYPE_NULL_WHERE_IT_SHOULD_NOT_BE, () -> {
               wapController.determineInputFormat(null, null, null, null);
            });
      // check unknown format (nquads not registered via autowire)
      final String contentTypeHeaderNquads = NquadsFormatter.NQUADS_STRING;
      checkException(FormatException.class,
            ErrorMessageRegistry.ALL_UNKNOWN_INPUT_FORMAT + " : " + contentTypeHeaderNquads, () -> {
               wapController.determineInputFormat(contentTypeHeaderNquads, Type.ANNOTATION, profileRegistry,
                     formatRegistry);
            });
      // Check invalid format string
      final String contentTypeHeaderInvalid = InvalidFormatterForTests.FORMAT_STRING;
      checkException(FormatException.class,
            ErrorMessageRegistry.ALL_INVALID_INPUT_FORMAT_HEADER + " : \"" + contentTypeHeaderInvalid
                  + "\" not a valid header for " + new InvalidFormatterForTests().getFormatString(),
            () -> {
               wapController.determineInputFormat(contentTypeHeaderInvalid, Type.ANNOTATION, profileRegistry,
                     formatRegistry);
            });
      // check unallowed format (only JSON-LD valid, see wapController instance here
      final String contentTypeHeaderTurtle = "text/turtle";
      checkException(FormatNotAvailableException.class,
            ErrorMessageRegistry.ALL_UNALLOWED_INPUT_FORMAT + " : " + new TurtleFormatter().getFormatString(), () -> {
               wapController.determineInputFormat(contentTypeHeaderTurtle, Type.ANNOTATION, profileRegistry,
                     formatRegistry);
            });
      // ok, now use a valid JSON-LD format
      Format format = wapController.determineInputFormat(JsonLdFormatter.JSON_LD_STRING, Type.ANNOTATION,
            profileRegistry, formatRegistry);
      assertEquals(Format.JSON_LD, format, "Unexpected format, should be JSON-LD");
   }

   /**
    * Test get content negotiator.
    */
   @Test
   final void testGetContentNegotiator() {
      // no content negotiation ==> always JSON-LD
      Properties props = WapServerConfig.getDefaultProperties();
      props.setProperty(ConfigurationKeys.EnableContentNegotiation.toString(), "false");
      wapServerConfig.updateConfig(props);
      ContentNegotiator negotiator = wapController.getContentNegotiator(TurtleFormatter.TURTLE_STRING, Type.ANNOTATION,
            profileRegistry, formatRegistry);
      assertNotNull(negotiator);
      Formatter formatter = negotiator.getFormatter();
      assertNotNull(formatter);
      Format format = formatter.getFormat();
      assertEquals(Format.JSON_LD, format, "Unexpected format, should be JSON-LD");
      // new again with enabled content negotiation
      props.setProperty(ConfigurationKeys.EnableContentNegotiation.toString(), "true");
      wapServerConfig.updateConfig(props);
      negotiator = wapController.getContentNegotiator(TurtleFormatter.TURTLE_STRING, Type.ANNOTATION, profileRegistry,
            formatRegistry);
      assertNotNull(negotiator);
      formatter = negotiator.getFormatter();
      assertNotNull(formatter);
      format = formatter.getFormat();
      assertEquals(Format.TURTLE, format, "Unexpected format, should be turtle");
   }

   private void helpTestStripQuotes(String paramQuotedString, boolean expectedThrow) {
      boolean actualThrow = false;
      try {
         wapController.stripQuotes(paramQuotedString);
      } catch (InternalServerException e) {
         actualThrow = true;
      }
      if (expectedThrow) {
         assertTrue(actualThrow,
               "With paramQuotedString: " + paramQuotedString + ", InternalServerException should have been thrown.");
      } else {
         assertFalse(actualThrow, "With paramQuotedString: " + paramQuotedString
               + ", InternalServerException should not have been thrown.");
      }
   }
}
