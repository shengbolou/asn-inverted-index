package cs220.actors

import scala.collection.mutable.Queue
import scala.collection.mutable.Set
import akka.actor.{Actor, ActorRef, ActorLogging}
import cs220.Indexer

/**
 * The `LinkQueueActor` is responsible for queuing links to be fetched by
 * a `FetchActor`. The `FetchActor` will communicate with this actor to
 * get the next link to fetch. We limit the number of links fetched to an
 * arbitrary total of 500 - after which this actor will shutdown the actor
 * system.
 */
class LinkQueueActor(parseQueue: ActorRef) extends Actor with ActorLogging {
  // We have provided some definitions below which will help you with
  // you implementation. You are welcome to modify these, however, this
  // is what we used for our implementation.
  log.info("""
              |======================================================|
              |************* LinkQueueActor created *****************|
              |======================================================|
           """)
  
  val queue        = Queue[String]()
  // change limit to 100, because 500 takes too long.
  var limit        = 100


  def receive = {
   //received from FetchActor
   case NeedLink => {
     //if the limit is reached, shutdown the system
     if(limit == 0){
       log.info("""
                   |======================================================|
                   |******** Limit reached. System shutting down *********|
                   |======================================================|
                """)

         context.system.shutdown()

       }
     else
     //if queue is empty, send NoLinks message back
     if(queue.isEmpty)                sender ! NoLinks
     //otherwise dequeue and send FetchLink back
     else{
       sender ! FetchLink(queue.dequeue)
       //FetchLink onetime, limit reduced by 1
       limit = limit -1
       }
    }
   //reeived from FetchActor
   case Page(url,html) =>
     //forward to ParseQueueActor
     parseQueue ! Page(url,html)



   // received from ParseQueueActor
   case QueueLink(url)=>
     //simply enqueue the url
     queue.enqueue(url)
  }

}
