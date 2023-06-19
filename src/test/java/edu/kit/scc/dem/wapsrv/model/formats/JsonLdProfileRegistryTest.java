package edu.kit.scc.dem.wapsrv.model.formats;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.ConfigurationKeys;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;
import edu.kit.scc.dem.wapsrv.testscommon.TestDataStore;
import static edu.kit.scc.dem.wapsrv.controller.ControllerTestHelper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests the class JsonLdProfileRegistry
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WapServerConfig.class})
@ActiveProfiles("test")
class JsonLdProfileRegistryTest {

    private static final Logger logger = LoggerFactory.getLogger(JsonLdProfileRegistryTest.class);

    private static final String TEST_FOLDER = "temp/tests/JsonLdProfileRegistry";
    private static final long CACHE_VALIDITY = 1000;
    private static final String ANNO_URL_STRING = "http://www.w3.org/ns/anno.jsonld";
    private static final String LDP_URL_STRING = "http://www.w3.org/ns/ldp.jsonld";
    private static final String AS_URL_STRING = "https://www.w3.org/ns/activitystreams.jsonld";
    @Autowired
    private WapServerConfig config;
    // These two should not be accessed directly
    private JsonLdProfileRegistry profileRegistryHidden;
    private Properties initialPropsHidden;

    @BeforeEach
    private void initConfig() {
        Properties props = WapServerConfig.getDefaultProperties();
        props.setProperty(ConfigurationKeys.JsonLdProfileFolder.toString(), TEST_FOLDER);
        props.setProperty(ConfigurationKeys.JsonLdCachedProfileValidityInMs.toString(), CACHE_VALIDITY + "");
        // Apply to config
        config.updateConfig(props);
        // Clear test folder
        initTestFolder();
        initialPropsHidden = props;
    }

    /**
     * Use this method to access props
     *
     * @return The initial props
     */
    private Properties getInitialProps() {
        return initialPropsHidden;
    }

    /**
     * Use this one to get a fresh profile registry
     *
     * @return
     */
    private JsonLdProfileRegistry getJsonLdProfileRegistry() {
        return getJsonLdProfileRegistry(true);
    }

    /**
     * Use this one to get a fresh profile registry
     *
     * @return
     */
    private JsonLdProfileRegistry getJsonLdProfileRegistry(boolean init) {
        assertNull(this.profileRegistryHidden);
        profileRegistryHidden = new JsonLdProfileRegistry();
        if (init) {
            profileRegistryHidden.init(config);
        }
        return profileRegistryHidden;
    }

    /**
     * Reset config and stop updater
     */
    @AfterEach
    public void afterEach() {
        Properties props = WapServerConfig.getDefaultProperties();
        config.updateConfig(props);
        if (profileRegistryHidden != null) {
            profileRegistryHidden.deinit();
            profileRegistryHidden = null;
        }
    }

    private void initTestFolder() {
        logger.trace("Cleaning " + TEST_FOLDER);
        File testFolder = new File(TEST_FOLDER);
        if (!testFolder.exists()) {
            if (!testFolder.mkdirs()) {
                fail("Cannot create test folder");
            }
        } else {
            File[] files = testFolder.listFiles();
            for (File file : files) {
                if (!file.delete()) {
                    fail("Cannot delete file in test folder : " + file.getName());
                }
            }
        }
        File profileDb = new File(testFolder, WapServerConfig.getInstance().getJsonLdProfileFile());
        try {
            if (!profileDb.createNewFile()) {
                fail("Cannot create profile db : " + profileDb.getName());
            }
        } catch (IOException ex) {
            fail("Cannot create profile db : " + profileDb.getName() + " - " + ex.getMessage());
        }
        // Create empty profile db to fill it for testing cacheProfile
        try {
            Properties props = new Properties();
            FileOutputStream out = new FileOutputStream(profileDb);
            props.storeToXML(out, "Registered JSON-LD Profiles - Usage: key=filename, value=URL used in context");
            out.flush();
            out.close();
        } catch (IOException e) {
            fail("Could not populate profile db :" + e.getMessage());
        }
    }

    private URL getAnnoProfileUrl() {
        try {
            return new URL(ANNO_URL_STRING);
        } catch (MalformedURLException e) {
            fail("Internal error, invalid profile url " + e.getMessage());
            return null;
        }
    }

    private URL getLdpProfileUrl() {
        try {
            return new URL(LDP_URL_STRING);
        } catch (MalformedURLException e) {
            fail("Internal error, invalid profile url " + e.getMessage());
            return null;
        }
    }

    private URL getAsProfileUrl() {
        try {
            return new URL(AS_URL_STRING);
        } catch (MalformedURLException e) {
            fail("Internal error, invalid profile url " + e.getMessage());
            return null;
        }
    }

    private void updateSystemProperty(String value, String key) {
        if (value == null) {
            System.getProperties().remove(key);
        } else {
            System.setProperty(key, value);
        }
    }
    // ################# Now come the real tests

    /**
     * Test that cache updating works
     */
    @Test
    final void testCacheUpdateWorks() {
        JsonLdProfileRegistry instance = getJsonLdProfileRegistry();
        URL profileUrl = getAnnoProfileUrl();
        long actualTime = instance.getLastUpdateTime(profileUrl);
        if (actualTime < 0) {
            fail("Profile not existent for cache update test :(");
        }
        logger.trace("Cache time before : " + actualTime);
        try {
            Thread.sleep(2000 + CACHE_VALIDITY); // 1s for downloading and the delay we have to wait
        } catch (InterruptedException e) {
            fail("Test interrupted ? " + e.getMessage());
        }
        long newTime = instance.getLastUpdateTime(profileUrl);
        if (newTime < 0) {
            fail("Internal error : Profile not existent for cache update test step 2");
        }
        logger.trace("Cache time after : " + newTime);
        if (actualTime == newTime) {
            fail("Cache update has not been performed as expected");
        }
    }

    /**
     * Tests that caching works with not working downloads
     */
    @Test
    final void testCacheUpdateDelayWorks() {
        System.setProperty(JsonLdProfileRegistry.DISABLED_UPDATER_PROPERTY, "xyz");
        JsonLdProfileRegistry instance = getJsonLdProfileRegistry();
        URL annoUrl = getAnnoProfileUrl();
        assertTrue(instance.cacheProfile(annoUrl));
        final String httpProxy = System.getProperty("http.proxyHost");
        final String httpsProxy = System.getProperty("https.proxyHost");
        final String httpPort = System.getProperty("http.proxyPort");
        final String httpsPort = System.getProperty("https.proxyPort");
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "65400");
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "65400");
        final long lastTime = instance.getLastUpdateTime(annoUrl);
        try {
            Thread.sleep(CACHE_VALIDITY + 500);
        } catch (InterruptedException e) {
        }
        instance.update();
        // run twice to get the case with one failure before exists
        instance.update();
        long newTime = instance.getLastUpdateTime(annoUrl);
        boolean erg = instance.isCachedProfile(annoUrl);
        updateSystemProperty(httpProxy, "http.proxyHost");
        updateSystemProperty(httpPort, "http.proxyPort");
        updateSystemProperty(httpsProxy, "https.proxyHost");
        updateSystemProperty(httpsPort, "https.proxyPort");
        updateSystemProperty(null, JsonLdProfileRegistry.DISABLED_UPDATER_PROPERTY);
        assertTrue(erg);
        assertEquals(lastTime, newTime);
    }

    /**
     * Tests that caching works with not working downloads and removal of not
     * updated profiles
     */
    @Test
    final void testCacheUpdateDelayNotKeepingLocalProfiles() {
        System.setProperty(JsonLdProfileRegistry.DISABLED_UPDATER_PROPERTY, "xyz");
        JsonLdProfileRegistry instance = getJsonLdProfileRegistry(false);
        profileRegistryHidden.init(config, false);
        URL annoUrl = getAnnoProfileUrl();
        assertTrue(instance.cacheProfile(annoUrl));
        final String httpProxy = System.getProperty("http.proxyHost");
        final String httpsProxy = System.getProperty("https.proxyHost");
        final String httpPort = System.getProperty("http.proxyPort");
        final String httpsPort = System.getProperty("https.proxyPort");
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "65400");
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "65400");
        try {
            Thread.sleep(CACHE_VALIDITY + 500);
        } catch (InterruptedException e) {
        }
        instance.update();
        updateSystemProperty(httpProxy, "http.proxyHost");
        updateSystemProperty(httpPort, "http.proxyPort");
        updateSystemProperty(httpsProxy, "https.proxyHost");
        updateSystemProperty(httpsPort, "https.proxyPort");
        updateSystemProperty(null, JsonLdProfileRegistry.DISABLED_UPDATER_PROPERTY);
        assertFalse(instance.isCachedProfile(annoUrl));
    }

    /**
     * Test is cached profile.
     */
    @Test
    final void testIsCachedProfile() {
        JsonLdProfileRegistry profileRegistry = getJsonLdProfileRegistry();
        assertTrue(profileRegistry.cacheProfile(getAnnoProfileUrl()));
        assertTrue(profileRegistry.isCachedProfile(getAnnoProfileUrl()));
    }

    /**
     * Test if defualt profiles are loaded
     */
    @Test
    final void testDefaultCachedProfilesLoaded() {
        JsonLdProfileRegistry profileRegistry = getJsonLdProfileRegistry();
        assertTrue(profileRegistry.isCachedProfile(getLdpProfileUrl()));
        assertTrue(profileRegistry.isCachedProfile(getAnnoProfileUrl()));
    }

    /**
     * Test get instance.
     */
    @Test
    final void testGetInstance() {
        assertNotNull(getJsonLdProfileRegistry());
    }

    /**
     * Test get JSON-LD options.
     */
    @Test
    final void testGetJsonLdOptions() {
        JsonLdProfileRegistry profileRegistry = getJsonLdProfileRegistry();
        assertNotNull(profileRegistry.getJsonLdOptions());
    }

    /**
     * Test cache profile.
     */
    @Test
    final void testCacheProfile() {
        JsonLdProfileRegistry profileRegistry = getJsonLdProfileRegistry();
        URL url = this.getAsProfileUrl();
        assertFalse(profileRegistry.isCachedProfile(url));
        assertTrue(profileRegistry.cacheProfile(url));
        assertTrue(profileRegistry.isCachedProfile(url));
    }

    /**
     * Test get last update time.
     */
    @Test
    final void testGetLastUpdateTime() {
        JsonLdProfileRegistry profileRegistry = getJsonLdProfileRegistry();
        URL url = this.getAnnoProfileUrl();
        assertTrue(profileRegistry.cacheProfile(url));
        assertNotEquals(-1, profileRegistry.getLastUpdateTime(url));
    }

    /**
     * Test get non existing frame string.
     */
    @Test
    final void testGetNonExistingFrameString() {
        final Type type = Type.PAGE;
        Properties props = getInitialProps();
        props.setProperty(ConfigurationKeys.JsonLdFrameFolder.toString(), "nonExistentFolder");
        WapServerConfig.getInstance().updateConfig(props);
        //config.updateConfig(props);
        checkException(InternalServerException.class, "Could not load JSON-LD Frame file for " + type, () -> {
            JsonLdProfileRegistry instance = getJsonLdProfileRegistry();
            instance.getFrameString(type);
        });
    }

    /**
     * Test get frame strings
     */
    @Test
    final void testGetFrameString() {
        JsonLdProfileRegistry instance = getJsonLdProfileRegistry();
        for (Type type : Type.values()) {
            assertNotNull(instance.getFrameString(type));
        }
    }

    /**
     * Test get frame strings
     */
    @Test
    final void testBlockUntilInit() {
        final JsonLdProfileRegistry instance = getJsonLdProfileRegistry(false);
        final List<Integer> list = new ArrayList<Integer>();
        Thread helper = new Thread() {
            public void run() {
                list.add(1);
                // The call blocks until we call init manually
                instance.isCachedProfile(getAnnoProfileUrl());
                list.add(2);
            }
        };
        helper.start();
        while (list.isEmpty()) {
            Thread.yield(); // This way the helper is guaranteed to have been executed
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        assertEquals(1, list.size());
        instance.init(config);
        try {
            helper.join(500);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
        assertEquals(2, list.size());
    }

    /**
     * Test profile folder creation
     */
    @Test
    final void testProfileFolderGeneration() {
        logger.trace("Cleaning " + TEST_FOLDER);
        File testFolder = new File(TEST_FOLDER);
        if (testFolder.exists()) {
            File[] files = testFolder.listFiles();
            for (File file : files) {
                if (!file.delete()) {
                    fail("Cannot delete file in test folder : " + file.getName());
                }
            }
            assertTrue(testFolder.delete());
        }
        JsonLdProfileRegistry instance = getJsonLdProfileRegistry();
        assertNotNull(instance);
        assertTrue(instance.isCachedProfile(getAnnoProfileUrl()));
    }

    /**
     * Test expand JSON-LD.
     */
    @Test
    final void testExpandJsonLd() {
        final String testString = "http://www.w3.org/ns/oa#hasBody";
        final String anno = TestDataStore.getAnnotation(1);
        assertNotNull(anno);
        assertEquals(-1, anno.indexOf(testString));
        JsonLdProfileRegistry profileRegistry = getJsonLdProfileRegistry();
        assertTrue(profileRegistry.cacheProfile(getAnnoProfileUrl()));
        final String expanded = profileRegistry.expandJsonLd(anno);
        assertNotNull(expanded);
        logger.trace(expanded);
        assertNotEquals(-1, expanded.indexOf(testString));
    }
}
