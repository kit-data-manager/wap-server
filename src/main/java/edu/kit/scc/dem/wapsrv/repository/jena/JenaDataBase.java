package edu.kit.scc.dem.wapsrv.repository.jena;

import java.util.Calendar;
import jakarta.annotation.PostConstruct;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.tdb2.TDB2Factory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.DcTermsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.LdpVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfSchemaVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.WapVocab;

/**
 * This Class initializes the DataBase in TDB2 and Jena.
 *
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Component
public class JenaDataBase {
   /** The wap server config. */
   @Autowired
   private WapServerConfig wapServerConfig;
   private Dataset dataBase;

   /**
    * Instantiates a new Jena database. The database folder is either autowired or has to be manually set with
    * {@link #init(String, String)} if Spring autowiring is not active.
    */
   public JenaDataBase() {
   }

   /**
    * Instantiates a new jena database at the given path with the given root container IRI
    *
    * @param path
    *                 the path of the database, if not exists it will be created. If null an in memory database will be
    *                 created
    * @param rootName
    *                 The IRI of the root container
    */
   public void init(String path, String rootName) {
      if (path != null) {
         dataBase = TDB2Factory.connectDataset(path);
      } else {
         dataBase = TDB2Factory.createDataset();
      }
      // Check if file is new, if new init database
      dataBase.begin(ReadWrite.WRITE);
      Model model = dataBase.getNamedModel(rootName);
      LoggerFactory.getLogger(getClass()).info(
            "******before init*******  Triples # in DB: " + dataBase.getUnionModel().listStatements().toList().size());
      model.listStatements().forEachRemaining(s -> {
         System.out.println(s.asTriple().getSubject().getURI() + " " + s.asTriple().getPredicate().getURI() + " "
               + s.asTriple().getObject().toString());
      });
      Resource root = model.getResource(rootName);
      Property predicate = model.getProperty(RdfVocab.type.getIRIString());
      Resource objectBasicContainer = model.getResource(LdpVocab.basicContainer.getIRIString());
      Resource objectOrderedCollection = model.getResource(AsVocab.orderedCollection.getIRIString());
      Seq contSeq = model.getSeq(Container.toContainerSeqIriString(root.getURI()));
      Seq annoSeq = model.getSeq(Container.toAnnotationSeqIriString(root.getURI()));
      Property etagProperty = model.getProperty(WapVocab.etag.getIRIString());
      Property modifiedProperty = model.getProperty(DcTermsVocab.modified.getIRIString());
      Property labelProperty = model.getProperty(RdfSchemaVocab.label.getIRIString());
      if (!root.hasProperty(predicate, objectBasicContainer) || !root.hasProperty(predicate, objectOrderedCollection)
            || contSeq == null || annoSeq == null || !root.hasProperty(etagProperty)
            || !root.hasProperty(modifiedProperty) || !root.hasProperty(labelProperty)) {
         // Initiate database
         Model modelNew = ModelFactory.createDefaultModel();
         modelNew.createResource(rootName, objectBasicContainer);
         modelNew.createResource(rootName, objectOrderedCollection);
         modelNew.createSeq(Container.toContainerSeqIriString(rootName));
         modelNew.createSeq(Container.toAnnotationSeqIriString(rootName));
         modelNew.addLiteral(root, etagProperty, modelNew.createLiteral("initial-root-etag"));
         Literal datetime = modelNew.createTypedLiteral(Calendar.getInstance());
         modelNew.addLiteral(root, modifiedProperty, datetime);
         modelNew.addLiteral(root, labelProperty, modelNew.createLiteral("The Root Container"));
         dataBase.replaceNamedModel(rootName, modelNew);
         LoggerFactory.getLogger(getClass()).info(
               "******after init******* Triples # in DB: " + dataBase.getUnionModel().listStatements().toList().size());
         model.listStatements().forEachRemaining(s -> {
            System.out.println(s.asTriple().getSubject().getURI() + " " + s.asTriple().getPredicate().getURI() + " "
                  + s.asTriple().getObject().toString());
         });
      }
      dataBase.commit();
      dataBase.end();
   }

   /**
    * Gets the database dataset.
    *
    * @return the database dataset
    */
   public Dataset getDataBase() {
      return dataBase;
   }

   @PostConstruct
   private void initAfterAutowiring() {
      init(wapServerConfig.getDataBasePath(), wapServerConfig.getRootContainerIri());
   }
}
