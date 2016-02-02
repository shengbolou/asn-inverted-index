package cs220.actors

import scalikejdbc._
import akka.actor.{Actor, ActorLogging}

/**
 * The `IndexActor` is responsible for communicating with the
 * database to index a word.
 */
class IndexActor extends Actor with ActorLogging {
  log.info("""
              |======================================================|
              |***************** IndexActor created *****************|
              |======================================================|
           """)

  var num = 1
  def receive = {
    //received from ParseQueueActor
    case CheckPage(url,html) => {
      log.info("""
                  |======================================================|
                  |******************* Checking page ********************|
                  |======================================================|
               """)

      //if the url is not visited, insert into documents table,
      //then send back ParsePage message
      if(Documents.get(url) == None){
        Documents.insert(url)
        //count the number of inserting
        log.info(s"""
                    |======================================================|
                    |*************** inserting url #${num} ****************|
                    |======================================================|
                 """)

        num = num + 1
        sender ! ParsePage(url,html)
       }
    }
    // received from PareseQueueActor
    case CheckLink(url) =>{
      log.info("""
                  |======================================================|
                  |******************* Checking Link ********************|
                  |======================================================|
               """)

      //if the url is not visited, send back QueueLink message
      if(Documents.get(url) == None)
        sender ! QueueLink(url)

    }
    // received from ParseQueueActor
    case Word(url,word) => {
      //if the word is not inserted before, insert into words table
      if(Words.get(word) ==None){
        log.info("""
                    |======================================================|
                    |******************* inserting word *******************|
                    |======================================================|
                 """)

        Words.insert(word)
      }
      // insert into index table
      Index.insert(url,word)
    }
  }

  ///////////////////////////////////////////////////////////////////
  // The code below is a starting point for your queries/updates to
  // the database. We have provided the database creation SQL for
  // you. You will not need to add any additional tables. Your goal
  // is to populate it with data you have received from parsed HTML
  // documents. We strongly suggest that you implement each of your
  // queries as individual methods in this class, where each method
  // corresponds to some query that is useful in building the index.
  ///////////////////////////////////////////////////////////////////

  // Necessary setup for connecting to the H2 database:
  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:./indexer", "sa", "")
  implicit val session = AutoSession

  // Create the database when this object is referenced.
  createDatabase

  def createDatabase: Unit = {
    sql"""
      drop table words if exists;
      drop table documents if exists;
      drop table index if exists;
    """.update.apply()

    // Create the tables if they do not already exist:
    sql"""
    create table if not exists words (
      wordid int auto_increment,
      word varchar(50),
      primary key (wordid)
    );
    """.update.apply()

    sql"""
    create table if not exists documents (
      docid int auto_increment,
      url varchar(1024),
      primary key (docid)
    );
    """.update.apply()

    sql"""
    create table if not exists index (
      wordid int,
      docid int,
      foreign key (wordid) references words (wordid) on delete cascade,
      foreign key (docid) references documents (docid) on delete cascade
    );
    """.update.apply()
  }

 /**
  * The inner class below is Documents class,
  * represents each element in documents table.
  * It also overrides 'toString' method,
  * so each element is a 'Documents(docid,url)'.
  **/

 private[actors] class Documents(val docid: Int, val url: String){
      Class.forName("org.h2.Driver")
      ConnectionPool.singleton("jdbc:h2:./indexer", "sa", "")
      implicit val session = AutoSession

      //override toString
      override def toString = s"Documents($docid,$url)"
}

/**
 *  This is the companion object of Documents class,
 *  its goal is manipulating documents table,
 *  it has four methods:
 *
 *  (1)"list": make a list of elements in documents table,
 *  (2)"printAll": print everything in documents table,
 *
 *  "list" and "printAll" are for DEBUGGING so you can ignore them.
 *
 *  (3)"get(url:String)": get url from documents table where url =${url},
 *  (4)"insert(url:String)": insert url into documents table,
 *
 *  "get" and "insert" are actually used to manipulate documents table.
 **/
 object Documents{

  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:./indexer", "sa", "")
  implicit val session = AutoSession
  //make a list of elements in documents table
  def list =
    sql"""
      select * from documents
    """.map(
      rs => new Documents(rs.int("docid"),
                          rs.string("url"))
    ).list.apply()
  //print everything in documents table
  def printAll: Unit =
    list.foreach(a =>
      println(s"${a.docid} ${a.url}")

      )
 //get url from documents table where url =${url}
  def get(url: String): Option[Documents] = {
    sql"""
      select * from documents
      where url = ${url}
    """.map(
      rs => new Documents(rs.int("docid"),
                          rs.string("url"))
    ).single.apply()
  }
  //insert url into documents table
  def insert(url: String): Unit =
       sql"""
         insert into documents (url) values
          (${url});
       """.update.apply()

}


/**
 * The class below is Words class, represents each element in words table,
 * it overrides 'toString' method,
 * so each element is a 'Words(wordid,word)'.
 **/
private[actors] class Words(
          val wordid: Int,
          val word: String){

      Class.forName("org.h2.Driver")
      ConnectionPool.singleton("jdbc:h2:./indexer", "sa", "")
      implicit val session = AutoSession
      //override toString here
       override def toString = s"Words($wordid,$word)"
 }

 /**
  *  This is the companion object of Words class,
  *  its goal is manipulating words table,
  *  it has four methods:
  *
  *  (1)"list": make a list of elements in words table,
  *  (2)"printAll": print everything in words table,
  *
  *  "list" and "printAll" are for DEBUGGING so you can ignore them.
  *
  *  (3)"get(word:String)": get word from words table where word =${word},
  *  (4)"insert(word:String)": insert word into words table,
  *
  *  "get" and "insert" are actually used to manipulate words table.
  **/
object Words{

  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:./indexer", "sa", "")
  implicit val session = AutoSession
  //make a list of elements in words table
  def list =
    sql"""
      select * from words
    """.map(
      rs => new Words(rs.int("wordid"),
                          rs.string("word"))
    ).list.apply()
  //"printAll": print everything in words table
  def printAll: Unit =
    list.foreach(a =>
      println(s"${a.wordid} ${a.word}")

      )
   //get word from words table where word =${word}
  def get(word: String): Option[Words] = {
    sql"""
      select * from words
      where word = ${word}
    """.map(
      rs => new Words(rs.int("wordid"),
                          rs.string("word"))
    ).single.apply()
  }
  //insert word into words table
  def insert(word: String): Unit =
       sql"""
         insert into words (word) values
          (${word});
       """.update.apply()
 }

/**
 * The object below is Index object, its goal is manipulating index table.
 * It only has one method  'insert(url:String, word:String)'
 * which query the database to find the document ID
 * and word ID then insert it into the index table.
 **/
object Index {
  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:./indexer", "sa", "")
  implicit val session = AutoSession


  //query the database to find the document ID
  //and word ID and insert it into the index table
  def insert(url: String, word: String): Unit =
    sql"""
      insert into index (wordid,docid)
      select words.wordid, documents.docid
      from words, documents
      where word = ${word}
      and   url = ${url};
    """.update.apply()
 }

}
