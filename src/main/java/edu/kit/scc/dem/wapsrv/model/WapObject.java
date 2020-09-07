package edu.kit.scc.dem.wapsrv.model;

import java.util.List;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfUtilities;

/**
 * The WapObject that is the base interface Annotation and Container extend
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface WapObject extends FormattableObject {
   /**
    * Gets the data set object acting as the data backend
    * 
    * @return The data set
    */
   Dataset getDataset();

   /**
    * Returns the IRI of the WapObject.
    * 
    * @return The IRI
    */
   BlankNodeOrIRI getIri();

   /**
    * Returns the IRI of the WapObject.
    * 
    * @return The IRI
    */
   default String getIriString() {
      return RdfUtilities.nStringToString(getIri().ntriplesString());
   }

   /**
    * Sets the IRI of the WapObject.
    * 
    * @param iri
    *            The IRI to set
    */
   void setIri(BlankNodeOrIRI iri);

   /**
    * Sets the IRI of the WapObject.
    * 
    * @param iri
    *                The IRI to set
    * @param copyVia
    *                true, if the IRI should be copied to via field, else false
    */
   void setIri(BlankNodeOrIRI iri, boolean copyVia);

   /**
    * Sets the IRI of the WapObject.
    * 
    * @param iri
    *                The IRI to set
    * @param copyVia
    *                true, if the IRI should be copied to via field, else false
    */
   void setIri(String iri, boolean copyVia);

   /**
    * Sets the IRI of the WapObject.
    * 
    * @param iri
    *            The IRI to set
    */
   void setIri(String iri);

   /**
    * Return the ETag of the WapObject
    * 
    * @return The ETag
    */
   String getEtagQuoted();

   /**
    * Return the ETag of the WapObject
    * 
    * @return The ETag
    */
   String getEtag();

   /**
    * Sets the ETag
    * 
    * @param etag
    *             The ETag
    */
   void setEtag(String etag);

   /**
    * Creates the created property if not already existent
    */
   void setCreated();

   /**
    * Returns the value of the given property. If more than one exists, the first occurrence is returned.<br>
    * In case more than one exists, this may be a different value on each call since no ordering is enforced.
    * 
    * @param  propertyName
    *                      The property to list its value
    * @return              The value of the property. Null is returned if none exists
    */
   String getValue(IRI propertyName);

   /**
    * Returns a list containing all occurrences of the given properties values.
    * 
    * @param  propertyName
    *                      The property to list its values
    * @return              List of all values of the property. An empty list is returned if none exist
    */
   List<String> getValues(IRI propertyName);

   /**
    * Compare if the values for the given WapObject of a given property are equal to ours.<br>
    * They are equal if we have the same amount of values for the property and we contain the same values, not depending
    * on order.
    *
    * @param  wapObject2
    *                      The second annotation
    * @param  propertyName
    *                      The property to check
    * @return              true, if is property with multiple values equal
    */
   default boolean isPropertyWithMultipleValuesEqual(WapObject wapObject2, IRI propertyName) {
      WapObject wapObject1 = this;
      List<String> values1 = wapObject1.getValues(propertyName);
      List<String> values2 = wapObject2.getValues(propertyName);
      // not equal size is always false
      if (values1.size() != values2.size()) {
         return false;
      }
      for (String value : values1) {
         if (!values2.contains(value)) {
            return false;
         }
      }
      // All values from values1 are in values2, size is equal ==> they are equal
      return true;
   }

   /**
    * Compare if the value for a given given WapObject of a given property is equal to ours.<br>
    * It is equal if we both don't have it, or if we both have it and the two are equal.
    * <p>
    * If it is not guaranteed that only one value of the given property may exist, use
    * {@link #isPropertyWithMultipleValuesEqual(WapObject, IRI)} instead.
    * 
    * @param  wapObject2
    *                      The second WapObject
    * @param  propertyName
    *                      The property to check
    * @return              true if equals, false otherwise
    */
   default boolean isPropertyEqual(WapObject wapObject2, IRI propertyName) {
      WapObject wapObject1 = this;
      String property1 = wapObject1.getValue(propertyName);
      String property2 = wapObject2.getValue(propertyName);
      // both null, OK
      if (property1 == null && property2 == null)
         return true;
      // only property1 null, not OK
      if (property1 == null)
         return false;
      // rest can be mapped to String.equals
      return property1.equals(property2);
   }

   /**
    * Checks if this WapObject has the given property.
    * 
    * @param  property
    *                  The property to check
    * @return          true If property (and associated value) exist, false otherwise
    */
   default boolean hasProperty(IRI property) {
      return getValue(property) != null;
   }

   /**
    * Checks whether this wap object has been marked deleted
    * 
    * @return true if marked as deleted
    */
   boolean isDeleted();

   /**
    * Extracts the parent container IRI of a given IRI
    * 
    * @param  iri
    *             The IRI to extract the parent container from
    * @return     The parent container IRI
    */
   static String getParentContainerIriString(String iri) {
      // http://example.org/container1/ ==> http://example.org/
      // http://example.org/container1/anno1 ==> http://example.org/container1/
      return iri.substring(0, iri.lastIndexOf("/", iri.length() - 2) + 1);
   }
}
