package edu.kit.scc.dem.wapsrv.controller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;

/**
 * This implementation of a {@link PathMatcher} introduces patterns needed for implementation of the WAP server. The
 * usual logic defines fixed endpoint URLs, which get varying data via fixed parts of their URL, request parameters or
 * via HTML headers or bodies. In our case, we have varying endpoints, and decisions cannot be made using the default
 * tools provided by spring without being left with only one endpoint per HTPP-request type. Only requests matched
 * against the new patterns defined here will be intercepted, everything else gets delegated to the spring default
 * implementation of PathMatcher.
 * <p>
 * Usage examples that also show how to distinguish between page and container:<br>
 * Container : \@RequestMapping(value = WapPathMatcher.CONTAINER_AND_PAGE_PATTERN, method = RequestMethod.GET)<br>
 * Page : \@RequestMapping(value = WapPathMatcher.CONTAINER_AND_PAGE_PATTERN, params = {"iris","page"}, method =
 * RequestMethod.GET)<br>
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class WapPathMatcher implements PathMatcher {
   /**
    * The String pattern used to mark container IDs
    */
   public static final String CONTAINER_ID = "ContainerId";
   /**
    * The String pattern used to mark annotation IDs
    */
   public static final String ANNOTATION_ID = "AnnotationId";
   /**
    * An array containing all paths that would match the newly created patterns, but should not get intercepted because
    * they are used by other components
    */
   private static final Set<String> PATHS_TO_IGNORE = new HashSet<String>();
   /**
    * The pattern to use in \@RequestMapping to signal the reception of container or page requests Final decision which
    * one is requested cannot be made here, usage of RequestMapping-params is necessary. We cannot distinguish them here
    * because the request parameters are not appended in the match requests .
    */
   public static final String CONTAINER_AND_PAGE_PATTERN = "/{" + CONTAINER_ID + "}/";
   /**
    * The pattern to use in \@RequestMapping to signal the reception of annotation requests.
    */
   public static final String ANNOTATION_PATTERN = "/{" + CONTAINER_ID + "}/{" + ANNOTATION_ID + "}";
   /**
    * The original PathMatcher implementation used as delegate
    */
   private final PathMatcher antPathMatcher = new AntPathMatcher();

   /**
    * Returns whether we would intercept match-requests for this pattern or not
    * 
    * @param  pattern
    *                 The pattern to inspect
    * @return         true if intercepted, false otherwise
    */
   private boolean isPatternToIntercept(String pattern) {
      if (pattern.indexOf(ANNOTATION_PATTERN) != -1)
         return true;
      if (pattern.indexOf(CONTAINER_AND_PAGE_PATTERN) != -1)
         return true;
      return false;
   }

   /**
    * Checks whether the given path is in annotation-format
    * 
    * @param  path
    *              The path to test
    * @return      true if annotation, false otherwise
    */
   private boolean isAnnotation(String path) {
      return !path.endsWith("/");
   }

   /**
    * Checks whether the given path is in container- or page-format
    * 
    * @param  path
    *              The path to test
    * @return      true if container or page, false otherwise
    */
   private boolean isContainerOrPage(String path) {
      return path.endsWith("/");
   }
   // Code below implements the interface. Not all methods need changes, some are
   // delegated directly

   /*
    * (non-Javadoc)
    * @see org.springframework.util.PathMatcher#isPattern(java.lang.String)
    */
   @Override
   public boolean isPattern(String path) {
      // System.err.println(path);
      if (isPatternToIntercept(path))
         return true;
      // System.err.println(path);
      return antPathMatcher.isPattern(path);
   }

   /*
    * @see org.springframework.util.PathMatcher#match(java.lang.String, java.lang.String)
    */
   @Override
   public boolean match(String pattern, String path) {
      if (isInterestingPath(path) && isPatternToIntercept(pattern) && haveTheSamePrefixes(pattern, path)) {
         // When looking for a match, Spring asks for a variant of different variants of the actual pattern
         // and it put the pattern behind eventually defined global @RequestMapping paths
         // e.G. /wap in this case
         // pattern= /wap/ANNOTATION_PATTERN.*
         // pattern= /wap/ANNOTATION_PATTERN
         // Therefore look only for the one which end with the pattern and have the same prefix and path
         // that means requests like pattern = /wap/ANNOTATION_PATTERN that also match /swagger-ui.html
         if (pattern.endsWith(CONTAINER_AND_PAGE_PATTERN)) {
            if (isContainerOrPage(path)) {
               return true;
            }
         }
         if (pattern.endsWith(ANNOTATION_PATTERN)) {
            if (isAnnotation(path)) {
               return true;
            }
         }
         // This case is reached if it is one of the ignored variants
         return false;
      } else {
         // Every other request is delegated to the default implementation
         return antPathMatcher.match(pattern, path);
      }
   }

   /**
    * To be of any interest, the path has to be at least a container under the root container
    * 
    * @param  path
    *              The path to check
    * @return      true if we should intercept the path, false otherwise
    */
   private boolean isInterestingPath(String path) {
      for (String ignoredPaths : PATHS_TO_IGNORE) {
         if (path.startsWith(ignoredPaths))
            return false;
      }
      // Extract the part after the wap root
      String endpoint = WapServerConfig.WAP_ENDPOINT;
      // Check if not needed anymore, we request at least a "/" in WapServerConfig
      // if(!endpoint.endsWith("/")) endpoint=endpoint+"/";
      // Only paths that are within our base endpoint are of interest
      if (!path.startsWith(endpoint))
         return false;
      String interestingPath = path.substring(endpoint.length());
      if (interestingPath.length() == 0)
         return false;
      // Root container gets managed by the original spring implementation.
      // Under the root container, no annotations are allowed
      // ==> This means the least that has to be left here must be a direct subcontainer
      // which has to end with a "/"
      // ==> indexOf("/")!=-1
      // http://...WAP_ENDPOINT/containter/ would result here in container/
      return interestingPath.lastIndexOf("/") != -1;
   }

   /**
    * Returns whether the path has the same prefix as the pattern
    * 
    * @param  pattern
    *                 The pattern to compare
    * @param  path
    *                 The path to compare
    * @return         true if prefixes match, false otherwise
    */
   private boolean haveTheSamePrefixes(String pattern, String path) {
      // If we use only / as endpoint, the prefixes do not matter
      if (WapServerConfig.WAP_ENDPOINT.equals("/"))
         return true;
      // One is -1, the other one wins and is the pattern used
      int index = Math.max(pattern.indexOf(CONTAINER_AND_PAGE_PATTERN), pattern.indexOf(ANNOTATION_PATTERN));
      // prefix is everything until the begin of this pattern
      String prefix = pattern.substring(0, index);
      return path.startsWith(prefix);
   }

   /*
    * @see org.springframework.util.PathMatcher#matchStart(java.lang.String, java.lang.String)
    */
   @Override
   public boolean matchStart(String pattern, String path) {
      return antPathMatcher.matchStart(pattern, path);
   }

   /*
    * @see org.springframework.util.PathMatcher#extractPathWithinPattern(java.lang. String, java.lang.String)
    */
   @Override
   public String extractPathWithinPattern(String pattern, String path) {
      return antPathMatcher.extractPathWithinPattern(pattern, path);
   }

   /*
    * @see org.springframework.util.PathMatcher#extractUriTemplateVariables(java.lang. String, java.lang.String)
    */
   @Override
   public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
      // logger.info("TEST pattern: " + pattern + " path: " + path);
      // use simpler variables so we handle by ourselves
      if (isPatternToIntercept(pattern)) {
         Map<String, String> variables = new HashMap<String, String>();
         if (pattern.endsWith(ANNOTATION_PATTERN)) {
            Pattern annoPattern = Pattern.compile("/(.*)/([^/].*)");
            Matcher matcher = annoPattern.matcher(path);
            if (matcher.matches()) {
               variables.put(CONTAINER_ID, matcher.group(1));
               variables.put(ANNOTATION_ID, matcher.group(1));
               // logger.info(CONTAINER_ID + ": *" + matcher.group(1) + "* " + ANNOTATION_ID +
               // ": *" + matcher.group(2) + "*");
            } else {
               // Nothing to do so far, maybe swagger integration will be reworked in implementation phase
            }
         } else if (pattern.endsWith(CONTAINER_AND_PAGE_PATTERN)) {
            Pattern contPattern = Pattern.compile("/(.*)/");
            Matcher matcher = contPattern.matcher(path);
            if (matcher.matches()) {
               variables.put(CONTAINER_ID, matcher.group(1));
               // logger.info(CONTAINER_ID + ": *" + matcher.group(1) + "*");
            } else {
               // Nothing to do so far, maybe swagger integration will be reworked in implementation phase
            }
         }
         return variables;
      } else
         return antPathMatcher.extractUriTemplateVariables(pattern, path);
   }

   /*
    * @see org.springframework.util.PathMatcher#getPatternComparator(java.lang.String)
    */
   @Override
   public Comparator<String> getPatternComparator(String path) {
      return antPathMatcher.getPatternComparator(path);
   }

   /*
    * @see org.springframework.util.PathMatcher#combine(java.lang.String,java.lang. String)
    */
   @Override
   public String combine(String pattern1, String pattern2) {
      // Default implementation is OK even for these patterns.
      // They are regarded as names for requests endpoints, not patterns
      return antPathMatcher.combine(pattern1, pattern2);
   }

   /**
    * Registers a given path to be ignored by local path matching.<br>
    * Every path for which the given one is a prefix will be ignored then.
    * 
    * @param path
    *             The path to ignore
    */
   public static void addIgnoredPattern(String path) {
      PATHS_TO_IGNORE.add(path);
   }
}
