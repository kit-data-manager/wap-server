package edu.kit.scc.dem.wapsrv.model.formats;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import static edu.kit.scc.dem.wapsrv.controller.ControllerTestHelper.*;

/**
 * Tests the class FormatRegistry
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WapServerConfig.class, JsonLdProfileRegistry.class, FormatRegistry.class,
      JsonLdFormatter.class, TurtleFormatter.class})
@ActiveProfiles("test")
class FormatRegistryTest {
   private static final List<Formatter> PARAM_FORMATTER_LIST = createFormatterList();
   private static final List<String> PARAM_FORMATTER_STRING_LIST = createFormatterStringList();
   private FormatRegistry objFormatRegistry;
   private boolean registeredFormatters = false;

   private static List<Formatter> createFormatterList() {
      List<Formatter> paramFormatterList = new ArrayList<Formatter>();
      paramFormatterList.add(new NquadsFormatter());
      paramFormatterList.add(new RdfXmlFormatter());
      paramFormatterList.add(new TurtleFormatter());
      return paramFormatterList;
   }

   private static List<String> createFormatterStringList() {
      List<String> paramFormatterStringList = new ArrayList<String>();
      for (Formatter formatter : PARAM_FORMATTER_LIST) {
         paramFormatterStringList.add(formatter.getFormatString());
      }
      return paramFormatterStringList;
   }

   @Autowired
   private void setFormatRegistry(FormatRegistry objFormatRegistry) {
      for (Formatter paramFormatter : PARAM_FORMATTER_LIST) {
         objFormatRegistry.registerFormatter(paramFormatter);
      }
      registeredFormatters = true;
      this.objFormatRegistry = objFormatRegistry;
   }

   /**
    * Test register formatter.
    */
   @Test
   final void testRegisterFormatter() {
      if (!registeredFormatters) {
         fail("Formatters could not be registered");
      }
   }

   /**
    * Test error cases
    */
   @Test
   final void testErros() {
      checkException(NullPointerException.class, "Null pointer given, formatString must not be null", () -> {
         objFormatRegistry.getFormatter(null);
      });
      final String unknownFormat = "application/unkown";
      checkException(NullPointerException.class, "No class known to implement the given format string " + unknownFormat,
            () -> {
               objFormatRegistry.getFormatter(unknownFormat);
            });
   }

   /**
    * Test get format strings.
    */
   @Test
   final void testGetFormatStrings() {
      Set<String> formatStrings = objFormatRegistry.getFormatStrings();
      assertNotNull(formatStrings);
      for (String formatString : PARAM_FORMATTER_STRING_LIST) {
         assertTrue(formatString.contains(formatString), "Not contained : " + formatString);
      }
   }

   /**
    * Test get formatter.
    */
   @Test
   final void testGetFormatter() {
      Formatter actual;
      boolean actualContainsExpected;
      // test for all format strings
      for (String paramFormatString : PARAM_FORMATTER_STRING_LIST) {
         try {
            actual = objFormatRegistry.getFormatter(paramFormatString);
         } catch (InternalServerException | NullPointerException e) {
            fail(e.getMessage());
            return;
         }
         actualContainsExpected = false;
         for (Formatter item : PARAM_FORMATTER_LIST) {
            if (item.getClass() == actual.getClass() || actual == null) {
               actualContainsExpected = true;
            }
         }
         assertTrue(actualContainsExpected,
               "Formatter not in expected list for String: " + paramFormatString + ", for Formatter: " + actual);
      }
   }
}
