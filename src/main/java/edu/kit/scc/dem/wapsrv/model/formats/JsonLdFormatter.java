package edu.kit.scc.dem.wapsrv.model.formats;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.github.jsonldjava.core.JsonLdConsts;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.AnnotationList;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;

/**
 * JSON-LD Formatter that can be used for JSON-LD Requests. It parses profiles
 * from Accept and Content-Type strings and applies them to the serialized
 * output in {@link #format(FormattableObject)}.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@Component
public final class JsonLdFormatter extends AbstractFormatter {

    /**
     * The default JSON-LD Profile
     */
    public static final URI DEFAULT_PROFILE = makeUri("http://www.w3.org/ns/anno.jsonld");
    /**
     * The LDP profile for containers
     */
    public static final URI LDP_PROFILE = makeUri("http://www.w3.org/ns/ldp.jsonld");
    /**
     * The string identifying JSON-LD
     */
    public static final String JSON_LD_STRING = "application/ld+json";
    /**
     * The set of used profiles
     */
    private final Set<URI> profiles = new HashSet<>();
    /**
     * The profile registry
     */
    private JsonLdProfileRegistry profileRegistry;
    /**
     * The logger to use
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Creates a new JsonLdFormatter instance
     */
    public JsonLdFormatter() {
        super(Format.JSON_LD);
    }

    /**
     * This is a helper method necessary to be able to use a static URL,
     * otherwise the exception can not be caught.
     *
     * @param urlString The String to generate a URL from
     * @return The generated URL Object
     */
    private static URI makeUri(String urlString) {
        try {
            return new URI(urlString);
        } catch (URISyntaxException e) {
            LoggerFactory.getLogger(ContentNegotiator.class).error("DEFAULT_PROFILE is an invalid URL : " + urlString);
            return null;
        }
    }

    /**
     * Sets the profile registry to use
     *
     * @param profileRegistry The profile registry
     */
    @Autowired
    public void setProfileRegistry(JsonLdProfileRegistry profileRegistry) {
        this.profileRegistry = profileRegistry;
    }

    /**
     * Overloaded method to format annotation lists Single annotations are
     * formatted as if they were annotations directly, multiple ones have their
     * own special array serialization
     *
     * @param annoList The list to format
     * @return String serialization of the annotation list
     */
    public String format(AnnotationList annoList) {
        if (annoList.size() == 1) {
            return format(annoList.getAnnotations().get(0));
        }
        // Serialize lists differently
        StringBuilder builder = new StringBuilder();
        builder.append("[\n");
        List<Annotation> annotations = annoList.getAnnotations();
        for (int n = 0; n < annotations.size(); n++) {
            Annotation annotation = annotations.get(n);
            builder.append(format(annotation));
            if (n < annotations.size() - 1) {
                builder.append(",\n");
            } else {
                builder.append("\n");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public String format(FormattableObject obj) {
        final String jsonldString = obj.toString(Format.JSON_LD);
        // Get the frame as JSON-LD string
        final String frameString = profileRegistry.getFrameString(obj.getType());
        final String jsonLdWithBlankNodeIds = applyProfiles(jsonldString, frameString);
        // Details why this step is necessary is found within the method
        return removeBlankNodeIds(jsonLdWithBlankNodeIds);
    }

    /**
     * Remove all blank node IDs from a given "pretty" JSON-LD String
     *
     * @return The same JSON-LD without blank node IDs
     */
    private String removeBlankNodeIds(String jsonLdWithBlankNodeIds) {
        // Whenever we add something that needs an IRI but has none
        // (more formally that is going to be used as an RDF subject)
        // to the triple store, it gets a new and umambiguously blank node IRI assigned.
        // these iris start with _: which is and invalid URI and can easily be recognized.
        // A jena one might look like that : _:b023497987982374982379842987
        // the problem with JSON-LD java is that it prunes these blank node iris to something
        // better readable for humans like _:b0. This retains their unambiguity within the
        // single JSON-LD String, but not globally. Whenever we try to put annotations with these
        // blank node ids, we get two kind of error messages: first those are invalid iris too,
        // not only invalid uris ==> Schema validation fails. Second jena assigns a new random
        // blank node IRI for those which has unwanted side effects, leading to rejection sometimes
        // because unallowed property changes occur.
        // The later has been solved by not copying autocreated blank node iris to via.
        // The former can not be solved easily and leads to the fact that all annotations with
        // complex bodies/target (that are indisputably all of real value and usage) cannot be
        // updated by put requests. JSON-LD-Java has the option to disable this behavior by
        // setting setPruneBlankNodeIdentifiers(false) to given JsonLdOptions.
        // But, as another bug of this "reference implementation", this is ignored or not
        // completely implemented yet. Until this bug is removed from JSON-LD-Java, we have to work
        // around it by removing all blank node IDs manually from a well formatted pretty string
        StringBuilder builder = new StringBuilder();
        String[] lines = jsonLdWithBlankNodeIds.split(Pattern.quote("\n"));
        for (String line : lines) {
            if (line.trim().startsWith("\"id\"")) {
                if (line.contains("\"_:b")) {
                    continue; // skip this unneeded line
                } else {
                    builder.append(line);
                }
            } else {
                builder.append(line);
            }
            // we append a new line if not the last }
            // update. We cut the last \n if not needed from the builder. Thats faster
            builder.append("\n");
        }
        if (builder.charAt(builder.length() - 1) == '\n') {
            return builder.substring(0, builder.length() - 1);
        } else {
            return builder.toString();
        }
    }

    /**
     * Expands all listed profiles in a given JSON-LD string
     *
     * @param jsonLd The string to expand
     * @param frameString The JSON-LD Frame as String
     * @return the expanded string
     */
    private String applyProfiles(String jsonLd, String frameString) {
        try {

            final Object frameObject = frameString == null ? null : JsonUtils.fromString(frameString);
            Object jsonObject = JsonUtils.fromString(jsonLd);
            // Use precreated options with in memory profiles
            JsonLdOptions optionsWithContexts = profileRegistry.getJsonLdOptions();

            List<Object> contexts = new ArrayList<>();

            //We assume that framing and compacting only needs to be applied if anno profile is present
            if (frameObject != null && profiles.contains(DEFAULT_PROFILE)) {
                final JsonLdOptions options = profileRegistry.getJsonLdOptions();
                options.format = JsonLdConsts.APPLICATION_NQUADS;
                options.setCompactArrays(true);

                // Frame the RDF-converted JSON-LD
                jsonObject = JsonLdProcessor.frame(jsonObject, frameObject, options);

                // Collect contexts from profiles
                for (URI url : profiles) {
                    contexts.add(url.toString());
                }

                // Extract @context from the frame and add it to the list
                if (frameObject instanceof Map) {
                    Object frameContext = ((Map<?, ?>) frameObject).get("@context");
                    if (frameContext != null) {
                        if (frameContext instanceof List) {
                            // Flatten the list
                            contexts.addAll((List<?>) frameContext);
                        } else {
                            // Add single context (string or map)
                            contexts.add(frameContext);
                        }
                    }
                }
            }
            for (URI url : profiles) {
                contexts.add(url.toString());
            }
            contexts = deduplicateContexts(contexts);
            jsonObject = JsonLdProcessor.compact(jsonObject, contexts, optionsWithContexts);

            jsonObject = reorderJsonAttributes(jsonObject);
            return JsonUtils.toPrettyString(jsonObject);
        } catch (JsonLdError | IOException e) {
            throw new FormatException(e.getMessage(), e);
        }
    }

    /**
     * Deduplicates and flattens a list of JSON-LD context entries.
     * Supports strings (URIs), maps (inline contexts), and nested lists.
     *
     * @param rawContexts A list of context entries (String, Map, or List)
     * @return A flattened, deduplicated list of context entries
     */
    public List<Object> deduplicateContexts(List<Object> rawContexts) {

        List<Object> result = new ArrayList<>();
        Set<String> seenUris = new HashSet<>();
        Set<String> seenInlineContexts = new HashSet<>();


        for (Object ctx : rawContexts) {
            flattenAndAdd(ctx, result, seenUris, seenInlineContexts);
        }

        return result;
    }

    private void flattenAndAdd(Object ctx, List<Object> result,
                                      Set<String> seenUris, Set<String> seenInlineContexts) {
        if (ctx instanceof String uri) {
            if (seenUris.add(uri)) {
                result.add(uri);
            }
        } else if (ctx instanceof Map) {
            try {
                String json = JsonUtils.toString(ctx);
                if (seenInlineContexts.add(json)) {
                    result.add(ctx);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize inline context", e);
            }
        } else if (ctx instanceof List) {
            for (Object nested : (List<?>) ctx) {
                flattenAndAdd(nested, result, seenUris, seenInlineContexts);
            }
        } else {
            throw new IllegalArgumentException("Unsupported context type: " + ctx.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    private Object reorderJsonAttributes(Object jsonObject) {
        if (jsonObject instanceof Map) {
            Map<String, Object> jsonMap = (Map<String, Object>) jsonObject;
            ListOrderedMap<String, Object> orderedJsonMap = new ListOrderedMap<>();
            orderedJsonMap.putAll(jsonMap);

            Object context = orderedJsonMap.get("@context");
            if (context != null) {
                orderedJsonMap.remove("@context");
                orderedJsonMap.put(0, "@context", context);
            }
            return orderedJsonMap;
        } else if (jsonObject instanceof List) {
            // Recursively reorder each item in the list
            List<Object> reorderedList = new ArrayList<>();
            for (Object item : (List<?>) jsonObject) {
                reorderedList.add(reorderJsonAttributes(item));
            }
            return reorderedList;
        } else {
            // Return as-is if it's neither a Map nor a List
            return jsonObject;
        }
    }

    @Override
    public void setAcceptPart(String profilesRaw, Type type) {
        // Profiles are optional, we are therefore always valid
        setValid(true);
        if (profilesRaw == null) {
            // This means we get a request for the default formatter with the default profile either through
            // the client not using a header at all or if content negotiation is disabled
            profiles.add(DEFAULT_PROFILE);
        } else if (profilesRaw.isEmpty()) {
            // This is the case when the client intentionally requested no applied profiles we therefore
            // do not add the default one if not defaulting to add it at the end of the method
        } else if (profilesRaw.startsWith("profile=")) {
            // First check we start with " and
            String profilesWithinQuotes = getProfilesWithingQuotes(profilesRaw.substring("profile=".length()).trim());
            if (profilesWithinQuotes == null) {
                logger.error("Malformed JSON-LD profile String : " + profilesRaw);
                // It is ok to have some malformed profile String here, since profile negotiation is optional
                // we treat this case as if no accept header was given
            } else {
                String[] profiles = profilesWithinQuotes.split(Pattern.quote(" "));
                for (String profile : profiles) {
                    String trimmedProfile = profile.trim();
                    try {
                        URI url = new URI(trimmedProfile);
                        // At least a valid url, but is it locally cached?
                        // We do not support uncached profiles. Inform the registry we need it cached.
                        if (profileRegistry.cacheProfile(url)) {
                            this.profiles.add(url);
                            logger.info("added recognized profile " + url);
                        } else {
                            logger.debug("Skipping not reachable profile : " + url);
                        }
                    } catch (URISyntaxException e) {
                        // not a valid url, this is an error
                        logger.error("Malformed URL in JSON-LD Profile : " + trimmedProfile);
                    }
                }
            }
        } else {
            // did not start with known additional information, we ignore it @code
        }
        // Add anno.jsonld it if server is configured this way.
        // The default profiles are always added if no accept header is given at all
        if (profilesRaw == null || WapServerConfig.getInstance().shouldAlwaysAddDefaultProfilesToJsonLdRequests()) {
            switch (type) {
                //TODO: check if this behaviour is working as intended
                case CONTAINER:
                    profiles.add(LDP_PROFILE);
                case ANNOTATION:
                case PAGE:
                    profiles.add(DEFAULT_PROFILE);
                    break;
                default:
                // Unknown type ? do nothing because profile negotiation can be ignored by the server
            }
        }
    }

    private String getProfilesWithingQuotes(String profiles) {
        // has to be of form "xxxx", optionally with a charset like "xxxx";charset...
        // we are only interested in the part within the quotes
        if (!profiles.startsWith("\"")) {
            // We do not have a starting "
            return null;
        }
        int index = profiles.indexOf("\"", 1); // The second one ends the profile string
        if (index == -1) {
            // We do not have a closing "
            return null;
        }
        return profiles.substring(1, index);
    }

    @Override
    public String getContentType() {
        if (profiles.isEmpty()) {
            return getFormatString();
        }
        StringBuilder profileString = new StringBuilder();
        for (URI url : profiles) {
            String context = url.toString();
            if (!profileString.isEmpty()) {
                profileString.append(" ");
            }
            profileString.append(context);
        }
        profileString.insert(0, "\"");
        profileString.append("\"");
        return getFormatString() + ";profile=" + profileString + ";charset=utf-8";
    }

    @Override
    public String getFormatString() {
        return JSON_LD_STRING;
    }
}
