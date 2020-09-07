package edu.kit.scc.dem.wapsrv.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests the class ContainerPreference
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
class ContainerPreferenceTest {
   /**
    * Test constructor
    */
   @Test
   void testConstruct() {
      ContainerPreference actual = new ContainerPreference();
      assertNotNull(actual, "Construction did fail.");
   }

   /**
    * Test to set.
    */
   @Test
   final void testToSet() {
      String paramPrefer;
      Set<Integer> actual;
      Set<Integer> expected;
      paramPrefer
            = "return=representation;   include=   \"" + ContainerPreference.PREFER_MINIMAL_CONTAINER_STRING + "\"   ";
      actual = ContainerPreference.toSet(paramPrefer);
      expected = new HashSet<Integer>();
      expected.add(ContainerPreference.PREFER_MINIMAL_CONTAINER);
      assertEquals(expected, actual,
            "PREFER_MINIMAL_CONTAINER_STRING turns into wrong integer value: " + actual.toArray()[0]);
      paramPrefer
            = "return=representation;   include=   \"" + ContainerPreference.PREFER_CONTAINED_IRIS_STRING + "\"   ";
      actual = ContainerPreference.toSet(paramPrefer);
      expected = new HashSet<Integer>();
      expected.add(ContainerPreference.PREFER_CONTAINED_IRIS);
      assertEquals(expected, actual,
            "PREFER_CONTAINED_IRIS_STRING turns into wrong integer value: " + actual.toArray()[0]);
      paramPrefer = "return=representation;   include=   \"" + ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS_STRING
            + "\"   ";
      actual = ContainerPreference.toSet(paramPrefer);
      expected = new HashSet<Integer>();
      expected.add(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS);
      assertEquals(expected, actual,
            "PREFER_CONTAINED_DESCRIPTIONS_STRING turns into wrong integer value: " + actual.toArray()[0]);
      paramPrefer = "return=representation;   include=   \"" + ContainerPreference.PREFER_MINIMAL_CONTAINER_STRING
            + "   " + ContainerPreference.PREFER_CONTAINED_IRIS_STRING + "   "
            + ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS_STRING + "\"   ";
      actual = ContainerPreference.toSet(paramPrefer);
      expected = new HashSet<Integer>();
      expected.add(ContainerPreference.PREFER_MINIMAL_CONTAINER);
      expected.add(ContainerPreference.PREFER_CONTAINED_IRIS);
      expected.add(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS);
      assertEquals(expected, actual, "Prefer Strings turns into not expected Set<Integer> values.");
      paramPrefer = "";
      actual = ContainerPreference.toSet(paramPrefer);
      assertNull(actual, "Empty prefer String should be null!");
      paramPrefer = "to much scrap...";
      actual = ContainerPreference.toSet(paramPrefer);
      assertNull(actual, "Invalid prefer String should be null!");
      paramPrefer = "return=representation;";
      actual = ContainerPreference.toSet(paramPrefer);
      assertNull(actual, "Incomplete prefer String should be null!");
      paramPrefer = "return=representation; include=\"\"";
      actual = ContainerPreference.toSet(paramPrefer);
      assertNull(actual, "Prefer String with empty includes should be null!");
      paramPrefer = "return=representation; include=\"" + ContainerPreference.PREFER_MINIMAL_CONTAINER_STRING
            + " scrap scrap\"";
      actual = ContainerPreference.toSet(paramPrefer);
      assertNull(actual, "Prefer String with invalid includes should be null!");
      paramPrefer = "return=representation; include=" + ContainerPreference.PREFER_MINIMAL_CONTAINER_STRING;
      actual = ContainerPreference.toSet(paramPrefer);
      assertNull(actual, "Prefer String with missing qoutes should be null!");
      actual = ContainerPreference.toSet(null);
      assertNull(actual, "Prefer of null should result in null!");
      actual = ContainerPreference.toSet("return=representation; include=invalid\"");
      assertNull(actual, "Prefer starting with quotes but ending with quotes should result in null!");
   }

   /**
    * Test if is prefer minimal container.
    */
   @Test
   final void testIsPreferMinimalContainer() {
      Set<Integer> paramPreferences;
      boolean actual;
      paramPreferences = new HashSet<Integer>();
      paramPreferences.add(ContainerPreference.PREFER_MINIMAL_CONTAINER);
      actual = ContainerPreference.isPreferMinimalContainer(paramPreferences);
      assertTrue(actual, "Preferences does not contain PREFER_MINIMAL_CONTAINER.");
      paramPreferences = null;
      actual = ContainerPreference.isPreferMinimalContainer(paramPreferences);
      assertFalse(actual, "With preferences NULL, return should be false.");
      paramPreferences = new HashSet<Integer>();
      paramPreferences.add(424242);
      actual = ContainerPreference.isPreferMinimalContainer(paramPreferences);
      assertFalse(actual, "Preferences with item 424242 should not contain PREFER_MINIMAL_CONTAINER.");
      paramPreferences = new HashSet<Integer>();
      actual = ContainerPreference.isPreferMinimalContainer(paramPreferences);
      assertFalse(actual, "Empty Preferences should not contain PREFER_MINIMAL_CONTAINER.");
      paramPreferences = new HashSet<Integer>();
      paramPreferences.add(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS);
      paramPreferences.add(ContainerPreference.PREFER_CONTAINED_IRIS);
      actual = ContainerPreference.isPreferMinimalContainer(paramPreferences);
      assertFalse(actual, "Preferences should not contain PREFER_MINIMAL_CONTAINER while it contains others.");
   }

   /**
    * Test if is prefer contained descriptions.
    */
   @Test
   final void testIsPreferContainedDescriptions() {
      Set<Integer> paramPreferences;
      boolean actual;
      paramPreferences = new HashSet<Integer>();
      paramPreferences.add(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS);
      actual = ContainerPreference.isPreferContainedDescriptions(paramPreferences);
      assertTrue(actual, "Preferences does not contain PREFER_CONTAINED_DESCRIPTIONS.");
      paramPreferences = null;
      actual = ContainerPreference.isPreferContainedDescriptions(paramPreferences);
      assertFalse(actual, "With preferences NULL, return should be false.");
      paramPreferences = new HashSet<Integer>();
      paramPreferences.add(424242);
      actual = ContainerPreference.isPreferContainedDescriptions(paramPreferences);
      assertFalse(actual, "Preferences with item 424242 should not contain PREFER_CONTAINED_DESCRIPTIONS.");
      paramPreferences = new HashSet<Integer>();
      actual = ContainerPreference.isPreferContainedDescriptions(paramPreferences);
      assertFalse(actual, "Empty Preferences should not contain PREFER_CONTAINED_DESCRIPTIONS.");
      paramPreferences = new HashSet<Integer>();
      paramPreferences.add(ContainerPreference.PREFER_MINIMAL_CONTAINER);
      paramPreferences.add(ContainerPreference.PREFER_CONTAINED_IRIS);
      actual = ContainerPreference.isPreferContainedDescriptions(paramPreferences);
      assertFalse(actual, "Preferences should not contain PREFER_CONTAINED_DESCRIPTIONS while it contains others.");
   }

   /**
    * Test if is prefer contained IRI.
    */
   @Test
   final void testIsPreferContainedIRIs() {
      Set<Integer> paramPreferences;
      boolean actual;
      paramPreferences = new HashSet<Integer>();
      paramPreferences.add(ContainerPreference.PREFER_CONTAINED_IRIS);
      actual = ContainerPreference.isPreferContainedIRIs(paramPreferences);
      assertTrue(actual, "Preferences does not contain PREFER_CONTAINED_IRIS.");
      paramPreferences = null;
      actual = ContainerPreference.isPreferContainedIRIs(paramPreferences);
      assertFalse(actual, "With preferences NULL, return should be false.");
      paramPreferences = new HashSet<Integer>();
      paramPreferences.add(424242);
      actual = ContainerPreference.isPreferContainedIRIs(paramPreferences);
      assertFalse(actual, "Preferences with item 424242 should not contain PREFER_CONTAINED_IRIS.");
      paramPreferences = new HashSet<Integer>();
      actual = ContainerPreference.isPreferContainedIRIs(paramPreferences);
      assertFalse(actual, "Empty Preferences should not contain PREFER_CONTAINED_IRIS.");
      paramPreferences = new HashSet<Integer>();
      paramPreferences.add(ContainerPreference.PREFER_MINIMAL_CONTAINER);
      paramPreferences.add(ContainerPreference.PREFER_CONTAINED_DESCRIPTIONS);
      actual = ContainerPreference.isPreferContainedIRIs(paramPreferences);
      assertFalse(actual, "Preferences should not contain PREFER_CONTAINED_IRIS while it contains others.");
   }
}
