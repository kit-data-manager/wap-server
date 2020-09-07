package edu.kit.scc.dem.wapsrv.model.rdf;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDF;
import edu.kit.scc.dem.wapsrv.exceptions.FormatNotAvailableException;
import edu.kit.scc.dem.wapsrv.model.Annotation;
import edu.kit.scc.dem.wapsrv.model.AnnotationList;
import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.formats.Format;

/**
 * The RdfAnnotationList implementation
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfAnnotationList implements AnnotationList {
   /**
    * List of annotations in this annotation list
    */
   private final List<Annotation> annotations = new Vector<Annotation>();
   /**
    * The RdfBackend
    */
   private final RdfBackend rdfBackend;
   /**
    * The container IRI
    */
   private IRI containerIri;
   /**
    * The container ETag
    */
   private String containerEtag;

   /**
    * Creates a new RdfAnnotationList object using the given parameters
    * 
    * @param rdfBackend
    *                   The RDF backend
    */
   public RdfAnnotationList(RdfBackend rdfBackend) {
      this.rdfBackend = rdfBackend;
   }

   @Override
   public List<Annotation> getAnnotations() {
      return annotations;
   }

   @Override
   public String toString(Format format) throws FormatNotAvailableException {
      Dataset dataset = createUnionDataset();
      return rdfBackend.getOutput(dataset, format);
   }

   /**
    * Creates a dataset combining all annotations into one
    * 
    * @return The combined dataset
    */
   private Dataset createUnionDataset() {
      RDF rdf = rdfBackend.getRdf();
      Dataset combinedDataset = rdf.createDataset();
      for (Annotation anno : annotations) {
         // Maybe there is need to add a quad that links the different annotations, maybe not.
         for (Quad quad : anno.getDataset().iterate()) {
            combinedDataset.add(quad);
         }
      }
      return combinedDataset;
   }

   @Override
   public IRI getContainerIri() {
      return containerIri;
   }

   @Override
   public void setContainerIri(IRI containerIri) {
      this.containerIri = containerIri;
   }

   @Override
   public String getContainerEtag() {
      return containerEtag;
   }

   @Override
   public void setContainerEtag(String containerEtag) {
      this.containerEtag = containerEtag;
   }

   @Override
   public void addAnnotation(Annotation anno) {
      annotations.add(anno);
   }

   @Override
   public Iterator<Annotation> iterator() {
      return annotations.iterator();
   }

   @Override
   public void setContainerIri(String containerIri) {
      setContainerIri(rdfBackend.getRdf().createIRI(containerIri));
   }

   @Override
   public Type getType() {
      return FormattableObject.Type.ANNOTATION;
   }
}
