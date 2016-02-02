package cs220

import scalikejdbc._

/**
 * This is the main entry point for the `Searcher` application.
 * The `Searcher` application is given a single argument which
 * is a word and returns a list of documents (URLs) in which
 * the word was found. If no argument is provided a list of
 * words that were indexed are displayed.
 */
object Searcher extends App {
  // Necessary setup for connecting to the H2 database:
  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:./indexer", "sa", "")
  implicit val session = AutoSession

  // Check the arguments length to determine what to do:
  if (args.length == 0) {
    // If a word was not provided we simply list the words
    // that have been indexed.
    list
  } else {
    // Otherwise, we search for the given word:
    search(args(0))
  }

  /**
   * `list` lists the words that have been indexed.
   */
  def list = {
    val words =
      sql"""
        select word from words;
      """.map(
        rs => rs.string("word")
      ).list.apply()

    println("The following words are available to search:")
    for (w <- words) {
      println(w)
    }
  }

  /**
   * `search` retrieves the list of documents the word
   * was found in.
   */
  def search(word: String) = {
    val docs =
      sql"""
        select url
        from words, documents, index
        where word = $word
          and words.wordid = index.wordid
          and documents.docid = index.docid;
      """.map(
            rs => rs.string("url")
         ).list.apply()

    for (d <- docs) {
      println(d)
    }
  }
}