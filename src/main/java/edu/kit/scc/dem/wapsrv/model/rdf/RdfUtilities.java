package edu.kit.scc.dem.wapsrv.model.rdf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.commons.rdf.api.*;
import edu.kit.scc.dem.wapsrv.exceptions.InternalServerException;
import org.apache.jena.datatypes.xsd.XSDDatatype;

/**
 * Utility class with common operations on graphs and datasets
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class RdfUtilities {
   /**
    * Renames an existing IRI to a new one
    * 
    * @param sourceGraph
    *                    The graph to rename the IRI in
    * @param oldIdIri
    *                    The old IRI
    * @param newIri
    *                    The new IRI
    */
   public static void renameNodeIri(final Graph sourceGraph, BlankNodeOrIRI oldIdIri, BlankNodeOrIRI newIri) {
      // Find all the triples containing the IRI
      Iterable<Triple> subjectIt = sourceGraph.iterate(oldIdIri, null, null);
      Iterable<Triple> objectIt = sourceGraph.iterate(null, null, oldIdIri);
      List<Triple> tripleDelete = new ArrayList<Triple>();
      // First add the new subject related ones
      for (Triple t : subjectIt) {
         sourceGraph.add(newIri, t.getPredicate(), t.getObject());
         tripleDelete.add(t);
      }
      // First add the new object related ones
      for (Triple t : objectIt) {
         sourceGraph.add(t.getSubject(), t.getPredicate(), newIri);
         tripleDelete.add(t);
      }
      tripleDelete.iterator().forEachRemaining(sourceGraph::remove);
   }

   /**
    * Renames an existing IRI to a new one
    * 
    * @param sourceDataset
    *                      The data set to rename the IRI in
    * @param oldIdIri
    *                      The old IRI
    * @param newIri
    *                      The new IRI
    */
   public static void renameNodeIri(final Dataset sourceDataset, BlankNodeOrIRI oldIdIri, BlankNodeOrIRI newIri) {
      renameNodeIri(sourceDataset.getGraph(), oldIdIri, newIri);
   }

   /**
    * Clones a graph object with the help of a given RDF factory
    * 
    * @param  sourceGraph
    *                     The graph to clone
    * @param  factory
    *                     The RDF factory
    * @return             The cloned graph
    */
   public static Graph clone(Graph sourceGraph, RDF factory) {
      Graph targetGraph = factory.createGraph();
      // Find all the triples containing the IRI
      Iterable<Triple> it = sourceGraph.iterate();
      it.iterator().forEachRemaining(targetGraph::add);
      return targetGraph;
   }

   /**
    * Clones a data set object with the help of a given RDF factory.
    *
    * @param  sourceDataset
    *                       The data set to clone
    * @param  factory
    *                       The RDF factory
    * @return               the dataset
    */
   public static Dataset clone(Dataset sourceDataset, RDF factory) {
      throw new InternalServerException("Not yet implemented : RdfUtilities.clone()");
   }

   /**
    * Converts nString to string. nStrings come from the RDF Store and include enclosing characters like
    * quotes(&lt;,&gt;) and quotes ("). This enclosing characters will be removed.
    *
    * @param  nString
    *                 the nString
    * @return         the stripped string
    */
   public static String nStringToString(String nString) {
      String firstChar = nString.substring(0, 1);
      String lastChar = nString.substring(nString.length() - 1, nString.length());
      if ((firstChar.equals(lastChar) && firstChar.equals("\"")) || (firstChar.equals("<") && lastChar.equals(">"))) {
         return nString.substring(1, nString.length() - 1);
      }
      return nString;
   }

   /**
    * Get the whole subgraph denoted by the given root as a new dataset
    * 
    * @param  dataset
    *                 dataset to extract the data from
    * @param  rdf
    *                 the RDF Backend to be used
    * @param  rootIri
    *                 the IRI of the root of the subGraph to extract
    * @return         a Dataset With only the Graph of the rootIRI included.
    */
   public static Dataset getSubDataset(Dataset dataset, RDF rdf, BlankNodeOrIRI rootIri) {
      Dataset subset = rdf.createDataset();
      List<BlankNodeOrIRI> termsToCheck = new Vector<BlankNodeOrIRI>();
      Set<BlankNodeOrIRI> termsChecked = new HashSet<BlankNodeOrIRI>();
      termsToCheck.add(rootIri);
      for (int n = 0; n < termsToCheck.size(); n++) {
         BlankNodeOrIRI iri = termsToCheck.get(n);
         termsChecked.add(iri);
         Iterable<Quad> iterator = dataset.iterate(null, iri, null, null);
         for (Quad quad : iterator) {
            // System.out.println(quad);
            subset.add(quad);
            if (quad.getObject() instanceof BlankNodeOrIRI) {
               if (!termsChecked.contains(quad.getObject())) {
                  termsToCheck.add((BlankNodeOrIRI) quad.getObject());
               }
            }
         }
      }
      return subset;
   }

   /**
    * Generates a Rdf literal from a calendar object.
    *
    * @param  calendar
    *                  the calendar object
    * @param  rdf
    *                  the RDF Backend to be used
    * @return          the RDF literal
    */
   public static Literal rdfLiteralFromCalendar(Calendar calendar, RDF rdf) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      // calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
      String dateTimeString = sdf.format(calendar.getTime());
      IRI typeIRI_DATETIME = rdf.createIRI(XSDDatatype.XSDdateTime.getURI());
      Literal timedate = rdf.createLiteral(dateTimeString, typeIRI_DATETIME);
      return timedate;
   }
}
