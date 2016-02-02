package cs220

import akka.actor.ActorSystem
import akka.actor.Props
import cs220.actors._

/**
 * This is the main application entry point for the `Indexer` application.
 * The application expects a single argument which is the URL to begin
 * indexing on.
 */
object Indexer extends App {
  if (args.length == 0) {
    println("Expected URL argument")
    System.exit(1)
  }

  // Create the actor system:
  val system = ActorSystem("inverted-indexer")

  try {
    // Create the actors in our actor system:
    val indexer    = system.actorOf(Props(new IndexActor), "indexer")
    val parseQueue = system.actorOf(Props(classOf[ParseQueueActor], indexer), "parseQueue")
    val linkQueue  = system.actorOf(Props(classOf[LinkQueueActor], parseQueue), "linkQueue")
    val f01 = system.actorOf(Props(classOf[FetchActor], linkQueue), "w01")
    val p01 = system.actorOf(Props(classOf[ParseActor], parseQueue), "p01")

    // Send the first message to start the indexing process:
    linkQueue ! QueueLink(args(0))
  } catch {
    case _: Exception => system.shutdown()
  }
}