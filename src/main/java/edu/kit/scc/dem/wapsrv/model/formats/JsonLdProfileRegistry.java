package edu.kit.scc.dem.wapsrv.model.formats;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;

/**
 * Central registry for JSON-LD profiles. It manages a local in memory profile
 * cache. These profiles will be downloaded (on first access and them then on
 * durably) and kept up to date A list of known JSON-LD Contexts can be found at
 * https://github.com/json-ld/json-ld.org/wiki/existing-contexts
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@Component
public final class JsonLdProfileRegistry {

    /**
     * Via this key in System.setProperty() can the update be disabled (before
     * startup) The value is of no interest, as soon as the key exists, the
     * updater is not started
     */
    public static final String DISABLED_UPDATER_PROPERTY = "DISABLE_JSONLD_PROFILE_UPDATER";
    /**
     * The max size of profiles to download
     */
    private static final int MAX_DOWNLOAD_SIZE = 1 * 1024 * 1024; // 1 MB , we might choose smaller
    /**
     * The folder that is searched for local profile copies
     */
    private File profileFolder;
    /**
     * On application startup and after this time delay all profiles will be
     * renewed
     */
    private long profileCacheValidityInMs;
    /**
     * The profile registry file to read for profile definitions
     */
    private File profilesXmlFile;
    /**
     * It true profiles that have once been successfully downloaded are kept
     * valid even if their validity has expired
     */
    private boolean keepExpiredProfiles = true;
    /**
     * The profile database in memory
     */
    private final Properties profilesDatabase = new Properties();
    /**
     * JsonLdOptions object used, contains all parsed profiles as local in
     * memory copy
     */
    private JsonLdOptions jsonLdOptions = new JsonLdOptions();
    /**
     * The set of profiles loaded and cached. This set is dynamically populated
     * on first usage.
     */
    private final Set<URI> cachedProfiles = new HashSet<>();
    /**
     * The last times individual profiles have been updated
     */
    private final Map<URI, Long> lastUpdateTimes = new Hashtable<>();
    /**
     * The number individual profiles have had update failures
     */
    private final Map<URI, Integer> profile2failures = new Hashtable<>();
    /**
     * The last times individual profiles have had update failures
     */
    private final Map<URI, Long> profile2failureTimes = new Hashtable<>();
    /**
     * The frames needed for specific types
     */
    private final Map<Type, String> type2frame = new Hashtable<Type, String>();
    /**
     * The logger to use
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * Lock needed for updates
     */
    private final Object updateLock = new Object();
    /**
     * Variable telling the update thread to terminate
     */
    private boolean terminateUpdater = false;
    /**
     * The helper thread updating the database
     */
    private Thread updater;
    /**
     * Will be set to true if asynchronous initialization is done
     */
    private boolean isInitialized = false;

    /**
     * Reads in a file into a string
     *
     * @param file The file to read
     * @return String content of the file
     */
    private static String readFile(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Initialiazes the registry without usage of spring (just for testing)
     *
     * @param config The WapServerConfig to extract configuration from
     * @param keepExpiredProfiles Keep profiles or not
     */
    public void init(WapServerConfig config, boolean keepExpiredProfiles) {
        this.keepExpiredProfiles = keepExpiredProfiles;
        init(config);
    }

    /**
     * Initializes the JsonLdProfileRegistry
     *
     * @param config The WapServerConfig to extract configuration from
     */
    @Autowired
    public void init(WapServerConfig config) {
        profileFolder = new File(config.getJsonLdProfileFolder());
        profileCacheValidityInMs = config.getJsonLdCachedProfileValidityInMs();
        profilesXmlFile = new File(profileFolder, config.getJsonLdProfileFile());
        if (!profilesXmlFile.exists() && !createDefaultPropertiesFile()) {
            throw new RuntimeException("Could not create profile database file, exiting");
        }
        // Read in the data base
        readProfileDatabase();
        // On the very first start, we assure that the two mandatory profiles anno and ldp exist
        boolean inserted = false;
        if (!profilesDatabase.containsKey("anno.jsonld")) {
            profilesDatabase.put("anno.jsonld", "http://www.w3.org/ns/anno.jsonld");
            inserted = true;
        }
        if (!profilesDatabase.containsKey("ldp.jsonld")) {
            profilesDatabase.put("ldp.jsonld", "http://www.w3.org/ns/ldp.jsonld");
            inserted = true;
        }
        if (inserted) {
            try {
                saveDatabase();
            } catch (IOException e) {
                // If this fails, no problem. On next restart, we add them again
            }
        }
        // Updater updates the profiles in the future
        updater = new Thread() {
            public void run() {
                while (true) {
                    try {
                        // Can be improved if we calculate next download time,
                        // since wake-up every 30 sec does not cost too much
                        sleep(Math.min(30000, profileCacheValidityInMs));
                    } catch (InterruptedException e) {
                        // This should not happen except on context closing, but is not a problem anyway.
                    }
                    synchronized (updateLock) {
                        if (terminateUpdater) {
                            break;
                        }
                        update();
                        // Wakes up eventually waiting threads.
                        updateLock.notifyAll();
                        // This can only happen during a race condition on application startup.
                        // But notifying nobody is a noop,
                    }
                }
            }
        };
        updater.setDaemon(true);
        if (System.getProperty(DISABLED_UPDATER_PROPERTY) == null) {
            updater.start();
        }
        // The first update, we issue ourself
        synchronized (updateLock) {
            update();
            // Wakes up eventually waiting threads.
            updateLock.notifyAll();
        }
        // Disable remote context loading for JSON-LD Java makes request to URLs even when using complete
        // expanded strings with all contexts expanded. This is buggy behavior which might some time in the future be
        // fixed by JSON-LD Java people. Until then, disable it here as every peace of context that may be needed will be
        // downloaded from our code and either embedded or expanded, depending on what is needed
        
        //Addition 27.02.2023: Due to switching from http to https by w3c, JSON-LD formatting fails if context reloading is disabled. 
        //The only way to fix this was to re-enable context loading for the moment. 
        System.setProperty(DocumentLoader.DISALLOW_REMOTE_CONTEXT_LOADING, "false");
    }

    /**
     * Can be called externally to issue an update. Under normal circumstances
     * never needed, just for testing purposes
     */
    protected void update() {
        synchronized (updateLock) {
            updateProfiles();
            if (!isInitialized) {
                // On the first round, we set it to true
                isInitialized = true;
            }
        }
    }

    /**
     * Updates the registered profiles if their cache validity has expired or
     * they have not been downloaded at all (on app startup or if not reachable
     * before)
     */
    private void updateProfiles() {
        final JsonLdOptions jsonLdOptions = new JsonLdOptions();
        final long now = System.currentTimeMillis();
        boolean somethingChanged = false;
        // Walk through the in memory property database
        for (Object key : profilesDatabase.keySet()) {
            final String filename = (String) key;
            final String urlString = profilesDatabase.getProperty(filename);
            URI url = null;
            try {
                url = new URI(urlString);
            } catch (URISyntaxException e) {
                logger.warn("Invalid profile url, skipping it : " + urlString);
                continue;
            }
            // Now the url is given, check if cache is still valid
            if (lastUpdateTimes.containsKey(url)) {
                long lastTime = lastUpdateTimes.get(url);
                if (now - lastTime < profileCacheValidityInMs) {
                    // still valid
                    continue;
                }
            }
            // check retry delayer
            if (profile2failures.containsKey(url)) {
                int failureCount = profile2failures.get(url);
                long lastFailureTime = profile2failureTimes.get(url);
                long retryDelay = 5 * 60 * 1000; // 5 mins base
                // we scale it with the amount of failures before, limiting it
                // to 2^8 = 256 * 5mins = about a day
                retryDelay *= Math.pow(2, Math.min(8, failureCount));
                // System.out.println("retry of " + url + " delayed by " + retryDelay + " ms");
                if (now - lastFailureTime < retryDelay) {
                    // retry postponed because of consecutive errors
                    // System.out.println("retry postponed");
                    continue;
                }
                // System.out.println("retry is now due");
            }
            // Either not downloaded yet or validity has expired
            File profileFile = new File(profileFolder, filename);
            // General comment on variable updates : if we change global variables here and do not enforce blocking in the
            // public methods that rely on them, we "risk" inconsistencies as we might say "cached" but do not have an
            // updated options file. We regard this not so bad because if we could cache the profile here the external
            // caller would just download it again if not already in the options. This should in almost all cases be the
            // faster solution to waiting until dozens of profiles have been updated that might eventually exist in the
            // profile database. We might even try to download a profile here that is not accessible and this involves long
            // timeouts. This is even more problematic compared to dual downloading...
            if (!download(profileFile, url)) {
                // register failure
                Integer failures = profile2failures.get(url);
                if (failures == null) {
                    failures = 0;
                } else {
                    failures++;
                }
                profile2failures.put(url, failures);
                profile2failureTimes.put(url, System.currentTimeMillis());
                // Download replaces it only if successful
                if (!profileFile.exists()) {
                    // File has not existed before downloading, nothing to do so far
                    // as it is not registered anywhere relevant.
                    logger.info("Could not download profile, retrying later : " + urlString);
                } else {
                    // File has been downloaded before, now the behavior diverges.
                    if (keepExpiredProfiles) {
                        logger.info("Could not download profile, keeping expired one");
                        if (!cachedProfiles.contains(url)) {
                            // if not alreay registered in cached profiles, do this now with the "expired" one
                            cachedProfiles.add(url);
                            somethingChanged = true; // we need to update the jsonld options with the expired local one
                            // This way the client requesting if gets an answer at once (using the expired one)
                            // and only the downloader in background tries to update it
                        }
                    } else {
                        logger.info("Could not download profile, removing from cache");
                        // Does nothing if not known there
                        lastUpdateTimes.remove(url);
                        // Does nothing if not known there
                        cachedProfiles.remove(url);
                        // This might not succeed, but this is not a problem.
                        profileFile.delete();
                        somethingChanged = true;
                    }
                }
            } else {
                // Successful download
                profile2failures.remove(url);
                profile2failureTimes.remove(url);
                lastUpdateTimes.put(url, now);
                cachedProfiles.add(url);
                logger.info("Successfully updated profile : " + profileFile.getName());
                somethingChanged = true;
            }
        }
        if (!somethingChanged) {
            return;
        }
        // This call incorporates all profiles marked valid into the options object
        updateJsonLdOptions(jsonLdOptions);
        // Now set these options to be the new ones
        setJsonLdOptions(jsonLdOptions);
    }

    private boolean download(File destinationFile, URI srcUri) {
        try {
            URL srcUrl = srcUri.toURL();
            Proxy proxy = getProxy(srcUrl);
            HttpURLConnection httpConn
                    = (HttpURLConnection) (proxy == null ? srcUrl.openConnection() : srcUrl.openConnection(proxy));
            httpConn.setReadTimeout(5000);
            // 5s is more then enough. Many of the profiles deliver content depending on accept header.
            // Download XML files or the default format of the server. Set accept JSON-LD
            httpConn.setRequestProperty("Accept", "application/ld+json");
            int responseCode = httpConn.getResponseCode();
            /* System.out.println("STATUS " + responseCode);
            System.out.println("HEADER " + httpConn.getHeaderField("Location"));
             */
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                URL newSourceUrl = URI.create(httpConn.getHeaderField("Location")).toURL();
                logger.info("Received HTTP 301 in first attempt. Trying new location {}.", newSourceUrl);
                //new attempt
                proxy = getProxy(newSourceUrl);
                httpConn
                        = (HttpURLConnection) (proxy == null ? newSourceUrl.openConnection() : newSourceUrl.openConnection(proxy));
                httpConn.setReadTimeout(5000);
                // 5s is more then enough. Many of the profiles deliver content depending on accept header.
                // Download XML files or the default format of the server. Set accept JSON-LD
                httpConn.setRequestProperty("Accept", "application/ld+json");
                responseCode = httpConn.getResponseCode();
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                long length = httpConn.getContentLengthLong();
                if (length > MAX_DOWNLOAD_SIZE) {
                    // Skip too big ones to prevent some sort of attack on the application
                    logger.warn("Refused to download too big profile : size=" + length + " bytes : " + srcUrl.toString());
                    return false;
                }
                // opens input stream from the HTTP connection
                InputStream inputStream = httpConn.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int bytesRead = -1;
                // 4KB might be OK
                byte[] buffer = new byte[4096];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                inputStream.close();
                httpConn.disconnect();
                // Download successful, now store to file
                FileOutputStream fOut = new FileOutputStream(destinationFile);
                fOut.write(out.toByteArray());
                fOut.flush();
                fOut.close();
                return true;
            } else {
                logger.warn("No file to download. Server replied HTTP code: " + responseCode);
                httpConn.disconnect();
                return false;
            }
        } catch (IOException ex) {
            logger.warn("could not download profile : " + ex.getMessage());
            return false;
        }
    }

    private Proxy getProxy(URL srcUrl) {
        if (srcUrl == null) {
            return null;
        }
        if (srcUrl.getProtocol().equalsIgnoreCase("http")) {
            String proxy = System.getProperty("http.proxyHost");
            String port = System.getProperty("http.proxyPort");
            if (proxy == null || port == null) {
                return null;
            }
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy, Integer.parseInt(port)));
        } else if (srcUrl.getProtocol().equalsIgnoreCase("https")) {
            String proxy = System.getProperty("https.proxyHost");
            String port = System.getProperty("https.proxyPort");
            if (proxy == null || port == null) {
                return null;
            }
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy, Integer.parseInt(port)));
        } else {
            return null;
        }
    }

    /**
     * Determine whether a profile is cached or not
     *
     * @param url The URL to test
     * @return True if cached, false otherwise
     */
    public boolean isCachedProfile(URI url) {
        blockUntilInitialized();
        // always check for http and https. The file is the same, no need to distinguish
        URI complementaryUrl = toComplementaryUrl(url);
        return cachedProfiles.contains(url) || cachedProfiles.contains(complementaryUrl);
    }

    private void blockUntilInitialized() {
        // On application start the cached profiles might not be populated. In this case: acquire the lock on updateLock.
        // Not acquire if the object already exists or need to wait quite some time if an actual update is in progress
        if (!isInitialized) {
            synchronized (updateLock) {
                // Verify whether it is still not initialized. In that case wait.
                while (!isInitialized) {
                    try {
                        updateLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        } else {
            // just to clarify that the usual situation will be isInitialized ==> no lock acquiring needed
        }
    }

    /**
     * This method is called to create a new pofiles.xml File with the mandatory
     * anno.jsonld Profile. If not found on startup. The anno.jsonld has to be
     * downloaded and provided separately. http://www.w3.org/ns/anno.jsonld
     *
     * @return True if created, false otherwise
     */
    private boolean createDefaultPropertiesFile() {
        try {
            if (!profileFolder.exists() && !profileFolder.mkdirs()) {
                return false;
            }
            Properties props = new Properties();
            props.put("anno.jsonld", "https://www.w3.org/ns/anno.jsonld");
            props.put("ldp.jsonld", "https://www.w3.org/ns/ldp.jsonld");
            saveDatabase(props);
            return true;
        } catch (IOException e) {
            logger.error("Could not create default profiles.xml :" + e.getMessage());
            return false;
        }
    }

    /**
     * Deinitializes the JsonLdProfileRegistry
     */
    @EventListener(ContextClosedEvent.class)
    public void deinit() {
        logger.info("Shutting profile update thread");
        synchronized (updateLock) {
            terminateUpdater = true;
            updater.interrupt();
        }
        try {
            updater.join();
            logger.info("Updater Thread shut down");
        } catch (InterruptedException e) {
            logger.info("Updater Thread NOT shut down : " + e.getMessage());
        }
    }

    /**
     * Gets the JsonLDOptions containing all cached profiles
     *
     * @return The jsonLdOptions containing all cached profiles
     */
    public JsonLdOptions getJsonLdOptions() {
        blockUntilInitialized();
        synchronized (this) {
            return jsonLdOptions;
        }
    }

    private synchronized void setJsonLdOptions(JsonLdOptions jsonLdOptions) {
        this.jsonLdOptions = jsonLdOptions;
    }

    /**
     * Is used for application startup - first time reading in the profiles to
     * cache
     */
    private void readProfileDatabase() {
        // Read local profile cache from XML
        try {
            FileInputStream in = new FileInputStream(profilesXmlFile);
            profilesDatabase.loadFromXML(in);
            in.close();
        } catch (IOException e) {
            logger.error("Error parsing profiles.xml :" + e.getMessage());
        }
    }

    /**
     * Saves the actual in memory profile database
     *
     * @throws IOException In case any I/O related problems occur
     */
    private void saveDatabase() throws IOException {
        saveDatabase(profilesDatabase);
    }

    /**
     * Save a given properties file to the database file location
     *
     * @param props The properties to save
     * @throws IOException In case any I/O related problems occur
     */
    private void saveDatabase(Properties props) throws IOException {
        FileOutputStream out = new FileOutputStream(profilesXmlFile);
        props.storeToXML(out, "Registered JSON-LD Profiles - Usage: key=filename, value=URL used in context");
        out.flush();
        out.close();
    }

    private void updateJsonLdOptions(JsonLdOptions jsonLdOptions) {
        // Inject a context document into the options as a literal string
        DocumentLoader dl = new DocumentLoader();
        jsonLdOptions.setDocumentLoader(dl);
        // Load cached profiles
        for (Object filenameObject : profilesDatabase.keySet()) {
            final String filename = (String) filenameObject;
            final String urlString = profilesDatabase.getProperty(filename);
            URI url = null;
            try {
                url = new URI(urlString);
            } catch (URISyntaxException e) {
                logger.warn("Invalid profile url, skipping it : " + urlString);
                continue;
            }
            if (!cachedProfiles.contains(url)) {
                // not a valid profile
                continue;
            }
            File profileFile = new File(profileFolder, filename);
            if (!profileFile.exists()) {
                logger.error("Local profile " + profileFile.getName() + " does not exist despite beeing regarded valid");
                continue;
            }
            if (!profileFile.canRead()) {
                logger.error("Local (valid) profile " + profileFile.getName() + " cannot be read");
                continue;
            }
            String jsonContext = readFile(profileFile);
            if (jsonContext == null) {
                logger.error("Error loading local profile : " + profileFile.getName());
            } else {
                try {
                    dl.addInjectedDoc(urlString, jsonContext);
                    String complementaryUrlString = toComplementaryUrl(urlString);
                    dl.addInjectedDoc(complementaryUrlString, jsonContext);
                    logger.info("Successfully loaded local profile : " + profileFile.getName());
                } catch (JsonLdError e) {
                    logger.error("Error loading local profile : " + profileFile.getName() + " - " + e.getMessage());
                }
            }
        }
    }

    /**
     * Converts http to https and vice versa
     *
     * @param urlString The url to convert
     * @return The converted url
     */
    private String toComplementaryUrl(String urlString) {
        if (urlString == null) {
            return null;
        }
        if (urlString.toLowerCase().startsWith("http:")) {
            return "https:" + urlString.substring(5);
        } else if (urlString.toLowerCase().startsWith("https:")) {
            return "http:" + urlString.substring(6);
        } else {
            throw new InternalServerException("invalid URL");
        }
    }

    /**
     * Converts http to https and vice versa
     *
     * @param url The url to convert
     * @return The converted url
     */
    private URI toComplementaryUrl(URI url) {
        if (url == null) {
            return null;
        }
        String compString = toComplementaryUrl(url.toString());
        try {
            return new URI(compString);
        } catch (URISyntaxException e) {
            // should not happen
            logger.warn("could not complement url, this should not happen : " + url);
            // return the unswitched url as fallback
            return url;
        }
    }

    /**
     * Caches the profile at the given URL. If already cached, just return. If
     * not, try to download.<br>
     * If download does not succeed within a reasonable time frame, this method
     * returns without caching the profile.
     *
     * @param url The URL to cache
     * @return True if already cached or caching was successful, false otherwise
     */
    public boolean cacheProfile(URI url) {
        if (url == null) {
            return false;
        }
        blockUntilInitialized();
        synchronized (cachedProfiles) {
            for (URI urlRegistered : cachedProfiles) {
                if (url.equals(urlRegistered)) {
                    return true;
                }
                URI complementaryUrl = toComplementaryUrl(urlRegistered);
                if (url.equals(complementaryUrl)) {
                    return true;
                }
            }
        }
        // If not in cache, we have to lock the update lock, which might in worst case scenario
        // be exactly when a new update cache round is active and involve significant delay here.
        // This is not very common (updates more than once a day might not make sense) and is
        // therefore acceptable.
        synchronized (updateLock) {
            final String urlString = url.toString();
            final File profileFile = new File(profileFolder, getFilename(url));
            URI urlHttp = null;
            URI urlHttps = null;
            if (url.toString().toLowerCase().startsWith("https:")) {
                urlHttps = url;
                urlHttp = toComplementaryUrl(url);
            } else {
                urlHttp = url;
                urlHttps = toComplementaryUrl(url);
            }
            // First try the https one, then http
            if (download(profileFile, urlHttps) || download(profileFile, urlHttp)) {
                // Download OK, now register it in profile database if not already there. This might be the case if it is
                // known but could not be downloaded until now.
                if (!profilesDatabase.containsKey(profileFile)) {
                    profilesDatabase.put(profileFile.getName(), urlString);
                    try {
                        saveDatabase();
                    } catch (IOException e) {
                        // Should never happen
                        profilesDatabase.remove(profileFile);
                        return false;
                    }
                }
                // Now it is registered in the data base, create the keys necessary
                lastUpdateTimes.put(url, System.currentTimeMillis());
                cachedProfiles.add(url);
                // This call incorporates all profiles marked valid into the options object
                updateJsonLdOptions(jsonLdOptions);
                // now set these options to be the new ones
                setJsonLdOptions(jsonLdOptions);
                return true;
            } else {
                // could not download
                return false;
            }
        }
    }

    private String getFilename(URI url) {
        // We use the path part and replace / with _
        return url.getPath().replaceAll(Pattern.quote("/"), "_");
    }

    /**
     * Returns the last update time of a given profile url. Mainly needed during
     * testing.
     *
     * @param url The url to get the update time from
     * @return the last update time, -1 if never updated (=not cached yet)
     */
    protected long getLastUpdateTime(URI url) {
        blockUntilInitialized();
        Long lastTime = lastUpdateTimes.get(url);
        return lastTime == null ? -1 : lastTime;
    }

    /**
     * Gets the JSON-LD Frame needed for serialization of the given type of
     * object
     *
     * @param type The object type
     * @return The frame needed for serialization
     */
    public String getFrameString(Type type) {
        if (!type2frame.containsKey(type)) {
            // load on first access
            File frameFile = new File(WapServerConfig.getInstance().getJsonLdFrameFolder(), "FRAME_" + type.toString() + ".jsonld");
            String frame = readFile(frameFile);
            if (frame == null) {
                logger.warn("Could not load JSON-LD Frame file for " + type);
                throw new InternalServerException("Could not load JSON-LD Frame file for " + type);
            } else {
                // We have to embed them on every startup again. If the profile changes,
                // these changes will be held up to date in the frame with this behavior
                frame = embedContexts(frame);
                type2frame.put(type, frame);
                return frame;
            }
        } else {
            return type2frame.get(type);
        }
    }

    /**
     * Embeds the used contexts into the given JSON-LD serialized string
     *
     * @param serialization The JSON-LD string
     * @return The same string with the referenced contexts embedded
     */
    private String embedContexts(String serialization) {
        // Find a more general way to do that. For now this only works 100%
        // on the FRAME Files delivered with the project. There is only one occurrence
        // of the URL that could be replaced. And we know the contexts beforehand and
        // and their names
        String annoContext = readFile(new File(profileFolder, "anno.jsonld"));
        String ldpContext = readFile(new File(profileFolder, "ldp.jsonld"));
        boolean annoContextExists = serialization.indexOf("\"https://www.w3.org/ns/anno.jsonld\"") > 0;
        boolean ldpContextExists = serialization.indexOf("\"https://www.w3.org/ns/ldp.jsonld\"") > 0;
        if (annoContextExists && ldpContextExists) {
            // The context files look like
            // 1 {
            // 2 "@context": {
            // .....
            // 2 }
            // 1 }
            // The String to replace like this
            // "@context": ["http://www.w3.org/ns/xxx.jsonld","http://www.w3.org/ns/xxx.jsonld"],
            // ==> remove the everything before second { in the context file (directly after the : following
            // @context) remove the last } put that as replacement for the context urls including the "
            annoContext = annoContext.substring(annoContext.indexOf("{", 3), annoContext.lastIndexOf("}"));
            ldpContext = ldpContext.substring(ldpContext.indexOf("{", 3), ldpContext.lastIndexOf("}"));
            return serialization.replaceAll(Pattern.quote("\"https://www.w3.org/ns/anno.jsonld\""), annoContext)
                    .replaceAll(Pattern.quote("\"https://www.w3.org/ns/ldp.jsonld\""), ldpContext);
        } else { // only one occurs, and must always be there
            // The context file looks like
            // 1 {
            // 2 "@context": {
            // .....
            // 2 }
            // 1 }
            // The String to replace like this "@context": "http://www.w3.org/ns/xxx.jsonld",
            // ==> remove the everything before second { in the context file (directly after the : following
            // @context) remove the last } put that as replacement for the context url including the "
            String contextString = annoContextExists ? annoContext : ldpContext;
            contextString = contextString.substring(contextString.indexOf("{", 3), contextString.lastIndexOf("}"));
            if (annoContextExists) {
                return serialization.replaceAll(Pattern.quote("\"https://www.w3.org/ns/anno.jsonld\""), contextString);
            } else {
                return serialization.replaceAll(Pattern.quote("\"https://www.w3.org/ns/ldp.jsonld\""), contextString);
            }
        }
    }

    /**
     * Expands a given JSON-LD serialization using cached profiles
     *
     * @param serialization The serialized JSON-LD to expand its profiles
     * @return The expanded and again serialized JSON-LD
     * @throws FormatException if anything went wrong expanding JSON-LD profiles
     */
    public String expandJsonLd(String serialization) throws FormatException {
        try {
            Object jsonObject = JsonUtils.fromString(serialization);
            jsonObject = JsonLdProcessor.expand(jsonObject, getJsonLdOptions());
            String erg = JsonUtils.toPrettyString(jsonObject);
            return erg;
        } catch (IOException | JsonLdError e) {
            throw new FormatException("Exception expanding JSON-LD profiles : " + e.getMessage());
        }
    }
}
