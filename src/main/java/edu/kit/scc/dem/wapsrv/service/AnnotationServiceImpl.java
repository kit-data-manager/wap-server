package edu.kit.scc.dem.wapsrv.service;

import java.util.Optional;
import java.util.UUID;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.kit.scc.dem.wapsrv.app.EtagFactory;
import edu.kit.scc.dem.wapsrv.exceptions.EtagDoesntMatchException;
import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.exceptions.MethodNotAllowedException;
import edu.kit.scc.dem.wapsrv.exceptions.NotAnAnnotationException;
import edu.kit.scc.dem.wapsrv.exceptions.ResourceDeletedException;
import edu.kit.scc.dem.wapsrv.exceptions.ResourceExistsException;
import edu.kit.scc.dem.wapsrv.exceptions.UnallowedPropertyChangeException;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.AnnotationList;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.WapObject;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AnnoVocab;
import edu.kit.scc.dem.wapsrv.repository.AnnotationRepository;

/**
 * This class provides the annotation service to use in controller methods. It
 * interacts with the repository, with validators and the model factory.
 * Additionally, it performs all further needed checks, modifications of
 * properties and so on. The repository should not have to think much about the
 * data it gets, it has already been checked.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@Service
public class AnnotationServiceImpl extends AbstractWapService implements AnnotationService{

  /**
   * The annotation repository, autowired
   */
  @Autowired
  private AnnotationRepository repository;
  /**
   * The JsonLdProfileRegistry, autowired
   */
  @Autowired
  private JsonLdProfileRegistry jsonLdProfileRegistry;
  /**
   * The ETag factory, autowired
   */
  @Autowired
  private EtagFactory etagFactory;

  /**
   * Spec: <a href="https://www.w3.org/TR/annotation-protocol/#update-an-existing-annotation">WAP 5.3 Update an Existing Annotation</a>
   *
   * @param iri
   *                       The IRI of the annotation
   * @param etag
   *                       The ETag associated with the annotation state known to the client
   * @param rawAnnotation
   *                       A String representation of the Annotation
   * @param format
   *                       The data format used
   * @return the updated annotation
   * @throws WapException on error encountered during update
   */
  @Override
  public Annotation putAnnotation(final String iri, final String etag, String rawAnnotation, Format format)
          throws WapException{
    // If format is JSON_LD, expand it
    String annotationString = null;
    if(format == Format.JSON_LD){
      annotationString = jsonLdProfileRegistry.expandJsonLd(rawAnnotation);
    } else{
      annotationString = rawAnnotation;
    }
    // Check if the annotation exists and is not deleted
    checkExistsAndNotDeleted(iri);
    // put can now again be validated after invalid blank node iris _:b....
    // are not copied to via anymore
    checkSchemaValidity(annotationString, format, FormattableObject.Type.ANNOTATION);
    // if code is reached, everything was fine
    Annotation existingAnnotation = getAnnotation(iri);
    if(!existingAnnotation.getEtag().equals(etag)){
      throw new EtagDoesntMatchException(
              "ETag mismatch : provided ETag : " + etag + " , DB ETag : " + existingAnnotation.getEtagQuoted());
    }
    Annotation newAnnotation = getModelFactory().createAnnotation(annotationString, format);
    if(!iri.equals(newAnnotation.getIriString())){
      throw new UnallowedPropertyChangeException("The IRI cannot change with a PUT requests");
    }
    /**
     * Servers SHOULD reject update requests that modify the values of the canonical or via properties,
     * if they have been already set
     */
    // Check if no forbidden field has been changed canonical (there is only one)
    if(existingAnnotation.hasProperty(AnnoVocab.canonical) && !existingAnnotation.isPropertyEqual(newAnnotation, AnnoVocab.canonical)){
      throw new UnallowedPropertyChangeException("canonical property cannot change");
    }
    // Check via (there may be more)
    if(existingAnnotation.hasProperty(AnnoVocab.via) && !existingAnnotation.isPropertyWithMultipleValuesEqual(newAnnotation, AnnoVocab.via)){
      throw new UnallowedPropertyChangeException("via properties cannot change");
    }
    String oldEtag = etag;
    // Just to clarify this. The annotation has already a new one set.
    BlankNodeOrIRI node = repository.getRdf().createIRI(iri);
    repository.writeRdfTransaction(ds -> {
      checkExistsAndNotDeleted(iri);
      checkEtag(iri, oldEtag);
      ds.remove(Optional.of(node), null, null, null);
      writeWapObjectToDb(newAnnotation);
      // New ETag for parent container
      updateEtag(WapObject.getParentContainerIriString(iri), etagFactory.generateEtag());
    });
    Annotation returnAnnotation = getAnnotation(iri);
    // If no exception has been thrown ETag matched and annotation was updated
    return returnAnnotation;
  }

  @Override
  public void deleteAnnotation(String iri, String etag) throws WapException{
    checkExistsAndNotDeleted(iri);
    // ETag check can be omitted since the object is not created and the repository will check anyway.
    repository.writeRdfTransaction(ds -> {
      checkEtag(iri, etag);
      deleteObject(iri, Container.toAnnotationSeqIriString(WapObject.getParentContainerIriString(iri)));
    });
  }

  @Override
  public AnnotationList postAnnotation(final String containerIri, String rawAnnotation, Format format)
          throws WapException{
    // Attention: Posting an annotation cannot collide, because the name is autocreated in a collision
    // free way. Therefore no need to check for the existence like in post container requests.
    // Post to the root container is forbidden by default.
    if(getWapServerConfig().isRootWapUrl(containerIri)){
      throw new MethodNotAllowedException("Post annotation to the root container not allowed");
    }
    // Check if the container exists and is not deleted.
    checkExistsAndNotDeleted(containerIri);
    // If format is JSON_LD, expand it.
    String annotationString = null;
    if(format == Format.JSON_LD){
      annotationString = jsonLdProfileRegistry.expandJsonLd(rawAnnotation);
      try{
        annotationString = assertValidIds(annotationString);
      } catch(FormatException ex){
        throw new NotAnAnnotationException(ex.getMessage());
      }
    } else{
      annotationString = rawAnnotation;
    }
    checkSchemaValidity(annotationString, format, FormattableObject.Type.ANNOTATION);
    // If code is reached, everything was fine.
    AnnotationList list = getModelFactory().createAnnotationList(annotationString, format);
    list.setContainerIri(containerIri);
    if(list.size() > 1 && !getWapServerConfig().isMultipleAnnotationPostAllowed()){
      throw new MethodNotAllowedException("Multiple annotation posting is disabled");
    }
    for(Annotation annotation : list.getAnnotations()){
      // Validity checks only assure that field values are in the expected format, but they do not ensure
      // existence. Therefore it must be ensured that these basic values exist here. This is always done.
      if(annotation.getIri() == null){
        // It may be that the annotation cannot be parsed if it has no id field,
        // but this additional check is not performance relevant.
        throw new NotAnAnnotationException();
      }
      if(!annotation.hasTarget()){
        throw new NotAnAnnotationException("The Annotation has no Target.");
      }
      // Create new ID based on containerIri and a uuid.
      UUID uuid = UUID.randomUUID();
      annotation.setIri(containerIri + uuid.toString());
      // Add created property if not existent.
      annotation.setCreated();
      // Update last modified - automatically done with ETag update annotation.updateModified();
      // ETag will be generated on changes in the repository. annotation.setEtag(generateEtag());
    }
    String newContainerEtag = etagFactory.generateEtag();
    // Store container IRI for later use
    list.setContainerIri(list.iterator().next().getContainerIri());
    repository.writeRdfTransaction((ds) -> {
      for(Annotation a : list){
        addAnnotation(a);
      }
      updateEtag(list.getContainerIri(), newContainerEtag);
      list.setContainerEtag(newContainerEtag);
    });
    return list;
  }

  @Override
  public Annotation addAnnotation(Annotation a){
    //repository.writeRdfTransaction((ds) -> {
    String iriString = a.getIriString();
    if(isIriDeleted(iriString)){
      throw new ResourceDeletedException(
              "The Annotation '" + iriString + "' already existed and can not be recreated.");
    }
    if(containsIri(iriString)){
      throw new ResourceExistsException("The annotation with IRI '" + iriString
              + "' already exists in the database. Please check an try PUT to update.");
    }
    writeWapObjectToDb(a);
    repository.addElementToRdfSeq(a.getContainerIri(), Container.toAnnotationSeqIriString(a.getContainerIri()), iriString);
    // Set new eTag
    updateEtag(a.getContainerIri(), etagFactory.generateEtag());
    //   });
    return a;
  }
}
