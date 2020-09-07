package edu.kit.scc.dem.wapsrv.repository;

import java.util.List;

/**
 * Interface to interact with the container repository
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface ContainerRepository extends WapObjectRepository {
   /**
    * Gets a range of object iris from rdf:seq.
    *
    * @param  modelIri
    *                              the graph/model iri
    * @param  seqIri
    *                              the sequence iri
    * @param  firstAnnotationIndex
    *                              the first item index
    * @param  lastAnnotationIndex
    *                              the last item index
    * @return                      a List of the range of object iris from rdf:seq
    */
   List<String> getRangeOfObjectIrisFromSeq(String modelIri, String seqIri, int firstAnnotationIndex,
         int lastAnnotationIndex);

   /**
    * Gets the all object iris of rdf:seq.
    *
    * @param  modelIri
    *                  the graph/model iri
    * @param  seqIri
    *                  the sequence iri
    * @return          a List of all the object iris of rdf:seq
    */
   List<String> getAllObjectIrisOfSeq(String modelIri, String seqIri);

   /**
    * Removes all elements from the RDF:Seq for the given IRI
    *
    * @param modelIri
    *                     the graph/model IRI
    * @param seqIriString
    *                     the seq IRI string
    */
   void emptySeq(String modelIri, String seqIriString);
}
