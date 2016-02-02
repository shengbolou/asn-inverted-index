package cs220.actors

import scala.util.{Try, Success, Failure}
import scalaj.http._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging

/**
 * The `FetchActor` is resposible for fetching an HTML page from
 * a remote server. You should use the scalaj library to fetch
 * the remote HTML document. Details about this library can be
 * found at [[https://github.com/scalaj/scalaj-http]].
 *
 * It is your responsibility to research this library to learn how
 * to use it properly to retrieve the HTML documents.
 *
 * HINT: import the library from the scala repl and play around
 * with the library to see how it is used. After you understand
 * how the library is used come back to this actor and fill in
 * the necessary call.
 */
class FetchActor(queue: ActorRef) extends Actor with ActorLogging {
  log.info("""
              |======================================================|
              |***************** FetchActor created *****************|
              |======================================================|
           """)
  var num = 1;
  // This message will start off the process of fetching
  // links from the QueueActor. We include this for you!
  queue ! NeedLink

  def receive = {
    // received from LinkQueueActor, request another link
    case NoLinks => sender ! NeedLink

    //received from LinkQueueActor
    case FetchLink(url) => {
        // If fetching is failure, request another link
        if(fetch(url).isFailure) {
          log.info("fetching is failure")
          sender ! NeedLink
        }
        //otherwise, return a Page with url and html, and then request another link
        else {
        //count the number of fetching
        log.info(s"""
                    |======================================================|
                    |***************** Fetching #${num} *******************|
                    |======================================================|
                 """)
        
        num = num + 1
        sender ! Page(url,fetch(url).get)
        sender ! NeedLink
      }
    }
  }


  def fetch(url: String): Try[String] =
    Try(Http(url).asString.body)
}
