package edu.kit.scc.dem.wapsrv.app;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

/**
 * Tests the class WapServerConfig
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
class WapServerConfigTest {

    private static final Logger logger = LoggerFactory.getLogger(WapServerConfigTest.class);

    private static WapServerConfig objWapServerConfig;
    private static Properties storedProperties;
    private static String serverAddress;
    private static String serverPort;
    private static String sslEnabled;
    private static String sslAlias;
    private static String sslKeystore;
    private static String sslKeystorePw;
    private static String sslKeyPw;
    private static String hostname;
    private static boolean testSsl = true;
    @SuppressWarnings("unused")
    private static String sslFileBef;
    private static boolean testConfGeneration = true;
    private static File defaultConfFile;

    /**
     * Init
     */
    @BeforeAll
    protected static void init() {
        serverAddress = System.getProperty("server.address");
        serverPort = System.getProperty("server.port");
        hostname = System.getProperty("hostname");
        sslEnabled = System.getProperty("server.ssl.enabled");
        sslAlias = System.getProperty("server.ssl.alias");
        sslKeystore = System.getProperty("server.ssl.key-store");
        sslKeystorePw = System.getProperty("server.ssl.key-store-password");
        sslKeyPw = System.getProperty("server.ssl.key-password");
        try {
            File sslTemp = File.createTempFile("ssl", ".conf");
            String str = "key-store-file=wap.pkcs12\n" + "key-store-password=test1234\n" + "key-password=test1234\n"
                    + "server.ssl.alias=1\n";
            FileOutputStream out = new FileOutputStream(sslTemp);
            out.write(str.getBytes());
            out.flush();
            out.close();
            sslFileBef = WapServerConfig.sslConfigFile;
            WapServerConfig.sslConfigFile = sslTemp.getAbsolutePath();
        } catch (IOException e) {
            testSsl = false;
        }
        defaultConfFile = WapServerConfig.getWapServerConfigFile();
        try {
            File confTemp = File.createTempFile("application", ".properties");
            confTemp.delete();
            logger.trace("Using conf file for test : " + confTemp.getAbsolutePath());
            WapServerConfig.setWapServerConfigFile(confTemp);
            if (WapServerConfig.isConfigFileExistent()) {
                testConfGeneration = false;
            } else {
                logger.trace("Hier");
                WapServerConfig.createDefaultConfigurationFile();
                testConfGeneration = WapServerConfig.isConfigFileExistent();
            }
        } catch (IOException e) {
            testConfGeneration = false;
        }
        objWapServerConfig = new WapServerConfig();
        storedProperties = WapServerConfig.getDefaultProperties();
    }

    /**
     * shutown
     */
    @AfterAll
    protected static void afterAll() {
        WapServerConfig.setWapServerConfigFile(defaultConfFile);
        updateProperty(serverAddress, "server.address");
        updateProperty(serverPort, "server.port");
        updateProperty(hostname, "server.address");
        updateProperty(sslEnabled, "server.ssl.enabled");
        updateProperty(sslAlias, "server.ssl.alias");
        updateProperty(sslKeystore, "server.ssl.key-store");
        updateProperty(sslKeystorePw, "server.ssl.key-store-password");
        updateProperty(sslKeyPw, "server.ssl.key-password");
    }

    private static void updateProperty(String value, String key) {
        if (value == null) {
            System.getProperties().remove(key);
        } else {
            System.setProperty(key, value);
        }
    }

    /**
     * After each test: restore Properties to clean unwanted side effects.
     */
    @AfterEach
    public void restoreProperties() {
        objWapServerConfig.updateConfig(storedProperties);
    }

    /**
     * Tests construction of protected WapServerConfig()
     */
    @Test
    final void testWapServerConfig() {
        assertTrue(testConfGeneration); // Has been tested in @BeforeAll
    }

    /**
     * Tests public static getWapServerConfigFile()
     */
    @Test
    final void testGetWapServerConfigFile() {
        assertTrue(testConfGeneration); // Has been tested in @BeforeAll
    }

    /**
     * Tests public static setWapServerConfigFile(File configFile) which sets
     * private static WapServerConfig instance
     */
    @Test
    final void testSetWapServerConfigFile() {
        assertTrue(testConfGeneration); // Has been tested in @BeforeAll
    }

    /**
     * Tests public static boolean isConfigFileExistent()
     */
    @Test
    final void testIsConfigFileExistent() {
        assertTrue(testConfGeneration); // Has been tested in @BeforeAll
    }

    /**
     * Tests public static File createDefaultConfigurationFile()
     */
    @Test
    final void testCreateDefaultConfigurationFile() {
        assertTrue(testConfGeneration); // Has been tested in @BeforeAll
    }

    /**
     * Tests public static Properties getDefaultProperties()
     */
    @Test
    final void testGetDefaultProperties() {
        Properties actual;
        actual = null;
        actual = WapServerConfig.getDefaultProperties();
        assertNotNull(actual, "Default properties should not be null.");
    }

    /**
     * Tests public static void updateConfigFromOldVersions()
     */
    @Test
    final void testUpdateConfigFromOldVersions() {
        if (!testConfGeneration) {
            fail("Could not create test config, cannot maniuplate real one");
        }
        if (defaultConfFile.equals(WapServerConfig.getWapServerConfigFile())) {
            fail("Cannot maniuplate real one, no test config file selected");
        }
        // create an old version of the config and let it get updated
        byte[] beforeBytes = null;
        try {
            beforeBytes = Files.readAllBytes(WapServerConfig.getWapServerConfigFile().toPath());
        } catch (IOException e) {
            fail("Error reading config : " + e.getMessage());
        }
        try {
            Properties props = WapServerConfig.getDefaultProperties();
            // Test the case address + new wap ip
            props.put("server.address", "testaddress");
            storePropsInternal(props);
            assertTrue(WapServerConfig.updateConfigFromOldVersions());
            // Test the case address + no new wap ip
            props = WapServerConfig.getDefaultProperties();
            props.put("server.address", "testaddress");
            props.remove(ConfigurationKeys.WapIp.toString());
            storePropsInternal(props);
            assertTrue(WapServerConfig.updateConfigFromOldVersions());
            // Test the case port + new wap port
            props.put("server.port", "123");
            storePropsInternal(props);
            assertTrue(WapServerConfig.updateConfigFromOldVersions());
            // Test the case port + no new wap port
            props = WapServerConfig.getDefaultProperties();
            props.put("server.port", "123");
            props.remove(ConfigurationKeys.WapPort.toString());
            storePropsInternal(props);
            assertTrue(WapServerConfig.updateConfigFromOldVersions());
            props = WapServerConfig.getDefaultProperties();
            props.put(ConfigurationKeys.WebClientFolder.toString(), "WebContent"); // it will become small letter
            storePropsInternal(props);
            assertTrue(WapServerConfig.updateConfigFromOldVersions());
            props = WapServerConfig.getDefaultProperties();
            props.put("JsonLdValidator_SchemaFile", "xyz");
            storePropsInternal(props);
            assertTrue(WapServerConfig.updateConfigFromOldVersions());
            props = WapServerConfig.getDefaultProperties();
            props.put("EnableAnnotationValidation", "xyz");
            storePropsInternal(props);
            assertTrue(WapServerConfig.updateConfigFromOldVersions());
            props = WapServerConfig.getDefaultProperties();
            props.remove(ConfigurationKeys.SimpleFormatters.toString());
            storePropsInternal(props);
            assertTrue(WapServerConfig.updateConfigFromOldVersions());
        } catch (IOException | AssertionError e) {
            e.printStackTrace();
            fail("Error updating config : " + e.getMessage());
        }
        try {
            FileOutputStream out = new FileOutputStream(WapServerConfig.getWapServerConfigFile());
            out.write(beforeBytes);
            out.flush();
            out.close();
            assertFalse(WapServerConfig.updateConfigFromOldVersions());
        } catch (IOException e) {
            fail("Error resetting config : " + e.getMessage());
        }
    }

    private void storePropsInternal(Properties props) throws IOException {
        File file = WapServerConfig.getWapServerConfigFile();
        FileOutputStream out = new FileOutputStream(file);
        props.store(out, "reset");
        out.flush();
        out.close();
    }

    /**
     * Tests public static void applyHttpConfigBeforeSpringInit()
     */
    @Test
    final void testApplyHttpConfigBeforeSpringInit() {
        // we test with ssl, as this include the test for non ssl
        if (!testSsl) {
            fail("SSL Config could not be initialized in @BeforeAll");
        }
        Properties props = WapServerConfig.getDefaultProperties();
        props.setProperty(ConfigurationKeys.EnableHttps.toString(), "true");
        try {
            FileOutputStream out = new FileOutputStream(WapServerConfig.getWapServerConfigFile());
            props.store(out, "Default SSL Wap Server Configuration");
            out.flush();
            out.close();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        WapServerConfig.applyHttpConfigBeforeSpringInit();
        props.setProperty(ConfigurationKeys.EnableHttps.toString(), "false");
        try {
            FileOutputStream out = new FileOutputStream(WapServerConfig.getWapServerConfigFile());
            props.store(out, "Default Wap Server Configuration");
            out.flush();
            out.close();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests public static boolean checkConfig()
     */
    @Test
    final void testCheckConfig() {
        boolean actual;
        // test default
        actual = false;
        actual = WapServerConfig.checkConfig();
        assertTrue(actual, "Check on default WapServerConfig should be true");
        // DOTEST write the test for this method
        // test hostname == null
        // not possible to set hostname = null without reflections
    }

    /**
     * Tests public static WapServerConfig getInstance()
     */
    @Test
    final void testGetInstance() {
        WapServerConfig newOne = new WapServerConfig();
        WapServerConfig actual = WapServerConfig.getInstance();
        // We might have an actual one from spring tests run before, but it should never be the same
        if (actual != null) {
            if (actual == newOne) {
                fail("Instance updated during construction, this should not happen");
            }
        }
    }

    /**
     * Tests public boolean isConfigInitialized()
     */
    @Test
    final void testIsConfigInitialized() {
        boolean actual;
        // test default
        actual = false;
        actual = objWapServerConfig.isConfigInitialized();
        assertTrue(actual, "Config should be initialized at default WapServerConfig.");
    }

    /**
     * Tests public void updateConfig(Properties props)
     */
    @Test
    final void testUpdateConfig() {
        Properties paramProps;
        paramProps = null;
        paramProps = WapServerConfig.getDefaultProperties();
        assertNotNull(paramProps, "Could not set paramProps.");
        objWapServerConfig.updateConfig(paramProps);
    }

    /**
     * Tests public String getJsonLdValidatorSchemaFolder()
     */
    @Test
    final void testGetJsonLdValidatorSchemaFolder() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getJsonLdValidatorSchemaFolder();
        assertNotNull(actual, "Should not be null for default WapServerConfig.");
    }

    /**
     * Tests public boolean isValidationEnabled()
     */
    @Test
    final void testIsValidationEnabled() {
        boolean actual;
        actual = false;
        actual = objWapServerConfig.isValidationEnabled();
        assertTrue(actual, "Validation should be enabled by default WapServerConfig.");
    }

    /**
     * Tests public boolean isLabelMandatoryInContainers()
     */
    @Test
    final void testIsLabelMandatoryInContainers() {
        boolean actual;
        actual = true;
        actual = objWapServerConfig.isLabelMandatoryInContainers();
        assertFalse(actual, "Label mandatory in containers should not be enabled by default WapServerConfig.");
    }

    /**
     * Tests public boolean isSlugMandatoryInContainerPosts()
     */
    @Test
    final void testIsSlugMandatoryInContainerPosts() {
        boolean actual;
        actual = true;
        actual = objWapServerConfig.isSlugMandatoryInContainerPosts();
        assertFalse(actual, "Slug mandatory in container posts should not be enabled by default WapServerConfig.");
    }

    /**
     * Tests public boolean isContentNegotiationEnabled()
     */
    @Test
    final void testIsContentNegotiationEnabled() {
        boolean actual;
        actual = false;
        actual = objWapServerConfig.isContentNegotiationEnabled();
        assertTrue(actual, "Content Negotiation should be enabled by default WapServerConfig.");
    }

    /**
     * Tests public boolean isRootWapUrl(String url)
     */
    @Test
    final void testIsRootWapUrl() {
        String paramUrl;
        boolean actual;
        // test empty string
        paramUrl = "";
        actual = false;
        actual = objWapServerConfig.isRootWapUrl(paramUrl);
        assertFalse(actual, "Is Root WAP URL should be false for paramUrl: " + paramUrl);
        // test real root wap url
        paramUrl = objWapServerConfig.getBaseUrl() + "/wap/";
        actual = false;
        actual = objWapServerConfig.isRootWapUrl(paramUrl);
        assertTrue(actual, "Is Root WAP URL should be true for paramUrl: " + paramUrl);
    }

    /**
     * Tests public String getBaseUrl()
     */
    @Test
    final void testGetBaseUrl() {
        Properties paramProperties = WapServerConfig.getDefaultProperties();
        String actual;
        String expected;
        // test default
        actual = null;
        actual = objWapServerConfig.getBaseUrl();
        assertNotNull(actual, "Could not get base url from default WapServerConfig.");
        expected = "http://localhost:8080";
        assertEquals(expected, actual);
        // set wapPort == 80
        paramProperties.setProperty(ConfigurationKeys.WapPort.toString(), "80");
        objWapServerConfig.updateConfig(paramProperties);
        // test wapPort == 80
        actual = null;
        actual = objWapServerConfig.getBaseUrl();
        assertNotNull(actual, "Could not get base url from WapServerConfig after setting wapPort = 80.");
        expected = "http://localhost";
        assertEquals(expected, actual);
        // set enableHttps = true
        paramProperties.setProperty(ConfigurationKeys.EnableHttps.toString(), "true");
        objWapServerConfig.updateConfig(paramProperties);
        // test enableHttps = true
        actual = null;
        actual = objWapServerConfig.getBaseUrl();
        assertNotNull(actual, "Could not get base url from WapServerConfig after setting enableHttps = true.");
        expected = "https://localhost:80";
        assertEquals(expected, actual);
        // set wapPort == 443
        paramProperties.setProperty(ConfigurationKeys.WapPort.toString(), "443");
        objWapServerConfig.updateConfig(paramProperties);
        // test wapPort == 443
        actual = null;
        actual = objWapServerConfig.getBaseUrl();
        assertNotNull(actual, "Could not get base url from WapServerConfig after setting wapPort == 443.");
        expected = "https://localhost";
        assertEquals(expected, actual);
    }

    /**
     * Tests public double getDefaultQValue()
     */
    @Test
    final void testGetDefaultQValue() {
        double actual;
        actual = -1;
        actual = objWapServerConfig.getDefaultQValue();
        assertTrue(actual > 0.0, "Default Q value should be greater 0.");
        assertTrue(actual <= 1.0, "Default Q value should not be greater then 1.0.");
    }

    /**
     * Tests public String getJsonLdProfileFolder()
     */
    @Test
    final void testGetJsonLdProfileFolder() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getJsonLdProfileFolder();
        assertNotNull(actual, "Could not get JSON-LD profile folder for default WapServerConfig.");
    }

    /**
     * Tests public long getJsonLdCachedProfileValidityInMs()
     */
    @Test
    final void testGetJsonLdCachedProfileValidityInMs() {
        long actual;
        actual = -1L;
        actual = objWapServerConfig.getJsonLdCachedProfileValidityInMs();
        assertTrue(actual > 0L, "JSON-LD cached profile validity in ms should be greater 0L.");
    }

    /**
     * Tests public String getDataBasePath()
     */
    @Test
    final void testGetDataBasePath() {
        // we execute outside spring, but use the default settings. this means we have the production_db
        // since we never start jena, this is no problem per se, but triggers the "test MUST not use production DB"
        // check. Therefore we have to update the database path for this test to work
        Properties props = WapServerConfig.getDefaultProperties();
        props.setProperty(ConfigurationKeys.DataBasePath.toString(), "temp/testdb"); // anything with temp/ is ok
        objWapServerConfig.updateConfig(props);
        String actual;
        actual = null;
        actual = objWapServerConfig.getDataBasePath();
        assertNotNull(actual, "Could not get database path from default WapServerConfig.");
    }

    /**
     * Tests public String getRootContainerIri()
     */
    @Test
    final void testGetRootContainerIri() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getRootContainerIri();
        assertNotNull(actual, "Could not get root container IRI default WapServerConfig.");
    }

    /**
     * Tests public String getWebClientFolder()
     */
    @Test
    final void testGetWebClientFolder() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getWebClientFolder();
        assertNotNull(actual, "Could not get web client folder from default WapServerConfig.");
    }

    /**
     * Tests public String getJavaDocFolder()
     */
    @Test
    final void testGetJavaDocFolder() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getJavaDocFolder();
        assertNotNull(actual, "Could not get javadoc folder from default WapServerConfig.");
    }

    /**
     * Tests public String getJsonLdProfileFile()
     */
    @Test
    final void testGetJsonLdProfileFile() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getJsonLdProfileFile();
        assertNotNull(actual, "Could not get JSON-LD profile file string from default WapServerConfig.");
    }

    /**
     * Tests public boolean isFallbackValidationActive()
     */
    @Test
    final void testIsFallbackValidationActive() {
        boolean actual;
        actual = false;
        actual = objWapServerConfig.isFallbackValidationActive();
        assertTrue(actual, "Fallback validation should be active for default WapServerConfig.");
    }

    /**
     * Tests public void configurePathMatch(PathMatchConfigurer configurer)
     */
    @Test
    final void testConfigurePathMatchPathMatchConfigurer() {
        objWapServerConfig.configurePathMatch(new PathMatchConfigurer());
        // assert no error
    }

    /**
     * Tests public String getJsonLdFrameFolder()
     */
    @Test
    final void testGetJsonLdFrameFolder() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getJsonLdFrameFolder();
        assertNotNull(actual, "Could not get JSON-LD frame folder.");
    }

    /**
     * Tests public int getPageSize()
     */
    @Test
    final void testGetPageSize() {
        int actual;
        actual = -1;
        actual = objWapServerConfig.getPageSize();
        assertTrue(actual >= 0, "Page size should not be negative.");
    }

    /**
     * Tests public boolean isMultipleAnnotationPostAllowed()
     */
    @Test
    final void testIsMultipleAnnotationPostAllowed() {
        boolean actual;
        actual = false;
        actual = objWapServerConfig.isMultipleAnnotationPostAllowed();
        assertTrue(actual, "Multiple annotation post should be allowed by default WapServerConfig.");
    }

    /**
     * Tests public boolean shouldAlwaysAddDefaultProfilesToJsonLdRequests()
     */
    @Test
    final void testShouldAlwaysAddDefaultProfilesToJsonLdRequests() {
        boolean actual;
        actual = true;
        actual = objWapServerConfig.shouldAlwaysAddDefaultProfilesToJsonLdRequests();
        assertFalse(actual,
                "Always add default profiles to JSON-LD requests should not be allowed by default WapServerConfig.");
    }

    /**
     * Tests public boolean shouldAppendStackTraceToErrorMessages()
     */
    @Test
    final void testShouldAppendStackTraceToErrorMessages() {
        boolean actual;
        actual = true;
        actual = objWapServerConfig.shouldAppendStackTraceToErrorMessages();
        assertFalse(actual, "Append stack trace to error messages should not be allowed by default WapServerConfig.");
    }

    /**
     * Tests public int getMaxNumberOfStackTraceElementsToInclude()
     */
    @Test
    final void testGetMaxNumberOfStackTraceElementsToInclude() {
        int actual;
        actual = -1;
        actual = objWapServerConfig.getMaxNumberOfStackTraceElementsToInclude();
        assertTrue(actual >= 0, "Max number of stack trace elements to include should not be negative.");
    }

    /**
     * Tests public String getHostname()
     */
    @Test
    final void testGetHostname() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getHostname();
        assertNotNull(actual, "Could not get hostname from default WapServerConfig.");
    }

    /**
     * Tests public int getWapPort()
     */
    @Test
    final void testGetWapPort() {
        int actual;
        actual = -1;
        actual = objWapServerConfig.getWapPort();
        assertTrue(actual >= 0, "Wap port should not be negative.");
    }

    /**
     * Tests public String getWapIp()
     */
    @Test
    final void testGetWapIp() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getWapIp();
        assertEquals("*", actual);
    }

    /**
     * Tests public int getSparqlReadPort()
     */
    @Test
    final void testGetSparqlReadPort() {
        int actual;
        actual = -1;
        actual = objWapServerConfig.getSparqlReadPort();
        assertTrue(actual >= 0, "SPARQL read port should not be negative.");
    }

    /**
     * Tests public String getSparqlReadIp()
     */
    @Test
    final void testGetSparqlReadIp() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getSparqlReadIp();
        assertNotNull(actual, "Could not get SPARQL read IP from default WapServerConfig.");
    }

    /**
     * Tests public int getSparqlWritePort()
     */
    @Test
    final void testGetSparqlWritePort() {
        int actual;
        actual = -1;
        actual = objWapServerConfig.getSparqlWritePort();
        assertTrue(actual >= 0, "SPARQL write port should not be negative.");
    }

    /**
     * Tests public String getSparqlWriteIp()
     */
    @Test
    final void testGetSparqlWriteIp() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getSparqlWriteIp();
        assertNotNull(actual, "Could not get SPARQL write IP from default WapServerConfig.");
    }

    /**
     * public boolean isHttpsEnabled()
     */
    @Test
    final void testIsHttpsEnabled() {
        boolean actual;
        actual = true;
        actual = objWapServerConfig.isHttpsEnabled();
        assertFalse(actual, "HTTPS should not be enabled by default WapServerConfig.");
    }

    /**
     * public String getSimpleFormatters()
     */
    @Test
    final void testGetSimpleFormatters() {
        String actual;
        actual = null;
        actual = objWapServerConfig.getSimpleFormatters();
        assertNotNull(actual, "Could not get simple formatters string from default WapServerConfig.");
    }
}
