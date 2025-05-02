package edu.kit.scc.dem.wapsrv.testsbenchmark;

import static org.junit.jupiter.api.Assertions.fail;
import java.io.IOException;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.controller.ControllerTestHelper;
import edu.kit.scc.dem.wapsrv.model.formats.FormatRegistry;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdFormatter;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.testscommon.TestDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Do a Benchmark test on registering Json-LD Profiles
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {WapServerConfig.class, JsonLdProfileRegistry.class, FormatRegistry.class, JsonLdFormatter.class})
@ExtendWith(HoverflyExtension.class)
@HoverflySimulate(source = @HoverflySimulate.Source(value = "w3c_simulation.json", type = HoverflySimulate.SourceType.DEFAULT_PATH))
@Tag("benchmark")
@ActiveProfiles("test")
public class JsonLdJavaBenchmark {

    private static final Logger logger = LoggerFactory.getLogger(JsonLdJavaBenchmark.class);

    private static String[] annotations;
    @Autowired
    private JsonLdProfileRegistry registry;
    private long beforeTime;
    private long afterTime;

    /**
     * Prepare class.
     */
    @BeforeAll
    public static void prepareClass() {
        annotations = TestDataStore.readAnnotations();
    }

    /**
     * Prepare test.
     */
    @BeforeEach
    public void prepareTest() {
        logger.trace("---------------------------");
        logger.trace("Begin Test :");
        beforeTime = System.currentTimeMillis();
    }

    /**
     * Expand string test.
     */
    @Test
    public void expandStringTest() {
        int runs = 10000 / annotations.length;
        for (int n = 0; n < runs; n++) {
            for (String jsonLd : annotations) {
                // try {
                Object jsonObject;
                try {
                    jsonObject = JsonUtils.fromString(jsonLd);
                    jsonObject = JsonLdProcessor.expand(jsonObject, registry.getJsonLdOptions());
                    JsonUtils.toPrettyString(jsonObject);
                } catch (IOException | JsonLdError e) {
                    fail(e.getMessage());
                }
            }
        }
        logger.trace("Expanded " + annotations.length * runs + " Annotations");
    }

    /**
     * Finish test.
     */
    @AfterEach
    public void finishTest() {
        afterTime = System.currentTimeMillis();
        logger.trace("End Test :");
        logger.trace("Test duration : " + (afterTime - beforeTime) + "ms");
        logger.trace("---------------------------");
    }

    /**
     * Cleanup class.
     */
    @AfterAll
    public static void cleanupClass() {
    }
}
