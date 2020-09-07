package edu.kit.scc.dem.wapsrv.model.rdf;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.simple.Types;
import edu.kit.scc.dem.wapsrv.app.WapServerConfig;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.LdpVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;

/**
 * The class is used to generate the correct output representation of the Container. It extends the Container class and
 * overrides functions to generate the dataset for the output
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfOutputContainer extends RdfContainer {
   /**
    * Instantiates a new RDF output container.
    *
    * @param dataset
    *                               the dataset with the RDF data from the database
    * @param preferMinimalContainer
    *                               true, if prefer minimal container was requested
    * @param preferIrisOnly
    *                               true, if prefer iris only was requested
    * @param rdfBackend
    *                               the RDF backend to be used
    */
   public RdfOutputContainer(Dataset dataset, boolean preferMinimalContainer, boolean preferIrisOnly,
         RdfBackend rdfBackend) {
      super(dataset, preferMinimalContainer, preferIrisOnly, rdfBackend);
      if (preferIrisOnly) {
         setIri(getIriString() + "?iris=1", false);
      } else {
         setIri(getIriString() + "?iris=0", false);
      }
   }

   /*
    * (non-Javadoc)
    * @see edu.kit.scc.dem.wapsrv.model.rdf.RdfWapObject#getDataset()
    */
   @Override
   public Dataset getDataset() {
      // Because the sequence head is also included we need to substract 1
      long annoCount = dataset.getGraph().stream(Container.toAnnotationSeqIri(iri), null, null).count() - 1;
      Literal annoCountLiteral
            = rdfBackend.getRdf().createLiteral(String.valueOf(annoCount), Types.XSD_NONNEGATIVEINTEGER);
      dataset.getGraph().add(iri, AsVocab.totalItems, annoCountLiteral);
      // Don't show first and last if there are no Annotation, hence no page.
      if (annoCount != 0) {
         int pageCount
               = (int) (Math.floor((annoCount - 1.0f) / (WapServerConfig.getInstance().getPageSize() + 0.0f)) + 1);
         IRI firstIri = rdfBackend.getRdf().createIRI(getIriString() + "&page=0");
         int lastPage = pageCount == 0 ? 0 : pageCount - 1;
         IRI lastIri = rdfBackend.getRdf().createIRI(getIriString() + "&page=" + lastPage);
         dataset.getGraph().add(iri, AsVocab.first, firstIri);
         dataset.getGraph().add(iri, AsVocab.last, lastIri);
      }
      // Do not add contains in minimalContainer
      if (!preferMinimalContainer) {
         // Add subcontainer in contains according to the sequence
         dataset.getGraph().stream(Container.toContainerSeqIri(iri), null, null).forEach(t -> {
            if (!t.getObject().equals(RdfVocab.seq)) {
               dataset.getGraph().add(iri, LdpVocab.contains, t.getObject());
            }
         });
      }
      // Delete sequence for container
      dataset.getGraph().remove(Container.toContainerSeqIri(iri), null, null);
      // Delete sequence for annotations
      dataset.getGraph().remove(Container.toAnnotationSeqIri(iri), null, null);
      return super.getDataset();
   }
}
