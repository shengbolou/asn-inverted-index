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
  log.info("""
              |======================================================|
              |************* ParseQueueActor created ****************|
              |======================================================|
           """)
  var linkQueue: Option[ActorRef] = None
  val queue = Queue[ParsePage]()


  def receive = {
    //received from LinkQueueActor
    case Page(url, html) => {
      //save a reference to the sender
      if (linkQueue == None)
        linkQueue = Some(sender)
      //send CheckPage message to IndexActor
      indexer ! CheckPage(url,html)

    }
    //received from IndexActor, in response to CheckPage message
    case ParsePage(url,html) => {
      //enqueue the ParsePage
      log.info("""
                  |======================================================|
                  |*************** Enqueuing ParsePage ******************|
                  |======================================================|
               """)

      queue.enqueue(ParsePage(url,html))

    }
    //received from ParseActor
    case NeedPage => {
      //if queue is not empty,
      //dequeue and send ParsePage message back
      if(!queue.isEmpty) {
        log.info("""
                    |======================================================|
                    |*************** Dequeuing ParsePage ******************|
                    |======================================================|
                 """)
      
        sender ! queue.dequeue
      }
      //otherwise send NoPages message back
      else               sender ! NoPages
    }
    //received from ParseActor
    case Link(url) =>
      //send CheckLink message to IndexActor
      indexer ! CheckLink(url)

    //received from ParseQueueActor
    case Word(url,word) =>
      //send Word message to IndexActor
      indexer ! Word(url,word)

    //received from IndexActor, in respond to CheckLink message
    case QueueLink(url) =>
      //forward to LinkQueueActor QueueLink message
      linkQueue.get ! QueueLink(url)
  }

}
