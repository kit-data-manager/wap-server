package edu.kit.scc.dem.wapsrv.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.Literal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.kit.scc.dem.wapsrv.app.ErrorMessageRegistry;
import edu.kit.scc.dem.wapsrv.exceptions.ContainerNotEmptyException;
import edu.kit.scc.dem.wapsrv.exceptions.FormatException;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidContainerException;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidRequestException;
import edu.kit.scc.dem.wapsrv.exceptions.NotAContainerException;
import edu.kit.scc.dem.wapsrv.exceptions.NotExistentException;
import edu.kit.scc.dem.wapsrv.exceptions.ResourceDeletedException;
import edu.kit.scc.dem.wapsrv.exceptions.ResourceExistsException;
import edu.kit.scc.dem.wapsrv.exceptions.WapException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.ContainerPreference;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.Page;
import edu.kit.scc.dem.wapsrv.model.WapObject;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfUtilities;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.DcTermsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfSchemaVocab;
import edu.kit.scc.dem.wapsrv.repository.ContainerRepository;

/**
 * A general implementation of the container service interface. It is used by
 * the controllers as an interface to the main application. It uses the
 * repository and the model factory to create instances of model objects and
 * manipulate them according to the clients requests prior to storing them in
 * the repository or returning them to the controllers for responses to the
 * clients.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@Service
public class ContainerServiceImpl extends AbstractWapService implements ContainerService{

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * The container service
   */
  @Autowired
  private ContainerRepository repository;
  /**
   * The JsonLdProfileRegistry, autowired
   */
  @Autowired
  private JsonLdProfileRegistry jsonLdProfileRegistry;

  @Override
  public void deleteContainer(String iri, String etag) throws WapException{
    log.info("deleting Container: '" + iri + "' with given etag: '" + etag + "'");
    repository.writeRdfTransaction(ds -> {
      checkExistsAndNotDeleted(iri);
      checkEtag(iri, etag);
      // Check if the container has subcontainer.
      if(hasSubcontainers(iri)){
        log.warn("Container: '" + iri + "' has subcontainers, deleting aborted.");
        throw new ContainerNotEmptyException("The container has subcontainers and cannot be deleted");
      }
      deleteObject(iri, Container.toContainerSeqIriString(WapObject.getParentContainerIriString(iri)));
      List<String> annoIris = repository.getAllObjectIrisOfSeq(iri, Container.toAnnotationSeqIriString(iri));
      log.info("deleting " + annoIris.size() + " annotations form Container: '" + iri + "'");
      deleteObjectBulk(annoIris);
      repository.emptySeq(iri, Container.toAnnotationSeqIriString(iri));
    });
    log.info("deleting of Container: '" + iri + "' was finished with success");
  }

  @Override
  public Container getContainer(String containerIri, Set<Integer> preferences) throws WapException{
    log.info("Get Container from DB: '" + containerIri + "'");
    final boolean preferMinimalContainer = ContainerPreference.isPreferMinimalContainer(preferences);
    final boolean preferIrisOnly = ContainerPreference.isPreferContainedIRIs(preferences);
    checkExistsAndNotDeleted(containerIri);
    Dataset[] retDs = new Dataset[1];
    repository.readRdfTransaction(ds -> {
      Dataset containerDataset = repository.getWapObject(containerIri);
      if(!preferMinimalContainer){
        // Need a page from database depending on preferIrisOnly
        try{
          Page page = getPage(containerIri, 0, preferIrisOnly, true);
          // Add the page to the container data set
          page.getDataset().getGraph().iterate().forEach(t -> {
            containerDataset.getGraph().add(t);
          });
        } catch(NotExistentException e){
          log.warn("the requested page does not exist: '" + containerIri + "' page 0");
        }
      }
      retDs[0] = containerDataset;
    });
    log.info("Get Container finished: '" + containerIri + "'");
    return modelFactory.createContainer(retDs[0], preferMinimalContainer, preferIrisOnly);
    // No exception ==> container exists
    // The container now contains only the basic information.
    // No page is embedded.
    // If empty no first/last properties are set
  }

  @Override
  public Container postContainer(String baseContainerIri, String name, String rawContainer, Format format)
          throws WapException{
    log.info("Post Container to: '" + baseContainerIri + "' in Format: '" + format + "'");
    final boolean overwriteIfDeleted = name != null; // If slug is given, we overwrite deleted containers
    if(overwriteIfDeleted){
      log.info("Slug used: '" + name + "'");
    }
    String newName = name;
    // Check if parent container exists
    if(newName == null){
      // the client provided none, but the server is set to autocreate
      UUID uuid = UUID.randomUUID();
      newName = uuid.toString();
      log.info("using autocreated name, no slug was provided: '" + newName + "'");
    }
    if(!isValidName(newName)){
      log.warn("the provided slug name is not valid: '" + newName + "'");
      throw new InvalidRequestException("Invalid characters in container name");
    }
    checkSchemaValidity(rawContainer, format, FormattableObject.Type.CONTAINER);
    // If code is reached, everything was fine.
    // check the parent container
    checkExistsAndNotDeleted(baseContainerIri);
    // If format is JSON_LD, expand it.
    String containerString = null;
    if(format == Format.JSON_LD){
      log.info("expanding JSONLD");
      containerString = jsonLdProfileRegistry.expandJsonLd(rawContainer);
      try{
        containerString = assertValidIds(containerString);
      } catch(FormatException ex){
        log.warn("some ids in the JSONLD file are not valid.");
        throw new NotAContainerException(ex.getMessage());
      }
    } else{
      containerString = rawContainer;
    }
    final String containerIri = baseContainerIri + newName + "/";
    if(containsIri(containerIri)){
      if(isIriDeleted(containerIri)){
        if(overwriteIfDeleted){
          // Slug was given, we may overwrite deleted containers, go on
          log.info("Slug used to recreate deleted container: '" + containerIri + "'. Deleting old data.");
          repository.writeRdfTransaction(ds -> {
            Optional<BlankNodeOrIRI> node = Optional.of(repository.getRdf().createIRI(containerIri));
            ds.remove(node, null, null, null);
          });
        } else{
          throw new ResourceDeletedException(
                  "A container with that IRI once existed " + "and is now deleted. Recreation is forbidden.");
        }
      } else{
        throw new ResourceExistsException("A container with that IRI already exists");
      }
    }
    Container container = getModelFactory().createContainer(containerString, format, containerIri);
    // No exception ==> everything fine
    // To backup the container IRI provided is not needed because it is not documented in the
    // specification.
    // The container will almost never have an IRI already that fits to his final destination.
    // the renaming should now be done in the containers constructor
    container.setIri(containerIri, false);
    if(container.getLabel() == null){
      if(getWapServerConfig().isLabelMandatoryInContainers()){
        log.warn("Settings request a label in the container.");
        throw new InvalidContainerException("label property is mandatory for containers");
      } else{
        container.createDefaultLabel();
      }
    }
    repository.writeRdfTransaction(ds -> {
      if(containsIri(container.getIriString())){
        throw new ResourceExistsException(
                "A container with the IRI: '" + container.getIriString() + "' already exists in the Database.");
      }
      writeWapObjectToDb(container);
      repository.addElementToRdfSeq(baseContainerIri, Container.toContainerSeqIriString(baseContainerIri),
              container.getIriString());
      // Update ETag of parent
      updateEtag(baseContainerIri, etagFactory.generateEtag());
    });
    log.info("Post Container for: '" + containerIri + "' successful.");
    // If no exception, everything is fine.
    return container;
  }

  @Override
  public Page getPage(String containerIri, int containerPreference, int pageNr) throws WapException{
    final boolean preferIrisOnly = ContainerPreference.PREFER_CONTAINED_IRIS == containerPreference;
    // default is not embedded
    return getPage(containerIri, pageNr, preferIrisOnly, false);
  }

  private Page getPage(String containerIri, int pageNr, boolean preferIrisOnly, boolean isEmbedded)
          throws WapException{
    log.info("Get Page of Container: '" + containerIri + "' page Nr: '" + pageNr + "'");
    int pageSize = wapServerConfig.getPageSize();
    Dataset retDs = repository.getRdf().createDataset();
    BlankNodeOrIRI containerNode = repository.getRdf().createIRI(containerIri);
    Page[] page = new Page[1];
    // No exception ==> page exists
    repository.readRdfTransaction(ds -> {
      // Check if container exists and has not been deleted
      checkExistsAndNotDeleted(containerIri);
      // Get total count of annotations
      Graph graph = ds.getGraph(containerNode).get();
      int annoTotalCount = repository.countElementsInSeq(containerIri, Container.toAnnotationSeqIriString(containerIri));

      //just moved up
      int firstAnnotationIndex = (pageNr * pageSize) + 1;
      int lastAnnotationIndex = Math.min(firstAnnotationIndex + pageSize - 1, annoTotalCount);
      // If the annotation starts at an index behind the size of the annotation sequence
      // an exception must be thrown.
      if(firstAnnotationIndex > annoTotalCount){
        throw new NotExistentException(ErrorMessageRegistry.PAGE_NOT_EXISTENT + " : " + pageNr + " in container " + containerIri);
      }
      //end of modification

      Literal objectOfModified = (Literal) graph.stream(containerNode, DcTermsVocab.modified, null).findFirst().get().getObject();
      String modifiedString = objectOfModified.getLexicalForm();
      String labelString = RdfUtilities.nStringToString(graph.stream(containerNode, RdfSchemaVocab.label, null).findFirst().get().getObject().ntriplesString());
      page[0] = modelFactory.createPage(retDs, containerIri, pageNr, preferIrisOnly, isEmbedded, annoTotalCount, modifiedString, labelString);
      // Read the annotation list with the correct size
      // ATTENTION: the index of the sequence does not start with 0 as usual
      // Keep that in mind in the rest of this method

///Do this earlier might avoid accessing empty optionals
//      int firstAnnotationIndex = (pageNr * pageSize) + 1;
//      int lastAnnotationIndex = Math.min(firstAnnotationIndex + pageSize - 1, annoTotalCount);
//      // If the annotation starts at an index behind the size of the annotation sequence
//      // an exception must be thrown.
//      if(firstAnnotationIndex > annoTotalCount){
//        throw new NotExistentException(ErrorMessageRegistry.PAGE_NOT_EXISTENT + " : " + pageNr + " in container " + containerIri);
//      }
      // repository.addAnnotationsToPage(page[0], firstAnnotationIndex, lastAnnotationIndex, preferIrisOnly);
      String containerIriPage = page[0].getContainerIri();
      List<String> annoIris = repository.getRangeOfObjectIrisFromSeq(containerIriPage,
              Container.toAnnotationSeqIriString(containerIriPage), firstAnnotationIndex, lastAnnotationIndex);
      annoIris.forEach(iri -> {
        if(preferIrisOnly){
          page[0].addAnnotationIri(iri);
        } else{
          Annotation annotation = getAnnotation(iri);
          page[0].addAnnotation(annotation);
        }
      });
      page[0].closeAdding();
    });
    log.info("Get Page for Container: '" + containerIri + "' successful.");
    return page[0];
  }

  /**
   * Checks if a given name is valid for containers. Allowed are small letters,
   * large letters, _ , - and numbers
   *
   * @param name The name to check
   * @return true if valid
   */
  private boolean isValidName(String name){
    // A regex might be more elegant, but definitely not faster
    for(char c : name.toCharArray()){
      // small letters
      if(c >= 'a' && c <= 'z'){
        continue;
      }
      // capital letters
      if(c >= 'A' && c <= 'Z'){
        continue;
      }
      // numbers
      if(c >= '0' && c <= '9'){
        continue;
      }
      // and the two scores
      if(c != '-' && c != '_'){
        return false;
      }
    }
    return true;
  }

  private boolean hasSubcontainers(String iri){
    log.info("checking for subcontainer: '" + iri + "'");
    Boolean[] result = new Boolean[1];
    repository.readRdfTransaction(ds -> {
      result[0] = (repository.countElementsInSeq(iri, Container.toContainerSeqIriString(iri)) != 0);
    });
    log.info("check for hasSubcontainer: '" + iri + "' is: '" + result[0] + "'");
    return result[0];
  }
}
