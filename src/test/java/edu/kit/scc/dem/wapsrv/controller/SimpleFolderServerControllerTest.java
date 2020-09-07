package edu.kit.scc.dem.wapsrv.controller;

import static org.junit.jupiter.api.Assertions.*;
import static edu.kit.scc.dem.wapsrv.controller.ControllerTestHelper.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Hashtable;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.exceptions.MethodNotAllowedException;
import edu.kit.scc.dem.wapsrv.exceptions.NotExistentException;

/**
 * Tests the class SimpleFolderServerController
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class SimpleFolderServerControllerTest {
   private static final String BASE_URL = "www.example.org";
   private static final String BASE_FOLDER = new File("./").getAbsolutePath();

   /**
    * Test simple folder server controller.
    */
   @Test
   final void testSimpleFolderServerController() {
      SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
         @Override
         protected String getFilenameForRootRequests() {
            return "index.html";
         }
      };
      assertNotNull(controller, "Could not instantiate SimpleFolderServerController");
   }

   /**
    * Test get content type string.
    */
   @Test
   final void testGetContentTypeString() {
      SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
         @Override
         protected String getFilenameForRootRequests() {
            return "index.html";
         }
      };
      assertThrows(InternalServerException.class, () -> {
         controller.getContentTypeString(null);
      });
      Map<String, String> filename2type = new Hashtable<String, String>();
      filename2type.put("test.jsonld", "application/ld+json; charset=utf-8");
      filename2type.put("test.json", "application/json; charset=utf-8");
      filename2type.put("test.js", "text/javascript; charset=utf-8");
      filename2type.put("test.html", "text/html; charset=utf-8");
      filename2type.put("test.css", "text/css; charset=utf-8");
      filename2type.put("test.rest", "text/plain; charset=utf-8");
      for (String filename : filename2type.keySet()) {
         String typeShould = filename2type.get(filename);
         String typeIs = controller.getContentTypeString(filename);
         assertEquals(typeShould, typeIs);
      }
   }

   /**
    * Test get file response with not existentFile
    */
   @Test
   public void testGetFileResponseNotExistentFile() {
      SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
         @Override
         protected String getFilenameForRootRequests() {
            return null;
         }
      };
      checkException(NotExistentException.class, ErrorMessageRegistry.FOLDER_SERVER_NOT_EXISTENT_FILE, () -> {
         controller.getFileResponse(new HttpServletRequestAdapter() {
            @Override
            public StringBuffer getRequestURL() {
               return new StringBuffer(BASE_URL + "/index" + System.currentTimeMillis() + ".html");
            }
         }, null);
      } // headers not used
      );
   }

   /**
    * Test get file response with not readable file
    */
   @Test
   public void testGetFileResponseNotReadableFile() {
      // DOTEST write the test for this method
      // Of minor usefulness, therefore ok
      // SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
      // @Override
      // protected String getFilenameForRootRequests() {
      // return null;
      // }
      // };
      //
      // checkException(NotExistentException.class, ErrorMessageRegistry.FOLDER_SERVER_NOT_EXISTENT_FILE,
      // () -> { controller.getFileResponse(new HttpServletRequestAdapter() {
      // @Override
      // public StringBuffer getRequestURL() {
      // return new StringBuffer(BASE_URL + "/index" + System.currentTimeMillis() + ".html");
      // }
      // }, null); } //headers not used
      // );
   }

   /**
    * Test get file response with a valid request
    * 
    * @throws IOException
    *                                      A IO exception
    * @throws UnsupportedEncodingException
    *                                      A exception for unsupported encoding
    */
   @Test
   public void testGetFileResponseValid() throws UnsupportedEncodingException, IOException {
      SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
         @Override
         protected String getFilenameForRootRequests() {
            return null;
         }
      };
      ResponseEntity<?> response = controller.getFileResponse(new HttpServletRequestAdapter() {
         @Override
         public StringBuffer getRequestURL() {
            return new StringBuffer(BASE_URL + "/schemas/w3c-annotation-schema.json");
         }
      }, null); // headers not used
      assertNotNull(response);
      assertEquals(HttpStatus.OK, response.getStatusCode(), "Unexcepected status code");
      // the getHeaders().get() has these [ ], they are not shown in the "real" answer
      assertEquals("[application/json; charset=utf-8]", response.getHeaders().get("Content-Type").toString(),
            "Unexcpected content type");
      checkAllowHeader(response, HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS);
      String body = new String(Files.readAllBytes(new File("./schemas/w3c-annotation-schema.json").toPath()), "UTF-8");
      assertEquals(body, response.getBody(), "Body of read file not matching");
   }

   /**
    * Test get file response with folder requested
    */
   @Test
   public void testGetFileResponseWithFolder() {
      SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
         @Override
         protected String getFilenameForRootRequests() {
            return null;
         }
      };
      checkException(MethodNotAllowedException.class, ErrorMessageRegistry.FOLDER_SERVER_NOT_A_FILE, () -> {
         controller.getFileResponse(new HttpServletRequestAdapter() {
            @Override
            public StringBuffer getRequestURL() {
               return new StringBuffer(BASE_URL + "/schemas");
            }
         }, null);
      } // headers not used
      );
   }

   /**
    * Test get file response with valid redirect
    */
   @Test
   public void testGetFileResponseRedirectToValidRootFile() {
      SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
         @Override
         protected String getFilenameForRootRequests() {
            return "index.html";
         }
      };
      ResponseEntity<?> response = controller.getFileResponse(new HttpServletRequestAdapter() {
         @Override
         public StringBuffer getRequestURL() {
            return new StringBuffer(BASE_URL);
         }
      }, null); // headers not used
      assertNotNull(response);
      assertEquals(HttpStatus.PERMANENT_REDIRECT, response.getStatusCode(), "Unexcepected status code");
      assertEquals(BASE_URL + "/index.html", response.getHeaders().getLocation().toString(),
            "Unexcepected redirection url");
   }

   /**
    * Test get file response with invalid redirect
    */
   @Test
   public void testGetFileResponseRedirectToInvalidRootFile() {
      SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
         @Override
         protected String getFilenameForRootRequests() {
            return "test !#+;";
         }
      };
      checkException(InternalServerException.class,
            ErrorMessageRegistry.INTERAL_FOLDER_SERVER_REDIRECT_ERROR + " " + BASE_URL + "/test !#+;", () -> {
               controller.getFileResponse(new HttpServletRequestAdapter() {
                  @Override
                  public StringBuffer getRequestURL() {
                     return new StringBuffer(BASE_URL + "/"); // add a / to catch the if clause...
                  }
               }, null);
            } // headers not used
      );
   }

   /**
    * Test get file response with no root file
    */
   @Test
   public void testGetFileResponseNoRootFile() {
      SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
         @Override
         protected String getFilenameForRootRequests() {
            return null;
         }
      };
      checkException(NotExistentException.class, ErrorMessageRegistry.FOLDER_SERVER_NOT_EXISTENT_FILE, () -> {
         controller.getFileResponse(new HttpServletRequestAdapter() {
            @Override
            public StringBuffer getRequestURL() {
               return new StringBuffer(BASE_URL + "/"); // add a / to catch the if clause...
            }
         }, null);
      } // headers not used
      );
   }

   /**
    * Test get file response unexpected mapping
    */
   @Test
   private void testGetFileResponseUnexpectedMapping() {
      SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
         @Override
         protected String getFilenameForRootRequests() {
            return "index.html";
         }
      };
      final String unexpectedMappingUrl = "www.test.org/test1";
      checkException(InternalServerException.class, ErrorMessageRegistry.INTERAL_FOLDER_SERVER_UNEXPECTED_MAPPING
            + " : Responsible for " + BASE_URL + " but requested " + unexpectedMappingUrl, () -> {
               controller.getFileResponse(new HttpServletRequestAdapter() {
                  @Override
                  public StringBuffer getRequestURL() {
                     return new StringBuffer(unexpectedMappingUrl);
                  }
               }, null);
            } // headers not used
      );
   }

   /**
    * Test get filename for root requests.
    */
   @Test
   final void testGetFilenameForRootRequests() {
      SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
         @Override
         protected String getFilenameForRootRequests() {
            return "index.html";
         }
      };
      assertEquals("index.html", controller.getFilenameForRootRequests());
   }

   /**
    * Test get file
    */
   @Test
   final void testGetFile() {
      SimpleFolderServerController controller = new SimpleFolderServerController(BASE_URL, BASE_FOLDER) {
         @Override
         protected String getFilenameForRootRequests() {
            return "index.html";
         }
      };
      String folder = "/test/";
      String relPath = "folder1/file.txt";
      assertEquals(new File(folder + relPath), controller.getFile(folder, relPath));
   }
}
