package edu.kit.scc.dem.wapsrv.repository.jena;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.rdf.api.RDF;
import org.apache.jena.commonsrdf.JenaCommonsRDF;
import org.apache.jena.commonsrdf.impl.JenaDataset;
import org.apache.jena.commonsrdf.JenaRDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.tdb2.DatabaseMgr;
import org.springframework.beans.factory.annotation.Autowired;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.exceptions.NotExistentException;
import edu.kit.scc.dem.wapsrv.model.WapObject;
import edu.kit.scc.dem.wapsrv.model.rdf.RdfBackend;
import edu.kit.scc.dem.wapsrv.repository.CollectedRepository;
import edu.kit.scc.dem.wapsrv.repository.TransactionRepository;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * The Jena repository implementation. <br>
 * IMPORTANT: Only the externally called functions are intercepted! Make sure to
 * add @JenaTransaction Annotation with the needed Type in front of every called
 * Method.
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
@Repository("jena")
@Primary
public class JenaRepository extends CollectedRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Dataset dataBase;
    /**
     * Database
     */
    @Autowired
    private JenaDataBase dataBaseSource;
    /**
     * The application configuration to use
     */
    @Autowired
    private WapServerConfig wapServerConfig;
    /**
     * The RDF backend
     */
    @Autowired
    private RdfBackend rdfBackend;

    /**
     * Sets the configuration to use
     *
     * @param wapServerConfig The application configuration
     */
    @Autowired
    public void setWapServerConfig(WapServerConfig wapServerConfig) {
        this.wapServerConfig = wapServerConfig;
    }

    @PostConstruct
    private void init() {
        if (wapServerConfig == null) {
            throw new RuntimeException("WapServerConfiguration not set in JenaRepository");
        }
        if (dataBaseSource != null) {
            setDataBase(dataBaseSource.getDataBase());
        }
    }

    @Override
    public org.apache.commons.rdf.api.Dataset getWapObject(String iri) {
        org.apache.commons.rdf.api.Dataset retDs = rdfBackend.getRdf().createDataset();
        JenaDataset jenaDs = (JenaDataset) retDs;
        if (!dataBase.containsNamedModel(iri)) {
            throw new NotExistentException("the requested container does not exist");
        }
        Model readModel = dataBase.getNamedModel(iri);
        readModel.listStatements().forEachRemaining(s -> {
            jenaDs.getDataset().getDefaultGraph().add(s.asTriple());
        });
        return retDs;
    }

    @Override
    public String backupDatabase() {
        DatabaseMgr.backup(dataBase.asDatasetGraph());
        Path path = FileSystems.getDefault().getPath(WapServerConfig.getInstance().getDataBasePath() + "/Backups/");
        return path.toUri().toString();
    }

    /**
     * Gets the database dataset.
     *
     * @return the dataBase dataset
     */
    @Override
    public Dataset getDataBase() {
        return dataBase;
    }

    /**
     * Sets the database dataset.
     *
     * @param dataBase the dataBase dataset to set
     */
    public void setDataBase(Dataset dataBase) {
        this.dataBase = dataBase;
    }

    @Override
    public boolean beginTransaction(TransactionRepository.Type type) {
        if (!dataBase.isInTransaction()) {
            log.trace("Beginning {} transaction.", type);
            dataBase.begin(translateType(type));
            log.trace("{} transaction now active.", type);
            return true;
        }
        return false;
    }

    @Override
    public void abortTransaction() {
        if (dataBase.isInTransaction()) {
            log.trace("Aborting {} transaction.");
            dataBase.abort();
            log.trace("Transaction aborted.");
            log.trace("Ending dataset.");
            dataBase.end();
            log.trace("Dataset ended.");
        }
    }

    @Override
    public void endTransaction(boolean wasOpend) {
        if (wasOpend) {
            try {
                if (dataBase.isInTransaction()) {
                    log.trace("Committing dataset.");
                    dataBase.commit();
                    log.trace("Dataset committed.");
                }
            } finally {
                log.trace("Ending dataset.");
                dataBase.end();
                log.trace("Dataset ended.");
            }
        }
    }

    private ReadWrite translateType(TransactionRepository.Type type) {
        if (type.equals(TransactionRepository.Type.Read)) {
            return ReadWrite.READ;
            //return TxnType.READ;
        }
        if (type.equals(TransactionRepository.Type.Write)) {
            return ReadWrite.WRITE;
            //return TxnType.WRITE;
        }
        return null;
    }

    @Override
    public RDF getRdf() {
        return rdfBackend.getRdf();
    }

    @Override
    public void addElementToRdfSeq(String modelIri, String seqIri, String objIri) {
        Model model = dataBase.getNamedModel(modelIri);
        Seq seq = model.getSeq(seqIri);
        Resource objectResource = model.getResource(objIri);
        seq.add(objectResource);
    }

    @Override
    public void removeElementFromRdfSeq(String modelIri, String seqIri, String objIri) {
        Model model = dataBase.getNamedModel(modelIri);
        Seq seq = model.getSeq(seqIri);
        Resource objectResource = model.getResource(objIri);
        int indexOfContainer = seq.indexOf(objectResource);
        if (indexOfContainer > 0) {
            seq.remove(indexOfContainer);
        }
    }

    @Override
    public int countElementsInSeq(String modelIri, String seqIri) {
        Seq seq = dataBase.getNamedModel(modelIri).getSeq(seqIri);
        return seq.size();
    }

    @Override
    public void writeObjectToDatabase(WapObject wapObject) {
        org.apache.commons.rdf.api.Dataset dataset = wapObject.getDataset();
        JenaDataset jenaDs = (JenaDataset) dataset;
        Graph jenaGraph = jenaDs.getDataset().getDefaultGraph();
        Model jenaModel = org.apache.jena.rdf.model.ModelFactory.createModelForGraph(jenaGraph);
        String iriString = wapObject.getIriString();
        dataBase.addNamedModel(iriString, jenaModel);
        // Model returnValue = dataBase.getNamedModel(iriString);
    }

    @Override
    public List<String> getRangeOfObjectIrisFromSeq(String containerIri, String seqIri, int firstIndex, int lastIndex) {
        List<String> retValue = new ArrayList<>();
        Model containerModel = dataBase.getNamedModel(containerIri);
        Seq annoSeq = containerModel.getSeq(seqIri);
        for (int i = firstIndex; i <= lastIndex; i++) {
            String annotationIri = annoSeq.getObject(i).asResource().toString();
            retValue.add(annotationIri);
        }
        return retValue;
    }

    @Override
    public List<String> getAllObjectIrisOfSeq(String modelIri, String seqIri) {
        Model containerModel = dataBase.getNamedModel(modelIri);
        Seq annoSeq = containerModel.getSeq(seqIri);
        return getRangeOfObjectIrisFromSeq(modelIri, seqIri, 1, annoSeq.size());
    }

    @Override
    public org.apache.commons.rdf.api.Dataset getTransactionDataset() {
        JenaRDF jenaRDF = (JenaRDF) rdfBackend.getRdf();

        return JenaCommonsRDF.fromJena(dataBase.asDatasetGraph());
    }

    @Override
    public void emptySeq(String modelIri, String seqIri) {
        Model model = dataBase.getNamedModel(modelIri);
        Resource subject = model.createResource(seqIri);
        model.removeAll(subject, null, null);
        // regenerate the seq.
        model.createSeq(seqIri);
    }
}
