package edu.kit.scc.dem.wapsrv.app;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests the class EtagFactory
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class EtagFactoryTest {
   /**
    * Test the ETag generation (Note, to adjust number of tests to realistic value for testing purpose)
    */
   @Test
   final void testGenerateEtag() {
      // For real testing set to big number
      // For development let it be small
      final int numberOfTest = 10;
      EtagFactory t = new EtagFactory();
      Boolean result = true;
      String eTag;
      List<String> eTagList = new ArrayList<String>();
      for (int i = 0; i < numberOfTest; i++) {
         eTag = t.generateEtag();
         if (!eTagList.contains(eTag)) {
            eTagList.add(eTag);
         } else {
            result = false;
            break;
         }
      }
      assertTrue(result == true);
   }

   /**
    * Tests public static boolean isValidEtag(String etag)
    */
   @Test
   final void testIsValidEtag() {
      String paramEtag;
      boolean actual;
      // test valid etag
      paramEtag = "\"tnfiocvvcqlhvmgttvpv\"";
      actual = EtagFactory.isValidEtag(paramEtag);
      assertTrue(actual, "Etag should be valid: " + paramEtag);
      // test null
      paramEtag = null;
      actual = EtagFactory.isValidEtag(paramEtag);
      assertFalse(actual, "Etag should be invalid: " + paramEtag);
      // test invalid etag
      paramEtag = "";
      actual = EtagFactory.isValidEtag(paramEtag);
      assertFalse(actual, "Etag should be invalid: " + paramEtag);
      // test invalid etag with surround spaces
      paramEtag = " dfgdfg ";
      actual = EtagFactory.isValidEtag(paramEtag);
      assertFalse(actual, "Etag should be invalid: " + paramEtag);
      // test invalid etag with surround spaces
      paramEtag = "\"dfgdfg";
      actual = EtagFactory.isValidEtag(paramEtag);
      assertFalse(actual, "Etag should be invalid: " + paramEtag);
      // test invalid etag with surround spaces
      paramEtag = "dfgdfg\"";
      actual = EtagFactory.isValidEtag(paramEtag);
      assertFalse(actual, "Etag should be invalid: " + paramEtag);
      // test invalid etag with inner quotes
      paramEtag = "\"df\"gdfg\"";
      actual = EtagFactory.isValidEtag(paramEtag);
      assertFalse(actual, "Etag should be invalid: " + paramEtag);
   }
}
