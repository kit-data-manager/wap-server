package edu.kit.scc.dem.wapsrv.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import java.util.Iterator;
import java.util.List;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.app.EtagFactory;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.FormatNotAvailableException;
import edu.kit.scc.dem.wapsrv.exceptions.HttpHeaderException;
import edu.kit.scc.dem.wapsrv.exceptions.IllegalHttpParameterException;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.AnnotationList;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdFormatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.TurtleFormatter;
import edu.kit.scc.dem.wapsrv.service.AnnotationService;
import edu.kit.scc.dem.wapsrv.service.AnnotationServiceMock;
import static edu.kit.scc.dem.wapsrv.controller.ControllerTestHelper.*;

/**
 * Tests the class AnnotationController
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
      classes = {AnnotationController.class, WapServerConfig.class, JsonLdProfileRegistry.class, FormatRegistry.class,
            JsonLdFormatter.class, AnnotationServiceMock.class, TurtleFormatter.class, EtagFactory.class})
@ExtendWith(HoverflyExtension.class)
@HoverflySimulate(source = @HoverflySimulate.Source(value = "w3c_simulation.json", type = HoverflySimulate.SourceType.DEFAULT_PATH))
@ActiveProfiles("test")
class AnnotationControllerTest extends BasicWapControllerTest {
   @Autowired
   private AnnotationController controller;
   @Autowired
   private WapServerConfig wapServerConfig;
   @Autowired
   private AnnotationService service;
   @Autowired
   private EtagFactory etagFactory;

   /**
    * Test is valid service format.
    */
   @Test
   final void testIsValidServiceFormat() {
      // this relies completely on the service and is tested there
   }

   /**
    * Test get annotation.
    */
   @Test
   final void testGetAnnotation() {
      testGetHeadOptionsAnnotation(HttpMethod.GET.toString());
   }

   /**
    * Test head annotation.
    */
   @Test
   final void testHeadAnnotation() {
      testGetHeadOptionsAnnotation(HttpMethod.HEAD.toString());
   }

   /**
    * Test options annotation.
    */
   @Test
   final void testOptionsAnnotation() {
      testGetHeadOptionsAnnotation(HttpMethod.OPTIONS.toString());
   }

   /**
    * Test get annotation.
    */
   @Test
   final void testGetAnnotationWithParams() {
      testGetHeadOptionsAnnotationWithParams(HttpMethod.GET.toString());
   }

   /**
    * Test head annotation.
    */
   @Test
   final void testHeadAnnotationWithParams() {
      testGetHeadOptionsAnnotationWithParams(HttpMethod.HEAD.toString());
   }

   /**
    * Test options annotation.
    */
   @Test
   final void testOptionsAnnotationWithParams() {
      testGetHeadOptionsAnnotationWithParams(HttpMethod.OPTIONS.toString());
   }

   private void testGetHeadOptionsAnnotationWithParams(String method) {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      checkException(IllegalHttpParameterException.class, ErrorMessageRegistry.ANNOTATION_NO_PARAMETERS_ALLOWED, () -> {
         switch (method) {
         case "GET":
            controller.getAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(),
                  createParamsMap("iris=0"), TurtleFormatter.TURTLE_STRING), null);
            break;
         case "HEAD":
            controller.headAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(),
                  createParamsMap("iris=0"), TurtleFormatter.TURTLE_STRING), null);
            break;
         case "OPTIONS":
            controller.optionsAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(),
                  createParamsMap("iris=0"), TurtleFormatter.TURTLE_STRING), null);
            break;
         default:
            fail("Unimplemented test method : " + method);
         }
      });
   }

   private void testGetHeadOptionsAnnotation(String method) {
      ResponseEntity<?> response = null;
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      // get an annotation
      when(service.getAnnotation(iri)).thenReturn(createAnnotation());
      switch (method) {
      case "GET":
         response = controller.getAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.GET.toString(),
               createParamsMap(null), TurtleFormatter.TURTLE_STRING), null);
         break;
      case "HEAD":
         response = controller.headAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.HEAD.toString(),
               createParamsMap(null), TurtleFormatter.TURTLE_STRING), null);
         break;
      case "OPTIONS":
         response = controller.optionsAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.OPTIONS.toString(),
               createParamsMap(null), TurtleFormatter.TURTLE_STRING), null);
         break;
      default:
         fail("Unimplemented test method : " + method);
      }
      // check common headers
      assertNotNull(response);
      checkAllowHeader(response, org.springframework.http.HttpMethod.GET, org.springframework.http.HttpMethod.HEAD,
            org.springframework.http.HttpMethod.OPTIONS, org.springframework.http.HttpMethod.PUT,
            org.springframework.http.HttpMethod.DELETE);
      checkVaryHeader(response, "Accept");
      checkLinkHeader(response, new String[] {"<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"",
            "<http://www.w3.org/ns/oa#Annotation>; rel=\"type\""});
      assertEquals(AnnotationConstants.GET_ANNOTATION_SUCCESS_CODE, response.getStatusCode().value());
      switch (method) {
      case "GET":
         if (!response.getHeaders().get("Content-Type").get(0).startsWith(TurtleFormatter.TURTLE_STRING)) {
            fail("Unexpected format of answer : " + response.getHeaders().get("Content-Type").get(0));
         }
         assertEquals("annotationBody", response.getBody());
         assertNotNull(response.getHeaders().getETag());
         break;
      case "HEAD":
         if (!response.getHeaders().get("Content-Type").get(0).startsWith(TurtleFormatter.TURTLE_STRING)) {
            fail("Unexpected format of answer : " + response.getHeaders().get("Content-Type").get(0));
         }
         assertNotNull(response.getBody());
         // The head request should not have a body. But our response needs one so spring / jetty
         // can determine the correct content length. They remove it from the final answer
         assertNotNull(response.getHeaders().getETag());
         break;
      case "OPTIONS":
         assertNull(response.getHeaders().getContentType());
         assertNull(response.getBody());
         break;
      default:
         fail("Unimplemented test method : " + method);
      }
   }

   /**
    * Test post annotation.
    */
   @Test
   final void testPostAnnotation() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String body = null; // Body does not matter
      // accept turtle as input format
      when(service.isValidInputFormat(Format.TURTLE)).thenReturn(true);
      // Add an annotation
      when(service.postAnnotation(iri, body, Format.TURTLE)).thenReturn(createAnnotationList(null));
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, TurtleFormatter.TURTLE_STRING);
      httpHeaders.add(HttpHeaders.ACCEPT, TurtleFormatter.TURTLE_STRING);
      ResponseEntity<?> response = controller.postAnnotation(
            new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(), createParamsMap(null)), body, httpHeaders);
      assertNotNull(response);
      checkAllowHeader(response, org.springframework.http.HttpMethod.GET, org.springframework.http.HttpMethod.HEAD,
            org.springframework.http.HttpMethod.OPTIONS, org.springframework.http.HttpMethod.PUT,
            org.springframework.http.HttpMethod.DELETE);
      checkVaryHeader(response, "Accept");
      assertNotNull(response.getHeaders().getETag());
      assertNotNull(response.getHeaders().getLocation());
      checkLinkHeader(response, new String[] {"<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"",
            "<http://www.w3.org/ns/oa#Annotation>; rel=\"type\""});
      assertEquals(AnnotationConstants.POST_ANNOTATION_SUCCESS_CODE, response.getStatusCode().value());
      if (!response.getHeaders().get("Content-Type").get(0).startsWith(TurtleFormatter.TURTLE_STRING)) {
         fail("Unexpected format of answer : " + response.getHeaders().get("Content-Type").get(0));
      }
      assertEquals("annotationListBody", response.getBody());
   }

   /**
    * Test post annotation invalid internal iri
    */
   @Test
   final void testPostAnnotationInvalidInternalIri() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String body = null; // Body does not matter
      // accept turtle as input format
      when(service.isValidInputFormat(Format.TURTLE)).thenReturn(true);
      // Add a annotation
      when(service.postAnnotation(iri, body, Format.TURTLE)).thenReturn(createAnnotationList("#####"));
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, TurtleFormatter.TURTLE_STRING);
      httpHeaders.add(HttpHeaders.ACCEPT, TurtleFormatter.TURTLE_STRING);
      checkException(InternalServerException.class, ErrorMessageRegistry.INTERNAL_IRI_NOT_A_URI, () -> {
         controller.postAnnotation(
               new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(), createParamsMap(null)), body,
               httpHeaders);
      });
   }

   private AnnotationList createAnnotationList(final String iri) {
      // we do not need an actual annotation here. this would be part of the repository test
      // or any other test. All we need is an annotation that can be formatted by the formatter
      // Attention: some are default implemented in the interface, we must override them manually
      return new AnnotationList() {
         // ##### these 3 are default implemented in the interface
         @Override
         public String getEtagQuoted() {
            return "\"" + etagFactory.generateEtag() + "\""; // we need a valid one, but do not care about its value
         }

         @Override
         public String getIriString() {
            if (iri != null)
               return iri;
            else
               return "http://www.example.org/container1/anno1";
            // we need a valid one, but do not care about its value
         }

         /**
          * Returns the size of the annotation list
          * 
          * @return The number of annotations in the list
          */
         public int size() {
            return 1;
         }

         // ########
         @Override
         public String toString(Format format) throws FormatNotAvailableException {
            return "annotationListBody";
         }

         @Override
         public Type getType() {
            return null;
         }

         @Override
         public Iterator<Annotation> iterator() {
            return null;
         }

         @Override
         public IRI getContainerIri() {
            return null;
         }

         @Override
         public void setContainerIri(String containerIri) {
         }

         @Override
         public void setContainerIri(IRI containerIri) {
         }

         @Override
         public String getContainerEtag() {
            return null;
         }

         @Override
         public void setContainerEtag(String containerEtag) {
         }

         @Override
         public void addAnnotation(Annotation anno) {
         }

         @Override
         public List<Annotation> getAnnotations() {
            return null;
         }
      };
   }

   /**
    * Test post annotation with params
    */
   @Test
   final void testPostAnnotationWithParams() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String body = null; // Body does not matter
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, JsonLdFormatter.JSON_LD_STRING);
      checkException(IllegalHttpParameterException.class, ErrorMessageRegistry.ANNOTATION_NO_PARAMETERS_ALLOWED, () -> {
         controller.postAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(),
               createParamsMap("iris=0"), TurtleFormatter.TURTLE_STRING), body, httpHeaders);
      });
   }

   /**
    * Test post annotation without content type
    */
   @Test
   final void testPostAnnotationWithoutContentType() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String body = null; // Body does not matter
      // Nothing to mock, the method should just not throw an exception, return=void
      final HttpHeaders httpHeaders = new HttpHeaders();
      checkException(HttpHeaderException.class, ErrorMessageRegistry.ALL_CONTENT_TYPE_NEEDED_IN_POST, () -> {
         controller.postAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.POST.toString(), createParamsMap(null),
               TurtleFormatter.TURTLE_STRING), body, httpHeaders);
      });
   }

   /**
    * Test put annotation.
    */
   @Test
   final void testPutAnnotation() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String etag = etagFactory.generateEtag();
      final String body = null; // Body does not matter
      // accept turtle as input format
      when(service.isValidInputFormat(Format.TURTLE)).thenReturn(true);
      // Add an annotation
      when(service.putAnnotation(iri, etag, body, Format.TURTLE)).thenReturn(createAnnotation());
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.IF_MATCH, "\"" + etag + "\"");
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, TurtleFormatter.TURTLE_STRING);
      httpHeaders.add(HttpHeaders.ACCEPT, TurtleFormatter.TURTLE_STRING);
      ResponseEntity<?> response = controller.putAnnotation(
            new HttpServletRequestAdapter(iri, HttpMethod.PUT.toString(), createParamsMap(null)), body, httpHeaders);
      assertNotNull(response);
      checkAllowHeader(response, org.springframework.http.HttpMethod.GET, org.springframework.http.HttpMethod.HEAD,
            org.springframework.http.HttpMethod.OPTIONS, org.springframework.http.HttpMethod.PUT,
            org.springframework.http.HttpMethod.DELETE);
      checkVaryHeader(response, "Accept");
      assertNotNull(response.getHeaders().getETag());
      checkLinkHeader(response, new String[] {"<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"",
            "<http://www.w3.org/ns/oa#Annotation>; rel=\"type\""});
      assertEquals(AnnotationConstants.PUT_ANNOTATION_SUCCESS_CODE, response.getStatusCode().value());
      if (!response.getHeaders().get("Content-Type").get(0).startsWith(TurtleFormatter.TURTLE_STRING)) {
         fail("Unexpected format of answer : " + response.getHeaders().get("Content-Type").get(0));
      }
      assertEquals("annotationBody", response.getBody());
   }

   private Annotation createAnnotation() {
      // we do not need an actual annotation here. Just needed is an annotation that can be formatted
      return new Annotation() {
         @Override
         public Dataset getDataset() {
            return null;
         }

         @Override
         public BlankNodeOrIRI getIri() {
            return null;
         }

         @Override
         public void setIri(BlankNodeOrIRI iri) {
         }

         @Override
         public void setIri(BlankNodeOrIRI iri, boolean copyVia) {
         }

         @Override
         public void setIri(String iri, boolean copyVia) {
         }

         @Override
         public void setIri(String iri) {
         }

         @Override
         public String getEtagQuoted() {
            return "\"" + etagFactory.generateEtag() + "\""; // we need a valid one, but do not care about its value
         }

         @Override
         public String getEtag() {
            return null;
         }

         @Override
         public void setEtag(String etag) {
         }

         @Override
         public void setCreated() {
         }

         @Override
         public String getValue(IRI propertyName) {
            return null;
         }

         @Override
         public List<String> getValues(IRI propertyName) {
            return null;
         }

         @Override
         public boolean isDeleted() {
            return false;
         }

         @Override
         public String toString(Format format) throws FormatNotAvailableException {
            return "annotationBody";
         }

         @Override
         public Type getType() {
            return null;
         }

         @Override
         public String getContainerIri() {
            return null;
         }

         @Override
         public boolean hasTarget() {
            return false;
         }
      };
   }

   /**
    * Test put annotation with params
    */
   @Test
   final void testPutAnnotationWithParams() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String etag = etagFactory.generateEtag();
      final String body = null; // Body does not matter
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.IF_MATCH, "\"" + etag + "\"");
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, JsonLdFormatter.JSON_LD_STRING);
      checkException(IllegalHttpParameterException.class, ErrorMessageRegistry.ANNOTATION_NO_PARAMETERS_ALLOWED, () -> {
         controller.putAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.PUT.toString(),
               createParamsMap("iris=0"), TurtleFormatter.TURTLE_STRING), body, httpHeaders);
      });
   }

   /**
    * Test put annotation without ifmatch
    */
   @Test
   final void testPutAnnotationWithoutIfMatch() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String body = null; // Body does not matter
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, JsonLdFormatter.JSON_LD_STRING);
      checkException(HttpHeaderException.class, ErrorMessageRegistry.ALL_ETAG_NEEDED_FOR_PUT, () -> {
         controller.putAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.PUT.toString(), createParamsMap(null),
               TurtleFormatter.TURTLE_STRING), body, httpHeaders);
      });
   }

   /**
    * Test put annotation with invalid etag
    */
   @Test
   final void testPutAnnotationInvalidEtag() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String etag = etagFactory.generateEtag();
      final String body = null; // Body does not matter
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.IF_MATCH, etag); // not quoted
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, JsonLdFormatter.JSON_LD_STRING);
      checkException(HttpHeaderException.class, ErrorMessageRegistry.ALL_INVALID_ETAG_FORMAT, () -> {
         controller.putAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.PUT.toString(), createParamsMap(null),
               TurtleFormatter.TURTLE_STRING), body, httpHeaders);
      });
   }

   /**
    * Test put annotation without content type
    */
   @Test
   final void testPutAnnotationWithoutContentType() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String etag = etagFactory.generateEtag();
      final String body = null; // Body does not matter
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.IF_MATCH, "\"" + etag + "\"");
      checkException(HttpHeaderException.class, ErrorMessageRegistry.ALL_CONTENT_TYPE_NEEDED_IN_PUT, () -> {
         controller.putAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.PUT.toString(), createParamsMap(null),
               TurtleFormatter.TURTLE_STRING), body, httpHeaders);
      });
   }

   /**
    * Test delete annotation with params
    */
   @Test
   final void testDeleteAnnotationWithParams() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String etag = etagFactory.generateEtag();
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.IF_MATCH, "\"" + etag + "\"");
      checkException(IllegalHttpParameterException.class, ErrorMessageRegistry.ANNOTATION_NO_PARAMETERS_ALLOWED, () -> {
         controller.deleteAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.DELETE.toString(),
               createParamsMap("iris=0"), TurtleFormatter.TURTLE_STRING), httpHeaders);
      });
   }

   /**
    * Test delete annotation without ifmatch
    */
   @Test
   final void testDeleteAnnotationWithoutIfMatch() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final HttpHeaders httpHeaders = new HttpHeaders();
      checkException(HttpHeaderException.class, ErrorMessageRegistry.ALL_ETAG_NEEDED_FOR_DELETE, () -> {
         controller.deleteAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.DELETE.toString(),
               createParamsMap(null), TurtleFormatter.TURTLE_STRING), httpHeaders);
      });
   }

   /**
    * Test delete annotation with invalid etag
    */
   @Test
   final void testDeleteAnnotationInvalidEtag() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String etag = etagFactory.generateEtag();
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.IF_MATCH, etag); // not quoted
      checkException(HttpHeaderException.class, ErrorMessageRegistry.ALL_INVALID_ETAG_FORMAT, () -> {
         controller.deleteAnnotation(new HttpServletRequestAdapter(iri, HttpMethod.DELETE.toString(),
               createParamsMap(null), TurtleFormatter.TURTLE_STRING), httpHeaders);
      });
   }

   /**
    * Test delete annotation.
    */
   @Test
   final void testDeleteAnnotationValid() {
      final String iri = makeUrl(WapServerConfig.WAP_ENDPOINT + "container1/anno1");
      final String etag = etagFactory.generateEtag();
      // Nothing to mock, the method should just not throw an exception, return=void
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(HttpHeaders.IF_MATCH, "\"" + etag + "\"");
      ResponseEntity<?> response = controller.deleteAnnotation(new HttpServletRequestAdapter(iri,
            HttpMethod.DELETE.toString(), createParamsMap(null), TurtleFormatter.TURTLE_STRING), httpHeaders);
      assertEquals(AnnotationConstants.DELETE_ANNOTATION_SUCCESS_CODE, response.getStatusCode().value());
      assertNull(response.getBody());
   }

   @Override
   protected WapServerConfig getWapServerConfig() {
      return wapServerConfig;
   }
}
