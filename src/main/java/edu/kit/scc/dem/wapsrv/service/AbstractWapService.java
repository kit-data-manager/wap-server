package edu.kit.scc.dem.wapsrv.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.*;
import org.apache.commons.rdf.simple.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.jsonldjava.utils.JsonUtils;
import edu.kit.scc.dem.wapsrv.app.EtagFactory;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.EtagDoesntMatchException;
import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import edu.kit.scc.dem.wapsrv.exceptions.NotAContainerException;
import edu.kit.scc.dem.wapsrv.exceptions.NotAnAnnotationException;
import edu.kit.scc.dem.wapsrv.exceptions.NotExistentException;
import edu.kit.scc.dem.wapsrv.exceptions.ResourceDeletedException;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.ModelFactory;
import edu.kit.scc.dem.wapsrv.model.WapObject;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfUtilities;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.DcTermsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.WapVocab;
import edu.kit.scc.dem.wapsrv.model.validators.Validator;
import edu.kit.scc.dem.wapsrv.model.validators.ValidatorRegistry;
import edu.kit.scc.dem.wapsrv.repository.WapObjectRepository;

/**
 * Base implementation for WapServices that implements common behavior
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public abstract class AbstractWapService implements WapService {
   /**
    * The model factory, autowired
    */
   @Autowired
   protected ModelFactory modelFactory;
   /**
    * The ETag factory, autowired
    */
   @Autowired
   protected EtagFactory etagFactory;
   /**
    * The application configuration, autowired
    */
   @Autowired
   protected WapServerConfig wapServerConfig;
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   /**
    * The wapobject repository, autowired
    */
   @Autowired
   private WapObjectRepository repository;
   /**
    * The validator registry, autowired
    */
   @Autowired
   private ValidatorRegistry validatorRegistry;

   /**
    * Gets the model factory
    * 
    * @return The model factory
    */
   protected ModelFactory getModelFactory() {
      return modelFactory;
   }

   /**
    * Gets the application configuration
    * 
    * @return The application configuration
    */
   protected WapServerConfig getWapServerConfig() {
      return wapServerConfig;
   }

   /**
    * Gets the validator registry
    * 
    * @return The validator registry
    */
   protected ValidatorRegistry getValidatorRegistry() {
      return validatorRegistry;
   }

   /**
    * Generates a random ETag value
    * 
    * @return The random ETag
    */
   protected String generateEtag() {
      return etagFactory.generateEtag();
   }

   @Override
   public boolean isValidInputFormat(Format format) {
      // This checks if the modelFactory can work with the format.
      if (!modelFactory.isValidInputFormat(format)) {
         return false;
      }
      if (wapServerConfig.isValidationEnabled()) {
         // If validation enabled it checks if there is a validator implemented for the format.
         if (wapServerConfig.isFallbackValidationActive()) {
            // Using fallback validation every valid format for the modelFactory is OK for validation
            // because we rely on the JSON-LD validator, which is always implemented.
            return true;
         } else {
            // Without fallback a fitting validator is needed.
            return validatorRegistry.getSupportedFormats().contains(format);
         }
      } else {
         // Everything is fine.
         return true;
      }
   }

   /**
    * Checks input for validity according to WADM schemas. If no Exception is thrown, the annotation/container is
    * valid.<br>
    * If globally disabled in {@link WapServerConfig#enableValidation} this method does nothing.
    * 
    * @param  rawString
    *                                  The serialized annotation/container
    * @param  format
    *                                  The format of the serialized annotation/container
    * @param  type
    *                                  The type of the string, Annotation or Container
    * @throws InternalServerException
    *                                  If validator could not be instantiated
    * @throws NotAnAnnotationException
    *                                  If type annotation and invalid
    * @throws NotAContainerException
    *                                  If type container and invalid
    */
   protected void checkSchemaValidity(String rawString, Format format, FormattableObject.Type type) {
      // If validation is disabled, just return
      if (!getWapServerConfig().isValidationEnabled()) {
         return;
      }
      if (type == FormattableObject.Type.CONTAINER) {
         // we do not implement a JSON-SCHEMA for containers. If one get's created in the future,
         // remove this if statement and do not return here
         return;
      }
      String serializedString = rawString;
      Validator validator = getValidatorRegistry().getValidator(format);
      if (validator == null) {
         if (getWapServerConfig().isFallbackValidationActive()) {
            validator = getValidatorRegistry().getValidator(Format.JSON_LD);
            if (validator == null) {
               throw new InternalServerException("Validator for " + format + " not instantiated");
            } else {
               LoggerFactory.getLogger(this.getClass()).info("Using fallback validation for " + format);
               serializedString = getModelFactory().convertFormat(serializedString, format, Format.JSON_LD);
               // If not working, an exception has been thrown, not null returned
            }
         } else {
            throw new InternalServerException("Validator for " + format + " not instantiated");
         }
      }
      long before = System.currentTimeMillis();
      if (type == FormattableObject.Type.ANNOTATION) {
         if (!validator.validateAnnotation(serializedString)) {
            throw new NotAnAnnotationException();
         }
      } else if (type == FormattableObject.Type.CONTAINER) {
         if (!validator.validateContainer(serializedString)) {
            throw new NotAContainerException();
         }
      } else {
         throw new InternalServerException("We should check schema validity of anything other than anno/container");
      }
      System.out.println("Validating annotation " + (System.currentTimeMillis() - before) + " ms");
   }

   /**
    * This methods asserts that only valid ids are used by replacing them if needed
    * 
    * @param  jsonLdString
    *                      The source string
    * @return              A copy of the input using only valid IDs
    */
   protected String assertValidIds(String jsonLdString) {
      try {
         Object jsonObject = JsonUtils.fromString(jsonLdString);
         if (jsonObject instanceof List) {
            List<?> jsonList = (List<?>) jsonObject;
            assertValidIds(jsonList);
         }
         String erg = JsonUtils.toPrettyString(jsonObject);
         // System.out.println("From:\n" + jsonLdString);
         // System.out.println("To:\n" + erg);
         return erg;
      } catch (IOException e) {
         throw new FormatException("Exception asserting only valid ids are used : " + e.getMessage());
      }
   }

   @SuppressWarnings("rawtypes")
   private void assertValidIds(List jsonList) {
      for (Object member : jsonList) {
         if (member instanceof List) {
            assertValidIds((List) member);
         } else if (member instanceof Map) {
            Map map = (Map) member;
            List<Object> keysToRemove = new ArrayList<Object>();
            for (Object key : map.keySet()) {
               Object value = map.get(key);
               if ("@id".equals(key)) {
                  if ("\"\"".equals(value)) {
                     keysToRemove.add(key);
                  } else if ("".equals(value)) {
                     keysToRemove.add(key);
                  } else if (!isValidUri(value)) {
                     throw new FormatException("ID not a URI : " + key + "=" + value);
                  }
               } else if (value instanceof List) {
                  assertValidIds((List) value);
               } else {
                  // Nothing to do so far
               }
            }
            for (Object key : keysToRemove) {
               // Either remove
               map.remove(key);
               // or replace
               // map.put(key, "_b:" + UUID.randomUUID().toString());
            }
         } else {
            // Nothing to do so far
         }
      }
   }

   private boolean isValidUri(Object value) {
      if (value == null)
         return false;
      if (!(value instanceof String))
         return false;
      String uriString = (String) value;
      if (uriString.indexOf(":") == -1) {
         return false; // we do not allow URIs without : (= URI reference)
      }
      try {
         @SuppressWarnings("unused")
         URI uri = new URI(uriString);
      } catch (URISyntaxException e) {
         return false;
      }
      return true;
   }

   /**
    * Check if etag of a WapObject matches. this function throws a {@link EtagDoesntMatchException} if the etag given is
    * not identical to the one in the database.
    *
    * @param iri
    *             the IRI of the WapObject to check the etag
    * @param etag
    *             the etag should be in the database for the WapObject with the given IRI
    */
   public void checkEtag(String iri, String etag) {
      log.info("checking etag match for: '" + iri + "' with given etag: '" + etag + "'");
      BlankNodeOrIRI node = repository.getRdf().createIRI(iri);
      Literal etagLiteral = repository.getRdf().createLiteral(etag);
      repository.readRdfTransaction(ds -> {
         Graph graph = ds.getGraph(node).get();
         if (!graph.stream(node, WapVocab.etag, etagLiteral).findFirst().isPresent()) {
            log.warn("checking etag match for: '" + iri + "' failed.");
            throw new EtagDoesntMatchException("The etag given does not match the etag in the database");
         }
      });
      log.info("checking etag match for: '" + iri + "' succeded.");
   }

   /**
    * Delete WapObject. This function marks the WapObject as "deleted" and removes it from its parent containers
    * annotation/subcontainer rdf:seq.
    *
    * @param iri
    *                     the IRI of the WapObject to delete
    * @param parentSeqIri
    *                     the parent rdf:seq IRI to remove the WapObject from
    */
   public void deleteObject(String iri, String parentSeqIri) {
      log.info("deleting object '" + iri + "'");
      BlankNodeOrIRI node = repository.getRdf().createIRI(iri);
      Literal trueLiteral = repository.getRdf().createLiteral("true", Types.XSD_BOOLEAN);
      String parentContainerIriString = WapObject.getParentContainerIriString(iri);
      BlankNodeOrIRI parentNode = repository.getRdf().createIRI(parentContainerIriString);
      repository.writeRdfTransaction(ds -> {
         Graph graph = ds.getGraph(node).get();
         graph.add(node, WapVocab.deleted, trueLiteral);
         repository.removeElementFromRdfSeq(parentContainerIriString, parentSeqIri, iri);
         updateEtag(parentNode, etagFactory.generateEtag());
      });
      log.info("deleteing object: '" + iri + "' succeded.");
   }

   /**
    * Delete WapObjects as Bulk. This function only marks every WapObject in the iriList as "deleted" but does not
    * remove it from its parent containers annotation/subcontainer rdf:seq.
    *
    * @param iriList
    *                the IRI list
    */
   public void deleteObjectBulk(List<String> iriList) {
      log.info("bulk deleting of objects.");
      Literal trueLiteral = repository.getRdf().createLiteral("true", Types.XSD_BOOLEAN);
      for (String iri : iriList) {
         BlankNodeOrIRI node = repository.getRdf().createIRI(iri);
         repository.writeRdfTransaction(ds -> {
            log.info("bulk deleting annotation: '" + iri + "'");
            ds.add(node, node, WapVocab.deleted, trueLiteral);
         });
      }
      log.info("bulk deleting of objects done.");
   }

   /**
    * Update etag and the modified Tag since any changes on a object have to update the etag this function has to adapt
    * the modified date, too.
    *
    * @param node
    *                     the IRI of the WapObject
    * @param generateEtag
    *                     the new etag for the WapObject
    */
   public void updateEtag(BlankNodeOrIRI node, String generateEtag) {
      log.info("updating etag for: '" + node.ntriplesString() + "' with given etag: '" + generateEtag + "'");
      Literal etagLiteral = repository.getRdf().createLiteral(generateEtag);
      Literal modifiedLiteral = RdfUtilities.rdfLiteralFromCalendar(Calendar.getInstance(), repository.getRdf());
      repository.writeRdfTransaction(ds -> {
         Graph graph = ds.getGraph(node).get();
         graph.remove(node, WapVocab.etag, null);
         graph.add(node, WapVocab.etag, etagLiteral);
         // Update the Modified
         graph.remove(node, DcTermsVocab.modified, null);
         graph.add(node, DcTermsVocab.modified, modifiedLiteral);
      });
      log.info("updated etag for: '" + node.ntriplesString() + "'");
   }

   /**
    * Update etag and the modified Tag since any changes on a object have to update the etag this function has to adapt
    * the modified date, too.
    *
    * @param iri
    *                     the IRI of the WapObject
    * @param generateEtag
    *                     the new etag for the WapObject
    */
   public void updateEtag(String iri, String generateEtag) {
      BlankNodeOrIRI node = repository.getRdf().createIRI(iri);
      updateEtag(node, generateEtag);
   }

   /**
    * Write WapObject to database.
    *
    * @param wapObject
    *                  the WapObject to be put in the database.
    */
   protected void writeWapObjectToDb(WapObject wapObject) {
      log.info("writeing WapObject to DB: '" + wapObject.getIriString() + "'");
      String etag = wapObject.getEtag();
      String iriString = wapObject.getIriString();
      repository.writeRdfTransaction(ds -> {
         repository.writeObjectToDatabase(wapObject);
         // Add ETag
         String newEtag = etag;
         if (etag == null) {
            newEtag = etagFactory.generateEtag();
            wapObject.setEtag(newEtag);
         }
         // Update ETag of this object
         updateEtag(iriString, newEtag);
      });
      log.info("writeing WapObject: '" + wapObject.getIriString() + "' successfull.");
   }

   /**
    * Check if the WapObject exists and has not been marked as "deleted". The function throws
    * {@link NotExistentException} if the WapObject does not exist and it throws a {@link ResourceDeletedException} if
    * the WapObject is marked as "deleted".
    *
    * @param objectIri
    *                  the object IRI to be checked
    */
   public void checkExistsAndNotDeleted(String objectIri) {
      String typeString = objectIri.endsWith("/") ? "container" : "annotation";
      repository.readRdfTransaction(ds -> {
         if (!containsIri(objectIri)) {
            throw new NotExistentException("The requested " + typeString + " does not exist.");
         }
         if (isIriDeleted(objectIri)) {
            throw new ResourceDeletedException("The requested " + typeString + " has already been deleted.");
         }
      });
   }

   /**
    * Checks if the database contains the given IRI.
    *
    * @param  iri
    *             the IRI
    * @return     true, if IRI in database
    */
   public boolean containsIri(String iri) {
      log.info("checking if exists: '" + iri + "'");
      Boolean[] result = new Boolean[1];
      BlankNodeOrIRI node = repository.getRdf().createIRI(iri);
      repository.readRdfTransaction(ds -> {
         Graph graph = ds.getGraph(node).get();
         result[0] = graph.contains(node, null, null);
      });
      log.info("check exists result for: '" + iri + "' is: '" + result[0] + "'");
      return result[0];
   }

   /**
    * Checks if IRI is marked as "deleted".
    *
    * @param  iri
    *             the IRI to check
    * @return     true, if IRI is marked as "deleted"
    */
   public boolean isIriDeleted(String iri) {
      log.info("checking if deleted: '" + iri + "'");
      Boolean[] result = new Boolean[1];
      BlankNodeOrIRI node = repository.getRdf().createIRI(iri);
      repository.readRdfTransaction(ds -> {
         Graph graph = ds.getGraph(node).get();
         result[0] = graph.contains(node, WapVocab.deleted, null);
         if(result[0]) {
            Triple deletedTriple = graph.stream(node, WapVocab.deleted, null).findFirst().orElseThrow();
            Literal falseLiteral = repository.getRdf().createLiteral("false", Types.XSD_BOOLEAN);
            if(deletedTriple.getObject().equals(falseLiteral)) {
               //deleted flag is present but value is set to false
               result[0] = false;
            }
         }
      });
      log.info("check deleted result for: '" + iri + "' is: '" + result[0] + "'");
      return result[0];
   }

   /**
    * Gets the Annotation. First it will be checked if the IRI exists and is not deleted.
    *
    * @param  iri
    *                      the IRI for the Annotation
    * @return              the requested annotation
    * @throws WapException
    *                      the Exceptions from @see #checkExistsAndNotDeleted(String)
    */
   @Override
   public Annotation getAnnotation(String iri) throws WapException {
      log.info("Get Annotation from DB: '" + iri + "'");
      Dataset[] retDs = new Dataset[1];
      repository.readRdfTransaction(ds -> {
         checkExistsAndNotDeleted(iri);
         retDs[0] = repository.getWapObject(iri);
      });
      log.info("Get Annotation finished: '" + iri + "'");
      return modelFactory.createAnnotation(retDs[0]);
   }
}
