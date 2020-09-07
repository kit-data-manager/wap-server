package edu.kit.scc.dem.wapsrv.model.rdf.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

/**
 * This class provides the vocabulary ActivityStreams (from http://www.w3.org/ns/activitystreams#).
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.1
 */
public class AsVocab {
   /**
    * Object ( object is a reserved Word: hence the "Obj")
    */
   public static IRI objectObj = buildIri("Object");
   /**
    * Link
    */
   public static IRI link = buildIri("Link");
   /**
    * Activity
    */
   public static IRI activity = buildIri("Activity");
   /**
    * IntransitiveActivity
    */
   public static IRI intransitiveActivity = buildIri("IntransitiveActivity");
   /**
    * Collection
    */
   public static IRI collection = buildIri("Collection");
   /**
    * OrderedCollection
    */
   public static IRI orderedCollection = buildIri("OrderedCollection");
   /**
    * CollectionPage
    */
   public static IRI collectionPage = buildIri("CollectionPage");
   /**
    * OrderedCollectionPage
    */
   public static IRI orderedCollectionPage = buildIri("OrderedCollectionPage");
   /**
    * Accept
    */
   public static IRI accept = buildIri("Accept");
   /**
    * Add
    */
   public static IRI add = buildIri("Add");
   /**
    * Announce
    */
   public static IRI announce = buildIri("Announce");
   /**
    * Arrive
    */
   public static IRI arrive = buildIri("Arrive");
   /**
    * Block
    */
   public static IRI block = buildIri("Block");
   /**
    * Create
    */
   public static IRI create = buildIri("Create");
   /**
    * Delete
    */
   public static IRI delete = buildIri("Delete");
   /**
    * Dislike
    */
   public static IRI dislike = buildIri("Dislike");
   /**
    * Flag
    */
   public static IRI flag = buildIri("Flag");
   /**
    * Follow
    */
   public static IRI follow = buildIri("Follow");
   /**
    * Ignore
    */
   public static IRI ignore = buildIri("Ignore");
   /**
    * Invite
    */
   public static IRI invite = buildIri("Invite");
   /**
    * Join
    */
   public static IRI join = buildIri("Join");
   /**
    * Leave
    */
   public static IRI leave = buildIri("Leave");
   /**
    * Like
    */
   public static IRI like = buildIri("Like");
   /**
    * Listen
    */
   public static IRI listen = buildIri("Listen");
   /**
    * Move
    */
   public static IRI move = buildIri("Move");
   /**
    * Offer
    */
   public static IRI offer = buildIri("Offer");
   /**
    * Question
    */
   public static IRI question = buildIri("Question");
   /**
    * Reject
    */
   public static IRI reject = buildIri("Reject");
   /**
    * Read
    */
   public static IRI read = buildIri("Read");
   /**
    * Remove
    */
   public static IRI remove = buildIri("Remove");
   /**
    * TentativeReject
    */
   public static IRI tentativeReject = buildIri("TentativeReject");
   /**
    * TentativeAccept
    */
   public static IRI tentativeAccept = buildIri("TentativeAccept");
   /**
    * Travel
    */
   public static IRI travel = buildIri("Travel");
   /**
    * Undo
    */
   public static IRI undo = buildIri("Undo");
   /**
    * Update
    */
   public static IRI update = buildIri("Update");
   /**
    * View
    */
   public static IRI view = buildIri("View");
   /**
    * Application
    */
   public static IRI application = buildIri("Application");
   /**
    * Group
    */
   public static IRI group = buildIri("Group");
   /**
    * Organization
    */
   public static IRI organization = buildIri("Organization");
   /**
    * Person
    */
   public static IRI person = buildIri("Person");
   /**
    * Service
    */
   public static IRI service = buildIri("Service");
   /**
    * Article
    */
   public static IRI article = buildIri("Article");
   /**
    * Audio
    */
   public static IRI audio = buildIri("Audio");
   /**
    * Document
    */
   public static IRI document = buildIri("Document");
   /**
    * Event
    */
   public static IRI event = buildIri("Event");
   /**
    * Image ( exists as Object with capital letter and as property without capital letter: hence the "Obj")
    */
   public static IRI imageObj = buildIri("Image");
   /**
    * Note
    */
   public static IRI note = buildIri("Note");
   /**
    * Page
    */
   public static IRI page = buildIri("Page");
   /**
    * Place
    */
   public static IRI place = buildIri("Place");
   /**
    * Profile
    */
   public static IRI profile = buildIri("Profile");
   /**
    * Relationship ( exists as Object with capital letter and as property without capital letter: hence the "Obj")
    */
   public static IRI relationshipObj = buildIri("Relationship");
   /**
    * Tombstone
    */
   public static IRI tombstone = buildIri("Tombstone");
   /**
    * Video
    */
   public static IRI video = buildIri("Video");
   /**
    * Mention
    */
   public static IRI mention = buildIri("Mention");
   /**
    * actor
    */
   public static IRI actor = buildIri("actor");
   /**
    * attachment
    */
   public static IRI attachment = buildIri("attachment");
   /**
    * attributedTo
    */
   public static IRI attributedTo = buildIri("attributedTo");
   /**
    * audience
    */
   public static IRI audience = buildIri("audience");
   /**
    * bcc
    */
   public static IRI bcc = buildIri("bcc");
   /**
    * bto
    */
   public static IRI bto = buildIri("bto");
   /**
    * cc
    */
   public static IRI cc = buildIri("cc");
   /**
    * context
    */
   public static IRI context = buildIri("context");
   /**
    * current
    */
   public static IRI current = buildIri("current");
   /**
    * first
    */
   public static IRI first = buildIri("first");
   /**
    * generator
    */
   public static IRI generator = buildIri("generator");
   /**
    * icon
    */
   public static IRI icon = buildIri("icon");
   /**
    * id
    */
   public static IRI id = buildIri("id");
   /**
    * image
    */
   public static IRI image = buildIri("image");
   /**
    * inReplyTo
    */
   public static IRI inReplyTo = buildIri("inReplyTo");
   /**
    * instrument
    */
   public static IRI instrument = buildIri("instrument");
   /**
    * last
    */
   public static IRI last = buildIri("last");
   /**
    * location
    */
   public static IRI location = buildIri("location");
   /**
    * items
    */
   public static IRI items = buildIri("items");
   /**
    * oneOf
    */
   public static IRI oneOf = buildIri("oneOf");
   /**
    * anyOf
    */
   public static IRI anyOf = buildIri("anyOf");
   /**
    * closed
    */
   public static IRI closed = buildIri("closed");
   /**
    * origin
    */
   public static IRI origin = buildIri("origin");
   /**
    * next
    */
   public static IRI next = buildIri("next");
   /**
    * object
    */
   public static IRI object = buildIri("object");
   /**
    * prev
    */
   public static IRI prev = buildIri("prev");
   /**
    * preview
    */
   public static IRI preview = buildIri("preview");
   /**
    * result
    */
   public static IRI result = buildIri("result");
   /**
    * replies
    */
   public static IRI replies = buildIri("replies");
   /**
    * tag
    */
   public static IRI tag = buildIri("tag");
   /**
    * target
    */
   public static IRI target = buildIri("target");
   /**
    * to
    */
   public static IRI to = buildIri("to");
   /**
    * type
    */
   public static IRI type = buildIri("type");
   /**
    * url
    */
   public static IRI url = buildIri("url");
   /**
    * accuracy
    */
   public static IRI accuracy = buildIri("accuracy");
   /**
    * altitude
    */
   public static IRI altitude = buildIri("altitude");
   /**
    * content
    */
   public static IRI content = buildIri("content");
   /**
    * name
    */
   public static IRI name = buildIri("name");
   /**
    * duration
    */
   public static IRI duration = buildIri("duration");
   /**
    * height
    */
   public static IRI height = buildIri("height");
   /**
    * href
    */
   public static IRI href = buildIri("href");
   /**
    * hreflang
    */
   public static IRI hreflang = buildIri("hreflang");
   /**
    * partOf
    */
   public static IRI partOf = buildIri("partOf");
   /**
    * latitude
    */
   public static IRI latitude = buildIri("latitude");
   /**
    * longitude
    */
   public static IRI longitude = buildIri("longitude");
   /**
    * mediaType
    */
   public static IRI mediaType = buildIri("mediaType");
   /**
    * endTime
    */
   public static IRI endTime = buildIri("endTime");
   /**
    * published
    */
   public static IRI published = buildIri("published");
   /**
    * startTime
    */
   public static IRI startTime = buildIri("startTime");
   /**
    * radius
    */
   public static IRI radius = buildIri("radius");
   /**
    * rel
    */
   public static IRI rel = buildIri("rel");
   /**
    * startIndex
    */
   public static IRI startIndex = buildIri("startIndex");
   /**
    * summary
    */
   public static IRI summary = buildIri("summary");
   /**
    * totalItems
    */
   public static IRI totalItems = buildIri("totalItems");
   /**
    * units
    */
   public static IRI units = buildIri("units");
   /**
    * updated
    */
   public static IRI updated = buildIri("updated");
   /**
    * width
    */
   public static IRI width = buildIri("width");
   /**
    * subject
    */
   public static IRI subject = buildIri("subject");
   /**
    * relationship
    */
   public static IRI relationship = buildIri("relationship");
   /**
    * describes
    */
   public static IRI describes = buildIri("describes");
   /**
    * formerType
    */
   public static IRI formerType = buildIri("formerType");
   /**
    * deleted
    */
   public static IRI deleted = buildIri("deleted");
   private static RDF rdf;
   private static final String ROOT = "http://www.w3.org/ns/activitystreams#";

   private static IRI buildIri(String property) {
      if (rdf == null) {
         rdf = new SimpleRDF();
      }
      return rdf.createIRI(ROOT + property);
   }
}
