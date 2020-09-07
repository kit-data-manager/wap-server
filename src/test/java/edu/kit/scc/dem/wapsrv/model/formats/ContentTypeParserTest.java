package edu.kit.scc.dem.wapsrv.model.formats;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import edu.kit.scc.dem.wapsrv.testscommon.JsonldTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the class ContentTypeParser
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
      classes = {WapServerConfig.class, FormatRegistry.class, JsonLdProfileRegistry.class, JsonLdFormatter.class})
@ActiveProfiles("test")
class ContentTypeParserTest {
        private static final Logger logger = LoggerFactory.getLogger(ContentTypeParserTest.class);

   private static ContentTypeParser objContentTypeParser;
   private static final Set<String> PARAM_CONTENT_TYPE_STRING_REAL_LIST = createParamContentTypeStringRealList();
   @Autowired
   private FormatRegistry formatRegistry;

   private static Set<String> createParamContentTypeStringRealList() {
      Set<String> paramList = new HashSet<String>();
      paramList.add("application/json+ld; profile=\"http://www.w3.org/ns/anno.jsonld\"");
      // Info : we need only a single valid one here. To recognize the individual formats
      // the test in the respective formatters are to be used
      // paramList.add("application/json+ld; profile=\"http://www.w3.org/ns/anno.jsonld\"");
      // paramList.add("application/n-quads");
      // paramList.add("application/n-tripples");
      // paramList.add("application/rdf+xml");
      // paramList.add("text/turtle");
      // paramList.add("application/rdf+json");
      return paramList;
   }

   /**
    * Test content type parser.
    */
   @Test
   final void testContentTypeParser() {
      ContentTypeParser actual;
      // test for all types and all contentTypeStrings from realList
      for (Type paramType : FormattableObject.Type.values()) {
         for (String paramContentTypeString : PARAM_CONTENT_TYPE_STRING_REAL_LIST) {
            actual = null;
            actual = new ContentTypeParser(paramContentTypeString, paramType, formatRegistry);
            assertNotNull(actual, "Construction did fail for type: " + paramType + ", for contentTypeString: "
                  + paramContentTypeString);
         }
      }
   }

   /**
    * Test get formatter.
    */
   @Test
   final void testGetFormatter() {
      Formatter actual;
      Formatter expected;
      // test for all types and all contentTypeStrings from realList
      for (Type paramType : FormattableObject.Type.values()) {
         for (String paramContentTypeString : PARAM_CONTENT_TYPE_STRING_REAL_LIST) {
            logger.trace("testing " + paramContentTypeString);
            objContentTypeParser = new ContentTypeParser(paramContentTypeString, paramType, formatRegistry);
            actual = null;
            actual = objContentTypeParser.getFormatter();
            expected = null;
            assertEquals(actual, expected, "expected: " + expected + "but was: " + actual);
         }
      }
   }

   /**
    * Test get Q value.
    */
   @Test
   final void testGetQValue() {
      final String acceptNoQ
            = "application/ld+json; " + "profile=\"http://www.w3.org/ns/ldp.jsonld http://www.w3.org/ns/anno.jsonld\"";
      ContentTypeParser parser = new ContentTypeParser(acceptNoQ, Type.ANNOTATION, formatRegistry);
      assertEquals(Format.JSON_LD, parser.getFormatter().getFormat());
      assertEquals(1.0, parser.getQValue());
      final String acceptQ = "application/ld+json; "
            + "profile=\"http://www.w3.org/ns/ldp.jsonld http://www.w3.org/ns/anno.jsonld\"; q=0.5";
      parser = new ContentTypeParser(acceptQ, Type.ANNOTATION, formatRegistry);
      assertEquals(Format.JSON_LD, parser.getFormatter().getFormat());
      assertEquals(0.5, parser.getQValue());
      String acceptFail = "application/ld+json; q=1";
      parser = new ContentTypeParser(acceptFail, Type.ANNOTATION, formatRegistry);
      assertNotNull(parser);
      acceptFail = "application/ld+json; q=a";
      parser = new ContentTypeParser(acceptFail, Type.ANNOTATION, formatRegistry);
      assertNotNull(parser);
      acceptFail = "application/ld+json; abc=1 ; def=1";
      parser = new ContentTypeParser(acceptFail, Type.ANNOTATION, formatRegistry);
      assertNotNull(parser);
   }
}
