package cs220.actors

import akka.actor.Actor
import akka.actor.{ActorRef, ActorLogging}
import scala.util.matching.Regex

/**
 * The `ParseActor` handles the parsing of HTML documents. Its
 * primary goal is to extract out the links and words contained
 * in an HTML document.
 */
class ParseActor(pq: ActorRef) extends Actor with ActorLogging {
  log.info("""
              |======================================================|
              |***************** ParseActor created *****************|
              |======================================================|
           """)

  pq ! NeedPage

  def receive = {
    //received from ParseQueueActor
    case NoPages =>

      //send back NeedPage messgae
      sender ! NeedPage

    //received from ParseQueueActor
    case ParsePage(url,html) =>{
      log.info("""
                  |======================================================|
                  |******************* Parsing page *********************|
                  |======================================================|
               """)
       

      //////////////////////////////////////////
      //The part below is for extracting links//
      //           and sending links          //
      //////////////////////////////////////////

      //get all links from html
      val getLinks = "\"(https?://[^\"]+)".r findAllIn html
      // split and make an Array of links, and filter out empty element(s)
      val ArrayofLinks = (getLinks.mkString).split('"').filter { x => x != "" }
      // iterate through and send each Link messgae back
      ArrayofLinks.foreach { x => sender ! Link(x) }


      //////////////////////////////////////////
      //The part below is for extracting words//
      //             and sending words        //
      //////////////////////////////////////////

      // remove all the tages
      val RMalltages = ("</?[^>]+>".r).replaceAllIn(html,"").mkString
      // replace newlines with blank
      val RMnewlines = ("[\\t\\n\\r]".r).replaceAllIn(RMalltages," ").mkString
      //keep only characters
      val keepchar = ("[^A-Za-z]+".r).replaceAllIn(RMnewlines, " ").mkString
      //replace whitespaces with ","
      val RMwhitespaces =("""[ \t\x0B\f]+""".r).replaceAllIn(keepchar,",").mkString
      //split and make an Array of words, and filter out empty element(s)
      val ArrayofWords = RMwhitespaces.split(',').filter { x => x != "" }
      //iterate through, get rid of duplicate words, then send each Word message back
      ArrayofWords.distinct.foreach { x => sender ! Word(url,x)}

      //when sending links and messages are all done,
      //request another page
      sender ! NeedPage
    }

  }
}
