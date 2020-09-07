package edu.kit.scc.dem.wapsrv.model;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Helper methods and constants for Container preferences
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public final class ContainerPreference {
   /**
    * The String to recognize the preference to contain full annotations
    */
   public static final String PREFER_CONTAINED_DESCRIPTIONS_STRING
         = "http://www.w3.org/ns/oa#PreferContainedDescriptions";
   /**
    * The String to recognize the preference to contain annotation IRIs only
    */
   public static final String PREFER_CONTAINED_IRIS_STRING = "http://www.w3.org/ns/oa#PreferContainedIRIs";
   /**
    * The String to recognize the preference for a minimal container representation
    */
   public static final String PREFER_MINIMAL_CONTAINER_STRING = "http://www.w3.org/ns/ldp#PreferMinimalContainer";
   /**
    * Prefer contained descriptions
    */
   public static final int PREFER_CONTAINED_DESCRIPTIONS = 0;
   /**
    * Prefer contained IRIs
    */
   public static final int PREFER_CONTAINED_IRIS = 1;
   /**
    * Prefer minimal container
    */
   public static final int PREFER_MINIMAL_CONTAINER = 2;

   /**
    * Returns a set containing the parsed container preferences.
    * 
    * @param  prefer
    *                The prefer header to parse
    * @return        The preferences, null if header was invalid
    */
   public static Set<Integer> toSet(String prefer) {
      if (prefer == null)
         return null;
      String preferedHeader = prefer.trim();
      final String returnString = "return=representation;";
      final String includeString = "include=";
      // System.err.println("1 "+preferedHeader);
      // example that would be given in one line : return=representation;include=
      // "http://www.w3.org/ns/ldp#PreferMinimalContainer
      // http://www.w3.org/ns/oa#PreferContainedIRIs"
      if (!preferedHeader.startsWith(returnString))
         return null;
      // Cut the return part off and remove potential white space
      preferedHeader = preferedHeader.substring(returnString.length()).trim();
      // System.err.println("2 "+preferedHeader);
      // now: include="http://www.w3.org/ns/ldp#PreferMinimalContainer
      // http://www.w3.org/ns/oa#PreferContainedIRIs"
      if (!preferedHeader.startsWith(includeString))
         return null;
      // Cut the return part off and remove potential white space
      preferedHeader = preferedHeader.substring(includeString.length()).trim();
      // System.err.println("3 "+preferedHeader);
      // now: "http://www.w3.org/ns/ldp#PreferMinimalContainer
      // http://www.w3.org/ns/oa#PreferContainedIRIs"
      if (!preferedHeader.startsWith("\"") && preferedHeader.endsWith("\""))
         return null;
      // Cut the " off and remove potential white space
      preferedHeader = preferedHeader.substring(1, preferedHeader.length() - 1).trim();
      // System.err.println("4 "+preferedHeader);
      // now: "http://www.w3.org/ns/ldp#PreferMinimalContainer
      // http://www.w3.org/ns/oa#PreferContainedIRIs"
      // this is a space separated list if urls, assure only one space is between them
      while (preferedHeader.contains("  ")) {
         preferedHeader = preferedHeader.replaceAll(Pattern.quote("  "), " ");
      }
      // split it now and parse the parts
      return toSet(preferedHeader.split(Pattern.quote(" ")));
   }

   /**
    * Parses the values of the given string array and adds them to the set
    * 
    * @param  preferenceStrings
    *                           The array of preference strings to parse
    * @return                   Set of preferences, null in case any error occurred
    */
   private static Set<Integer> toSet(String[] preferenceStrings) {
      // now: [0]=http://www.w3.org/ns/ldp#PreferMinimalContainer,
      // [1]=http://www.w3.org/ns/oa#PreferContainedIRIs ...
      HashSet<Integer> preferences = new HashSet<Integer>();
      for (String preference : preferenceStrings) {
         // System.err.println("5 "+preference);
         if (PREFER_CONTAINED_DESCRIPTIONS_STRING.equals(preference)) {
            preferences.add(PREFER_CONTAINED_DESCRIPTIONS);
         } else if (PREFER_CONTAINED_IRIS_STRING.equals(preference)) {
            preferences.add(PREFER_CONTAINED_IRIS);
         } else if (PREFER_MINIMAL_CONTAINER_STRING.equals(preference)) {
            preferences.add(PREFER_MINIMAL_CONTAINER);
         } else {
            // invalid header
            return null;
         }
      }
      return preferences;
   }

   /**
    * Returns whether preferMinimalContainer is within the given set
    * 
    * @param  preferences
    *                     Set of preferences
    * @return             true if preferMinimalContainer
    */
   public static boolean isPreferMinimalContainer(Set<Integer> preferences) {
      if (preferences == null)
         return false;
      return preferences.contains(ContainerPreference.PREFER_MINIMAL_CONTAINER);
   }

   /**
    * Returns whether preferContainedDescriptions is within the given set
    * 
    * @param  preferences
    *                     Set of preferences
    * @return             true if preferContainedDescriptions
    */
   public static boolean isPreferContainedDescriptions(Set<Integer> preferences) {
      if (preferences == null)
         return false;
      return preferences.contains(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS);
   }

   /**
    * Returns whether preferContainedIRIs is within the given set
    * 
    * @param  preferences
    *                     Set of preferences
    * @return             true if preferContainedIRIs
    */
   public static boolean isPreferContainedIRIs(Set<Integer> preferences) {
      if (preferences == null)
         return false;
      return preferences.contains(ContainerPreference.PREFER_CONTAINED_IRIS);
   }
}
