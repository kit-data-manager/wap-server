package edu.kit.scc.dem.wapsrv.model.rdf;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.kit.scc.dem.wapsrv.exceptions.NotAnAnnotationException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.AnnotationList;
import edu.kit.scc.dem.wapsrv.model.Container;
import edu.kit.scc.dem.wapsrv.model.ModelFactory;
import edu.kit.scc.dem.wapsrv.model.Page;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AnnoVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;

/**
 * The RdfModelFactory implementation. Relies on an actual {@link RDF} implementation.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
@Component
public class RdfModelFactory implements ModelFactory {
   /**
    * The underlying RDF
    */
   private RDF rdf;
   /**
    * The used RDF backend
    */
   private RdfBackend rdfBackend;

   /**
    * Sets the RDF backend via autowire
    * 
    * @param rdfBackend
    *                   The RDF backend
    */
   @Autowired
   private void setRdfBackend(RdfBackend rdfBackend) {
      this.rdfBackend = rdfBackend;
      this.rdf = rdfBackend.getRdf();
   }

   @Override
   public AnnotationList createAnnotationList(String rawAnnotation, Format format) {
      AnnotationList annotationList = new RdfAnnotationList(rdfBackend);
      Dataset dataset = rdfBackend.readFromString(rawAnnotation, format);
      Set<BlankNodeOrIRI> annotationIris = new HashSet<BlankNodeOrIRI>();
      Iterable<Quad> iterator = dataset.iterate(null, null, RdfVocab.type, AnnoVocab.annotation);
      for (Quad quad : iterator) {
         annotationIris.add(quad.getSubject());
      }
      if (annotationIris.isEmpty()) {
         throw new NotAnAnnotationException();
      }
      for (BlankNodeOrIRI iri : annotationIris) {
         Dataset annoDataset = RdfUtilities.getSubDataset(dataset, rdf, iri);
         annotationList.addAnnotation(new RdfAnnotation(annoDataset, rdfBackend));
      }
      return annotationList;
   }

   @Override
   public RDF getRDF() {
      return rdf;
   }

   @Override
   public Annotation createAnnotation(Dataset dataSet) {
      return new RdfAnnotation(dataSet, rdfBackend);
   }

   @Override
   public Annotation createAnnotation(String rawAnnotation, Format format) {
      Annotation annotation = new RdfAnnotation(rdfBackend.readFromString(rawAnnotation, format), rdfBackend);
      return annotation;
   }

   @Override
   public boolean isValidInputFormat(Format format) {
      return rdfBackend.isValidInputFormat(format);
   }

   @Override
   public Page createPage(Dataset dataset, String containerIri, int pageNr, boolean preferIrisOnly, boolean isEmbedded,
         int annoTotalCount, String modified, String label) {
      return new RdfPage(dataset, containerIri, pageNr, preferIrisOnly, isEmbedded, annoTotalCount, modified, label,
            rdfBackend);
   }

   @Override
   public Container createContainer(Dataset dataset, boolean preferMinimalContainer, boolean preferIrisOnly) {
      return new RdfOutputContainer(dataset, preferMinimalContainer, preferIrisOnly, rdfBackend);
   }

   @Override
   public Container createContainer(String rawContainer, Format format, String newContainerIri) {
      IRI containerIri = rdf.createIRI(newContainerIri);
      RdfContainer container
            = new RdfContainer(rdfBackend.readFromString(rawContainer, format), rdfBackend, containerIri);
      return container;
   }

   @Override
   public String convertFormat(String rawString, Format srcFormat, Format destFormat) {
      Dataset dataset = rdfBackend.readFromString(rawString, srcFormat);
      return rdfBackend.getOutput(dataset, destFormat);
   }

   @Override
   public Container createContainer(Dataset dataset) {
      // Default is preferMinimalContainer and irisOnly
      return createContainer(dataset, true, true);
   }
}
