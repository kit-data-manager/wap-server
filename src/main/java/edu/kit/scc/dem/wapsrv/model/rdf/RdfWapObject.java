package edu.kit.scc.dem.wapsrv.model.rdf;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.Triple;
import edu.kit.scc.dem.wapsrv.model.WapObject;
import edu.kit.scc.dem.wapsrv.model.formats.Format;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.AnnoVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.DcTermsVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.RdfVocab;
import edu.kit.scc.dem.wapsrv.model.rdf.vocabulary.WapVocab;

/**
 * Basic RdfWapObject implementing all common functionality of Annotations and Containers.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public abstract class RdfWapObject implements WapObject {
   /**
    * The RdfBackend
    */
   protected final RdfBackend rdfBackend;
   /**
    * The data set used as internal model
    */
   protected final Dataset dataset;
   /** The IRI of the WapObject */
   protected BlankNodeOrIRI iri;
   private String etag;

   /**
    * Creates a new RdfWapObject object using the given parameters
    * 
    * @param dataset
    *                   The data set used as data backend
    * @param rdfBackend
    *                   The RDF backend
    */
   public RdfWapObject(Dataset dataset, RdfBackend rdfBackend) {
      this.dataset = dataset;
      this.rdfBackend = rdfBackend;
      // Extract ETag and remove from data set.
      Optional<? extends Triple> etagTriple = dataset.getGraph().stream(iri, WapVocab.etag, null).findFirst();
      if (etagTriple.isPresent()) {
         etag = RdfUtilities.nStringToString(etagTriple.get().getObject().ntriplesString());
         dataset.getGraph().remove(iri, WapVocab.etag, null);
      } else {
         // Set eTag null -> to be handled in Repository
         etag = null;
      }
   }

   /**
    * Gets the IRI for the given type.
    *
    * @param  type
    *              the IRI representation of the rdf:type
    * @return      the IRI of the node declaring the given rdf:type
    */
   public BlankNodeOrIRI getIriForType(IRI type) {
      Optional<? extends Triple> triple = dataset.getGraph().stream(null, RdfVocab.type, type).findFirst();
      if (!triple.isPresent()) {
         // Not a valid WapObject of the defined Type
         return null;
      }
      return triple.get().getSubject();
   }

   @Override
   public Dataset getDataset() {
      return dataset;
   }

   @Override
   public String toString(Format format) {
      String serialized = rdfBackend.getOutput(getDataset(), format);
      return serialized;
   }

   @Override
   public String getEtagQuoted() {
      return "\"" + getEtag() + "\"";
   }

   @Override
   public String getEtag() {
      return etag;
   }

   @Override
   public void setEtag(String etag) {
      this.etag = etag;
   }

   @Override
   public BlankNodeOrIRI getIri() {
      return iri;
   }

   @Override
   public void setIri(BlankNodeOrIRI newIri, boolean copyVia) {
      // Check if IRI is same
      if (newIri.equals(iri))
         return;
      BlankNodeOrIRI oldIRI = iri;
      RdfUtilities.renameNodeIri(dataset, iri, newIri);
      iri = newIri;
      // never copy blank node identifiers
      if (copyVia && !oldIRI.ntriplesString().startsWith("_:")) {
         // Add via
         dataset.getGraph().add(iri, AnnoVocab.via, oldIRI);
      }
   }

   @Override
   public void setIri(String iri) {
      setIri(rdfBackend.getRdf().createIRI(iri));
   }

   @Override
   public void setIri(String iri, boolean copyVia) {
      setIri(rdfBackend.getRdf().createIRI(iri), copyVia);
   }

   @Override
   public void setIri(BlankNodeOrIRI iri) {
      setIri(iri, true);
   }

   @Override
   public void setCreated() {
      if (!dataset.getGraph().contains(iri, DcTermsVocab.created, null)) {
         Calendar calendar = Calendar.getInstance();
         Literal timedate = RdfUtilities.rdfLiteralFromCalendar(calendar, rdfBackend.getRdf());
         dataset.getGraph().add(iri, DcTermsVocab.created, timedate);
      }
   }

   @Override
   public String getValue(IRI propertyName) {
      Optional<? extends Triple> triple = dataset.getGraph().stream(iri, propertyName, null).findFirst();
      if (triple.isPresent()) {
         return RdfUtilities.nStringToString(triple.get().getObject().ntriplesString());
      }
      return null;
   }

   @Override
   public List<String> getValues(IRI propertyName) {
      Vector<String> values = new Vector<>();
      dataset.getGraph().stream(iri, propertyName, null).forEach(t -> {
         values.add(RdfUtilities.nStringToString(t.getObject().ntriplesString()));
      });
      return values;
   }

   @Override
   public boolean isDeleted() {
      return hasProperty(WapVocab.deleted);
   }
}
