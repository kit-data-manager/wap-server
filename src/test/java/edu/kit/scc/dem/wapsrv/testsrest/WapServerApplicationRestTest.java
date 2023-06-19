package edu.kit.scc.dem.wapsrv.testsrest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test REST interface.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@Tag("rest")
@ActiveProfiles("test")
public class WapServerApplicationRestTest extends AbstractRestTest {

    private static final Logger logger = LoggerFactory.getLogger(WapServerApplicationRestTest.class);

    /**
     * Create a new WapServerpplicationRestTest
     */
    protected WapServerApplicationRestTest() {
        super(true);
    }

    /**
     * Gets the lock.
     */
    @BeforeEach
    public void getLock() {
        lock();
        logger.trace("Running test");
    }

    /**
     * Free The lock.
     */
    @AfterEach
    public void freeLock() {
        logger.trace("Finished test");
        unlock();
    }

    /**
     * Test wap server application.
     */
    @Test
    final void testWapServerApplication() {
        // Not tested here. Test is executed in
        // edu.kit.scc.dem.wapsrv.testsrest.WapServerApplicationRestTest
    }

    /**
     * Test block until started.
     */
    @Test
    final void testBlockUntilStarted() {
        // Not tested here. Test is executed in
        // edu.kit.scc.dem.wapsrv.testsrest.WapServerApplicationRestTest
    }

    /**
     * Test main.
     */
    @Test
    final void testMain() {
        // Not tested here. Test is executed in
        // edu.kit.scc.dem.wapsrv.testsrest.WapServerApplicationRestTest
    }

    /**
     * Test create config if not existent.
     */
    @Test
    final void testCreateConfigIfNotExistent() {
        // Not tested here. Test is executed in
        // edu.kit.scc.dem.wapsrv.testsrest.WapServerApplicationRestTest
    }

    /**
     * Test get running application context
     */
    @Test
    final void testGetRunningApplicationContext() {
        // Not tested here. Test is executed in
        // edu.kit.scc.dem.wapsrv.testsrest.WapServerApplicationRestTest
    }

    /**
     * Test deint.
     */
    @Test
    final void testDeinit() {
        // Not tested here. Test is executed in
        // edu.kit.scc.dem.wapsrv.testsrest.WapServerApplicationRestTest
    }

    /**
     * Test set application context
     */
    @Test
    final void testSetApplicationContext() {
        // Not tested here. Test is executed in
        // edu.kit.scc.dem.wapsrv.testsrest.WapServerApplicationRestTest
    }
}
