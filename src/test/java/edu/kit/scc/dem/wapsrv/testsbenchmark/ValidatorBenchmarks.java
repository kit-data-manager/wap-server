package edu.kit.scc.dem.wapsrv.testsbenchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
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
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.testscommon.TestDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Do a Benchmark test on validation JSON-LD Profiles
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
public class ValidatorBenchmarks {

    private static final Logger logger = LoggerFactory.getLogger(ValidatorBenchmarks.class);

    private static final String[] ANNOTATIONS = TestDataStore.readAnnotations();
    private static final File SCHEMAFILE = new File("schemas/w3c-annotation-schema.json");
    private JsonNode jsonSchemaNode = null;

    /**
     * The main method.
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        initSpring();
        runTest("Validate annotations", new Runnable() {
            public void run() {
                validateAnnotations();
            }
        });
    }

    private static void initSpring() {
        validateAnnotations();
    }

    private static void runTest(String description, Runnable runnable) {
        logger.trace("------------------------------------------");
        logger.trace("Running test : " + description);
        long before = System.currentTimeMillis();
        runnable.run();
        long after = System.currentTimeMillis();
        logger.trace("Test time : " + (after - before) + " ms");
        logger.trace("------------------------------------------");
    }

    private static void validateAnnotations() {
        ValidatorBenchmarks validator = new ValidatorBenchmarks();
        for (String annotationString : ANNOTATIONS) {
            validator.validate(annotationString);
        }
        logger.trace("Validated " + ANNOTATIONS.length + " annotations");
    }

    private boolean validateJson(String jsonStr) throws IOException, ProcessingException, JsonLdError {
        String newJsonStr = jsonStr;
        JsonNode schema = getSchema();
        Object jsonObj = JsonUtils.fromString(newJsonStr);
        List<Object> expandedJson = JsonLdProcessor.expand(jsonObj);
        newJsonStr = JsonUtils.toString(expandedJson);
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
            return false;
        } else {
            return true;
        }
    }

    private String getJson(File file) throws IOException {
        try {
            int read = 0;
            byte[] bytes = new byte[(int) file.length()];
            FileInputStream in = new FileInputStream(file);
            while (read != bytes.length) {
                read += in.read(bytes, read, bytes.length - read);
            }
            in.close();
            return new String(bytes);
        } catch (FileNotFoundException e) {
            System.err.println("Read error for " + file.getName());
            return null;
        }
    }

    private synchronized JsonNode getSchema() throws IOException {
        if (jsonSchemaNode != null) {
            return jsonSchemaNode;
        }
        String jsonSchemaString = getJson(SCHEMAFILE);
        jsonSchemaNode = JsonLoader.fromString(jsonSchemaString);
        return jsonSchemaNode;
    }

    /**
     * Validate annotation
     *
     * @param annotationString The annotation string
     * @return True, if successful
     */
    public boolean validate(String annotationString) {
        try {
            return validateJson(annotationString);
        } catch (IOException | ProcessingException | JsonLdError e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets the format.
     *
     * @return The format
     */
    public Format getFormat() {
        return Format.JSON_LD;
    }
}
