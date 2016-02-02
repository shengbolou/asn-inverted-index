package cs220.actors

import scala.collection.mutable.Queue
import akka.actor.Actor
import akka.actor.{ActorRef, ActorLogging}

/**
 * The `ParseQueueActor` is responsible for maintaining a queue of pages
 * to be parsed by the `ParseActor`s. It is responsible for determining
 * if a page has already been parsed. If so, it should not parse it again.
 */
class ParseQueueActor(indexer: ActorRef) extends Actor with ActorLogging {
  var linkQueue: Option[ActorRef] = None
  val queue = Queue[ParsePage]()

  def receive = {
    case Page(url, html) => {
      if (linkQueue == None)
        linkQueue = Some(sender)
      // TODO
      ???
    }
    // MORE TO BE DONE HERE...
  }

}