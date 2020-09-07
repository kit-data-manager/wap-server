package edu.kit.scc.dem.wapsrv.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the interface FormattableObjectTest
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class ModelFactoryTest {
   /**
    * Test get container IRI form annotation IRI.
    */
   @Test
   final void testGetContainerIriFormAnnotationIri() {
      String annotationIri = null;
      assertNull(ModelFactory.getContainerIriFormAnnotationIri(annotationIri), "There was null expected");
      annotationIri = "123456";
      assertNull(ModelFactory.getContainerIriFormAnnotationIri(annotationIri),
            "There was null expected, because no / was found");
      annotationIri = "123/456";
      String expected = annotationIri.substring(0, annotationIri.lastIndexOf("/") + 1);
      assertEquals(expected, ModelFactory.getContainerIriFormAnnotationIri(annotationIri),
            "The string is not as expected");
   }
}
