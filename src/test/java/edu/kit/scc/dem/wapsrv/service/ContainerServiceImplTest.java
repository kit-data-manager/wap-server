package edu.kit.scc.dem.wapsrv.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import edu.kit.scc.dem.wapsrv.app.EtagFactory;
import edu.kit.scc.dem.wapsrv.app.EtagFactoryMock;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.app.WapServerConfigMock;
import edu.kit.scc.dem.wapsrv.exceptions.ContainerNotEmptyException;
import edu.kit.scc.dem.wapsrv.exceptions.InvalidContainerException;
import edu.kit.scc.dem.wapsrv.exceptions.ResourceDeletedException;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.ContainerPreference;
import edu.kit.scc.dem.wapsrv.model.ModelFactory;
import edu.kit.scc.dem.wapsrv.model.Page;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.formats.JsonLdProfileRegistry;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfUtilities;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.DcTermsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.LdpVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfSchemaVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.WapVocab;
import edu.kit.scc.dem.wapsrv.model.validators.JsonLdValidator;
import edu.kit.scc.dem.wapsrv.model.validators.Validator;
import edu.kit.scc.dem.wapsrv.model.validators.ValidatorRegistry;
import edu.kit.scc.dem.wapsrv.repository.CollectedRepository;
import edu.kit.scc.dem.wapsrv.repository.RepositoryMock;
import edu.kit.scc.dem.wapsrv.testscommon.ModelFactoryMock;

/**
 * Tests the class ContainerServiceImpl
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
// @DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ContainerServiceImpl.class, RepositoryMock.class, ModelFactoryMock.class,
      EtagFactoryMock.class, JsonLdProfileRegistry.class, ValidatorRegistry.class, WapServerConfigMock.class,
      JsonLdValidator.class, Validator.class})
@ExtendWith(HoverflyExtension.class)
@HoverflySimulate(source = @HoverflySimulate.Source(value = "w3c_simulation.json", type = HoverflySimulate.SourceType.DEFAULT_PATH))
@Configuration
@ActiveProfiles("test")
class ContainerServiceImplTest {
   private static final String ROOT_IRI = "http://www.example.org/wap/";
   @Autowired
   private ContainerService containerService;
   @Autowired
   private CollectedRepository wapObjectRepositoryMock;
   @SuppressWarnings("unused")
   @Autowired
   private JsonLdProfileRegistry jsonLdProfileRegistry;
   @Autowired
   private WapServerConfig wapServerConfigMock;
   @Autowired
   private ModelFactory modelFactoryMock;
   @SuppressWarnings("unused")
   @Autowired
   private ValidatorRegistry validatorRegistry;
   @Autowired
   private EtagFactory etagFactoryMock;
   private SimpleRDF simpleRDF;
   private Dataset dataset;

   /**
    * Tests deleting a container.
    */
   @Test
   final void testDeleteContainer() {
      when(wapObjectRepositoryMock.countElementsInSeq(any(), any())).thenReturn(0);
      when(etagFactoryMock.generateEtag()).thenReturn("test etag");
      containerService.deleteContainer(ROOT_IRI, "test etag");
      when(wapObjectRepositoryMock.countElementsInSeq(any(), any())).thenReturn(5);
      when(etagFactoryMock.generateEtag()).thenReturn("test etag");
      assertThrows(ResourceDeletedException.class, () -> {
         containerService.deleteContainer(ROOT_IRI, "test etag");
      });
      dataset.remove(null, null, WapVocab.deleted, null);
      assertThrows(ContainerNotEmptyException.class, () -> {
         containerService.deleteContainer(ROOT_IRI, "test etag");
      });
      // check if annotations will be deleted
      List<String> testList = new ArrayList<String>();
      testList.add(ROOT_IRI);
      testList.add(ROOT_IRI);
      testList.add(ROOT_IRI);
      when(wapObjectRepositoryMock.countElementsInSeq(any(), any())).thenReturn(0);
      when(wapObjectRepositoryMock.getAllObjectIrisOfSeq(ROOT_IRI, Container.toAnnotationSeqIriString(ROOT_IRI)))
            .thenReturn(testList);
      Mockito.clearInvocations(wapObjectRepositoryMock);
      containerService.deleteContainer(ROOT_IRI, "test etag");
      // 3 times the Annotation 1 time the Container
      verify(wapObjectRepositoryMock, times(1)).removeElementFromRdfSeq(any(), any(), any());
      verify(wapObjectRepositoryMock, times(1)).emptySeq(any(), any());
   }

   /**
    * Setup test.
    */
   @BeforeEach
   void setupTest() {
      simpleRDF = new SimpleRDF();
      dataset = simpleRDF.createDataset();
      IRI root = simpleRDF.createIRI(ROOT_IRI);
      dataset.add(root, root, RdfVocab.type, LdpVocab.basicContainer);
      dataset.add(root, root, DcTermsVocab.modified,
            RdfUtilities.rdfLiteralFromCalendar(Calendar.getInstance(), simpleRDF));
      dataset.add(root, root, RdfSchemaVocab.label, simpleRDF.createLiteral("label"));
      dataset.add(root, root, WapVocab.etag, simpleRDF.createLiteral("test etag"));
      when(wapObjectRepositoryMock.getRdf()).thenReturn(simpleRDF);
      when(wapObjectRepositoryMock.getTransactionDataset()).thenReturn(dataset);
      when(wapObjectRepositoryMock.getWapObject(ROOT_IRI)).thenReturn(dataset);
      when(modelFactoryMock.createContainer(any(Dataset.class), any(boolean.class), any(boolean.class)))
            .thenReturn(mock(Container.class));
      when(wapServerConfigMock.getPageSize()).thenReturn(10);
      Page pageMock = mock(Page.class);
      when(modelFactoryMock.createPage(any(Dataset.class), eq(ROOT_IRI), eq(0), any(boolean.class), any(boolean.class),
            any(int.class), any(String.class), eq("label"))).thenReturn(pageMock);
      when(pageMock.getContainerIri()).thenReturn(ROOT_IRI);
      Dataset pageDataset = simpleRDF.createDataset();
      pageDataset.getGraph().add(root, WapVocab.deleted, WapVocab.deleted);
      when(pageMock.getDataset()).thenReturn(pageDataset);
   }

   /**
    * Tests getting a container.
    */
   @Test
   final void testGetContainer() {
      Mockito.clearInvocations(modelFactoryMock);
      Mockito.clearInvocations(wapObjectRepositoryMock);
      // test get root container minimal
      String paramContainerIri = ROOT_IRI;
      Set<Integer> paramPreferences = new HashSet<Integer>();
      paramPreferences.add(ContainerPreference.PREFER_MINIMAL_CONTAINER);
      containerService.getContainer(paramContainerIri, paramPreferences);
      verify(modelFactoryMock).createContainer(any(Dataset.class), eq(true), any(boolean.class));
      when(wapObjectRepositoryMock.countElementsInSeq(any(), any())).thenReturn(5);
      // test get root container not minimal
      Set<Integer> paramPrefNotMinimal = new HashSet<Integer>();
      containerService.getContainer(paramContainerIri, paramPrefNotMinimal);
      verify(modelFactoryMock).createPage(any(Dataset.class), eq(ROOT_IRI), eq(0), eq(false), any(boolean.class), eq(5),
            any(String.class), eq("label"));
      when(wapObjectRepositoryMock.countElementsInSeq(any(), any())).thenReturn(0);
      // test get root container not minimal
      containerService.getContainer(paramContainerIri, paramPrefNotMinimal);
   }

   /**
    * Test to post a container.
    */
   @Test
   final void testPostContainer() {
      Container containerMock = mock(Container.class);
      when(modelFactoryMock.createContainer(any(String.class), eq(Format.JSON_LD), any(String.class)))
            .thenReturn(containerMock);
      when(containerMock.getIriString()).thenReturn(ROOT_IRI + "test/");
      when(etagFactoryMock.generateEtag()).thenReturn("test etag");
      Container retContainer = containerService.postContainer(ROOT_IRI, "Test0-_9", "{}", Format.JSON_LD);
      assertNotNull(retContainer);
      when(containerMock.getLabel()).thenReturn("label");
      when(containerMock.getIriString()).thenReturn(ROOT_IRI + "test2/");
      retContainer = containerService.postContainer(ROOT_IRI, "Test0-_9", "{}", Format.JSON_LD);
      assertNotNull(retContainer);
      when(containerMock.getLabel()).thenReturn(null);
      when(containerMock.getIriString()).thenReturn(ROOT_IRI + "test3/");
      when(wapServerConfigMock.isLabelMandatoryInContainers()).thenReturn(true);
      assertThrows(InvalidContainerException.class, () -> {
         containerService.postContainer(ROOT_IRI, "Test0-_9", "{}", Format.JSON_LD);
      });
   }

   /**
    * Tests getting a page.
    */
   @Test
   final void testGetPage() {
      when(wapObjectRepositoryMock.countElementsInSeq(any(), any())).thenReturn(5);
      ArrayList<String> iriList = new ArrayList<String>();
      iriList.add(ROOT_IRI);
      iriList.add(ROOT_IRI);
      iriList.add(ROOT_IRI);
      iriList.add(ROOT_IRI);
      iriList.add(ROOT_IRI);
      when(wapObjectRepositoryMock.getRangeOfObjectIrisFromSeq(ROOT_IRI, Container.toAnnotationSeqIriString(ROOT_IRI),
            1, 5)).thenReturn(iriList);
      containerService.getPage(ROOT_IRI, ContainerPreference.PREFER_CONTAINED_IRIS, 0);
      containerService.getPage(ROOT_IRI, 0, 0);
   }
}
