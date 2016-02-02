/**
 * This file contains all the messages you will need to implement
 * your actor system. You are welcome to introduce new messages
 * as you see fit, however, these are the ones we used in our
 * implementation.
 */
package cs220.actors

/**
 * `NeedLink` is sent from the FetchActor to the LinkQueueActor
 * to ask for a link to process.
 */
case object NeedLink

/**
 * `NoLinks` is sent from the LinkQueueActor to a FetchActor to
 * indicate that no links are available at this time. The FetchActor
 * will need to try again later.
 */
case object NoLinks

/**
 * `FetchLink` is sent from the LinkQueueActor to a FetchActor to
 * indicate that the URL should be fetched from the web.
 */
case class FetchLink(url: String)

/**
 * `Page` is sent from the FetchActor to the LinkQueueActor with
 * the URL fetched and the HTML retrieved from the URL. The
 * LinkQueueActor forwards the `Page` to the ParseQueueActor to
 * be processed.
 */
case class Page(url: String, html: String)

/**
 * `CheckPage` is sent from the ParseQueueActor to the IndexActor
 * to determine if the page has already been processed. If it has
 * the IndexActor does not respond. If it has not been processed
 * yet then the IndexActor responds with a `ParsePage` message.
 */
case class CheckPage(url: String, html: String)

/**
 * `CheckLink` is sent from the ParseQueueActor to the IndexActor
 * to determine if the URL has already been processed. If it has
 * then the IndexActor does not respond. Otherwise, it responds
 * with a `QueueLink` message. The `QueueLink` messagea is then
 * forwarded to the LinkQueueActor.
 */
case class CheckLink(url: String)

/**
 * `QueueLink` is sent from the IndexActor to the ParseQueueActor
 * to indicate that the link should be queued by the LinkQueueActor.
 * The ParseQueueActor forwards the message to the LinkQueueActor to
 * indicate that the URL should be queued for fetching.
 */
case class QueueLink(url: String)

/**
 * `ParsePage` is sent from the IndexActor to the ParseQueueActor.
 * This message indicates that the URL does not exist in the index
 * (not a duplicate) and so it can be queued by the ParseQueueActor.
 */
case class ParsePage(url: String, html: String)

/**
 * `NeedPage` is sent from a ParseActor to the ParseQueueActor to
 * request a page to parse. If there is a page in the queue the
 * ParseQueueActor responds with a `ParsePage` message.
 */
case object NeedPage

/**
 * `NoPages` is sent from the ParseQueueActor to a ParseActor in
 * response to a `NeedPage` message if there are no pages available
 * to be parsed. The ParseActor will need to make another `NeedPage`
 * request in the future.
 */
case object NoPages

/**
 * `Link` is sent from a ParseActor to the ParseQueueActor with a
 * link it found in the page. The ParseQueueActor then sends a
 * `CheckLink` messagea to the IndexActor to check if the link has
 * already been processed. The IndexActor will respond with a
 * `QueueLink` message if it has not already been indexed.
 */
 case class Link(url: String)

/**
 * `Word` is sent from a ParseActor to the ParseQueueActor which it
 * then forwards to the IndexActor for the word to be indexed in the
 * inverted index.
 */
case class Word(url: String, word: String)
