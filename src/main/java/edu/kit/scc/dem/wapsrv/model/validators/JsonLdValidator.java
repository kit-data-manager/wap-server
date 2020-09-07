package edu.kit.scc.dem.wapsrv.model.validators;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * Implements a input validator for JSON-LD that validates against the WADM schema
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Component
public class JsonLdValidator implements Validator {
   /**
    * The path where the schema file can be found for annotations
    */
   private final String fileNameOfAnnotationSchema;
   /**
    * The schema as a JsonNode for containers
    */
   private JsonNode jsonContainerSchemaNode = null;
   /**
    * The path where the schema file can be found for containers
    */
   private final String fileNameOfContainerSchema;
   /**
    * The schema as a JsonNode for annotations
    */
   private JsonNode jsonAnnotationSchemaNode = null;

   /**
    * Sets the WapServerConfig to use
    * 
    * @param config
    *               The config to use
    */
   @Autowired
   public JsonLdValidator(WapServerConfig config) {
      fileNameOfAnnotationSchema
            = new File(config.getJsonLdValidatorSchemaFolder(), "w3c-annotation-schema.json").getAbsolutePath();
      fileNameOfContainerSchema
            = new File(config.getJsonLdValidatorSchemaFolder(), "ldp-container-schema.json").getAbsolutePath();
   }

   @Override
   public boolean validateAnnotation(String annotationString) {
      JsonNode schema;
      try {
         schema = getAnnotationSchema();
         return validateJson(annotationString, schema);
      } catch (IOException | ProcessingException | JsonLdError e) {
         throw new FormatException(e.getMessage());
      }
   }

   @Override
   public boolean validateContainer(String containerString) {
      JsonNode schema;
      try {
         schema = getContainerSchema();
         return validateJson(containerString, schema);
      } catch (IOException | ProcessingException | JsonLdError e) {
         throw new FormatException(e.getMessage());
      }
   }

   @Override
   public Format getFormat() {
      return Format.JSON_LD;
   }

   /**
    * Validates a given string to the given schema
    * 
    * @param  jsonStr
    *                             The string to validate
    * @param  schema
    *                             The schema to use
    * @return                     true if valid, false will not occur, an exception is thrown
    * @throws IOException
    *                             On I/O errors
    * @throws ProcessingException
    *                             When JSON-LD has processing problems
    * @throws JsonLdError
    *                             When JSON-LD has errors
    */
   private boolean validateJson(String jsonStr, JsonNode schema) throws IOException, ProcessingException, JsonLdError {
      Object jsonObj = JsonUtils.fromString(jsonStr);
      List<Object> expandedJson = JsonLdProcessor.expand(jsonObj);
      String newJsonStr = JsonUtils.toString(expandedJson);
      JsonNode json = JsonLoader.fromString(newJsonStr);
      JsonValidator jsonValidator = JsonSchemaFactory.byDefault().getValidator();
      ProcessingReport processingReport = jsonValidator.validate(schema, json);
      if (!processingReport.isSuccess()) {
         ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
         Iterator<ProcessingMessage> iterator = processingReport.iterator();
         while (iterator.hasNext()) {
            ProcessingMessage processingMessage = iterator.next();
            jsonArray.add(processingMessage.asJson());
         }
         String errorJson = JsonUtils.toPrettyString(jsonArray);
         throw new IOException(errorJson);
      } else {
         return true;
      }
   }

   /**
    * Reads a given file into a string
    * 
    * @param  file
    *              The file to read
    * @return      The string content of the file
    */
   private String getJson(File file) {
      try {
         int read = 0;
         byte[] bytes = new byte[(int) file.length()];
         FileInputStream in = new FileInputStream(file);
         while (read != bytes.length) {
            read += in.read(bytes, read, bytes.length - read);
         }
         in.close();
         return new String(bytes);
      } catch (IOException ex) {
         LoggerFactory.getLogger(getClass()).error("Read error for " + file.getName());
         return null;
      }
   }

   /**
    * Gets the jsonNode implementing the schema for annotations
    * 
    * @return             The JSON schema node
    * @throws IOException
    */
   private synchronized JsonNode getAnnotationSchema() throws IOException {
      if (jsonAnnotationSchemaNode != null) {
         return jsonAnnotationSchemaNode;
      }
      String jsonSchemaString = getJson(new File(fileNameOfAnnotationSchema));
      jsonAnnotationSchemaNode = JsonLoader.fromString(jsonSchemaString);
      return jsonAnnotationSchemaNode;
   }

   /**
    * Gets the jsonNode implementing the schema for containers
    * 
    * @return             The JSON schema node
    * @throws IOException
    */
   private synchronized JsonNode getContainerSchema() throws IOException {
      if (jsonContainerSchemaNode != null) {
         return jsonContainerSchemaNode;
      }
      String jsonSchemaString = getJson(new File(fileNameOfContainerSchema));
      jsonContainerSchemaNode = JsonLoader.fromString(jsonSchemaString);
      return jsonContainerSchemaNode;
   }
}
