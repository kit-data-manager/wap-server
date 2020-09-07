package edu.kit.scc.dem.wapsrv.model;

import org.apache.commons.rdf.api.Dataset;

/**
 * Page objects need to implement this interface. Pages are used only as outgoing objects and are created with data in
 * the repositories database.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public interface Page extends FormattableObject {
   /**
    * Returns the IRI of the Page.
    * 
    * @return The IRI
    */
   String getIri();

   /**
    * Returns the pageNr of the page
    * 
    * @return the pageNr
    */
   int getPageNr();

   /**
    * Returns the IRI of the containers this page belongs to
    * 
    * @return The IRI of the page's container
    */
   String getContainerIri();

   /**
    * Returns the used container preference for annotation serialization
    * 
    * @return The preference regarding annotation serialization
    */
   int getContainerPreference();

   /**
    * Returns the IRI of the next page in the container
    * 
    * @return The IRI of the next page, null if the page is the last one
    */
   String getNextPage();

   /**
    * Returns the IRI of the previous page in the container
    * 
    * @return The IRI of the previous page, null if the page is the first one
    */
   String getPreviousPage();

   /**
    * Returns the absolute position in the container of the first annotation in this page. Numbering starts with 0.
    * 
    * @return The index of the first annotation in this page in the underlying container
    */
   int getFirstAnnotationPosition();

   /**
    * Adds an annotation to this page and also registers its IRI. Makes no sense in conjunction with PreferIriesOnly.
    * 
    * @param anno
    *             The annotation to add
    */
   void addAnnotation(Annotation anno);

   /**
    * Adds an annotation IRI to this page. Use only when PreferIriesOnly is used.
    * 
    * @param annoIri
    *                The annotation IRI to add
    */
   void addAnnotationIri(String annoIri);

   /**
    * After all annotations or iris have been added during construction of this page object, this method is called to
    * finalize it. Does nothing if the page has already been closed.
    */
   void closeAdding();

   /**
    * Gets the Dataset representing the output of the Page.
    *
    * @return the Dataset
    */
   Dataset getDataset();
}
