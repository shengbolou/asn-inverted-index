# Inverted Indexer

## Overview

The goal of this assignment is to exercise your understanding of using regular expressions, using external libraries, building your own application, designing your own implementation, parallism, and storing/retrieving data from a relational database. In particular, you will design the internals of a [web crawler] that will retrieve HTML documents from the web and build an [inverted index] of words contained in the retrieved documents.

In future software engineering courses you will be asked to design and implement applications integrating many different libraries and using a variety of programming techniques. This assignment is meant to be difficult and hard to achieve. You will be assessed on how well you are able to complete the assignment. In addition, if you are able to complete the assignment successfully it will be wonderful artifact to show off to company recruiters asking for sample work!

[web crawler]: https://en.wikipedia.org/wiki/Web_crawler
[inverted index]: https://en.wikipedia.org/wiki/Inverted_index

## Setup

First, you need to download the [project starter kit]. This will
download a zip file named
`asn-inverted-index-student-master.zip`. You should then unzip the
archive and move it into your git repository (do not clone this
assignment from github). After you move it into your git repository
run the necessary `git` commands to add it, commit it, and push it.

## H2 Database

We provide a script `tools/h2cli` that will run the H2 command line interface to the H2 database. You will not need to change anything in this script. You can run it like this:

```bash
$ ./tools/h2cli
```

From that point you will see an sql prompt and you can begin to make queries to see if your inverted indexer is doing something useful.

[project starter kit]: https://github.com/umass-cs-220/asn-inverted-index-student/archive/master.zip

## Compiling

You can compile the project using `sbt`:

```bash
$ sbt
> compile
```

This project will compile out of the box.

## Running

We provide two executable classes to *index* HTML documents and *search* for words. To run them you can do so from the command line:

```bash
$ sbt "run-main cs220.Searcher The"
```

This will search for a word in your index database. And this:

```bash
$ sbt "run-main cs220.Searcher"
```

Which will display all the words that have been found. And this:

```bash
$ sbt "run-main cs220.Indexer http://umass-cs-220.github.io/"
```

Which will perform the indexing process. You can use the above URL as a test, however, you should be able to run it on any starting URL to begin indexing.

## Provided

Your project is distributed with an `sbt` formatted project
structure. The files that are most important include:

[src/main/scala/cs220/Indexer.scala]: This file contains an object with a `main` method (extends `App`). It is the main entry point into the indexing process. It is resposible for creating all the *actors* responsible for the indexing process. We have provided this code for you - **you will not need to modify it**.

[src/main/scala/cs220/Searcher.scala]: This file contains an object with a `main` method (extends App). It is the main entry point into the "search" process. It is responsible for looking up a word in the database and displaying the list of URLs the word was found at. We have provided this code for you - **you will not need to modify it**.

[src/main/scala/cs220/actors]: This package contains the akka actors you will need to extend to implement the inverted indexer. In particular, you will find the following files:

* `LinkQueueActor.scala`: this actor is responsible for maintaining a queue of URL links that need to be fetched from the web.
* `FetchActor.scala`: this actor is responsible for fetching HTML documents from the web. It relies on an external library (described below) for performing the network communication.
* `ParseQueueActor.scala`: this actor is responsible for maintaining a queue of HTML documents to be parsed.
* `ParseActor.scala`: this actor is responsible for extracting URL links and words from a retrieved document from the web.
* `IndexActor.scala`: this actor is responsible for indexing words in a database. It reliease on the H2 database - we have provided adequate setup for this.
* `Messages.scala`: this contains definitions for all the messages that need to be sent/received between all the actors.

[src/main/scala/cs220/Indexer.scala]: src/main/scala/cs220/Indexer.scala
[src/main/scala/cs220/Searcher.scala]: src/main/scala/cs220/Searcher.scala
[src/main/scala/cs220/actors]: src/main/scala/cs220/actors


### LinkQueueActor

The `LinkQueueActor` maintains a queue of URL links for HTML documents to be retrieved from the web. Its primary goal is to communicate to the `FetchActor` (when it requests a link to fetch) and the `ParseQueueActor`. This actor is the first actor to receive a message (`QueueLink`) to queue the first link to be retrieved. It is responsible for handling the following incoming messages:

* `NeedLink`: this message is received from a `FetchActor` requesting a link to be fetched from the web. First, the `LinkQueueActor` must check to see if the limit on the number of pages has been reached (default is 500 - see `LinkQueueActor.scala`). If the limit has been reached the entire akka system should be shutdown (`context.system.shutdown()`). Otherwise, it should send a message to the `FetchActor` (sender) a `FetchLink` message if the queue is not empty or a `NoLinks` message otherwise.
* `Page(url, html)`: this message is received from a `FetchActor` with the `url` and `html` that has been retrieved from the web. The `html` parameter is a large `String` containing the entire HTML document. The task is then simple - forward the message to the `ParseQueueActor` to be queued for parsing.
* `QueueLink(url)`: this message is received from the `ParseQueueActor` indicating that the provided `url` should be enqueued. There is nothing more to do except queue the link.

### FetchActor

The `FetchActor` is responsible for fetching HTML documents from the web. You are to use the [scalaj] library for doing the dirty work of requesting the HTML document from web. Your job is to figure out how to use this library and integrate it into this actor. It is responsible for handling the following incoming messages:

* `NoLinks`: this message is received from the `LinkQueueActor` indicating that there are no links to fetch. The response should be to request another link.
* `FetchLink(url)`: this message is received from the `LinkQueueActor` indicating that it should fetch the `url` from the web. Again, use the [scalaj] library to do this work. You should make sure to catch for exceptions if there is a failure to communicate to the web. If there is a failure it should simply request another link from the `LinkQueueActor`. If it is successful you should send the `LinkQueueActor` a `Page` message with the `url` and `html` of the web document retrieved followed by a `NeedLink` message to request another link.

The `FetchActor` begins communicating to the `LinkQueueActor` immediately after it is constructed. We have provided this code for you. The `LinkQueueActor` is provided as an argument to this actor.

### ParseQueueActor

The `ParseQueueActor` is responsible for maintaining a queue of HTML pages to be parsed. It is also responsible for communicating with the `IndexActor` to ensure that a page has not already been visited previously and that new links extracted from HTML documents have not been traversed yet. In addition, it must communicate with `ParseActor`s to provide pages to be parsed for URL links and words. Lastly, it must communicate back to the `LinkQueueActor` with new links to enqueue for the `FetchActor`s. It is responsible for handling the following incoming messages:

* `Page(url, html)`: this message is received from the `LinkQueueActor` containing the `url` and `html` retrieved from the web. If we have not communicated with the `LinkQueueActor` yet we need to save a reference to the sender (as the `LinkQueueActor` is the only actor sending `Page` messages). We have provided this code for you. You must then send a `CheckPage` message to the `IndexActor` to check if the page has already been visited (in the database).
* `ParsePage(url, html)`: this message is sent from the `IndexActor` indicating that the page is safe to parse. It is in response to the `CheckPage` message. The `ParsePage` message should be enqueued.
* `NeedPage`: this message is sent from a `ParseActor` to request a page to parse. If the queue is not empty then a `ParsePage` message should be dequeued and sent back to the sender (a `ParseActor`). If the queue is empty then a `NoPages` message should be sent.
* `Link(url)`: this message is sent from a `ParseActor` containing a URL of a link it parsed from a page. A `CheckLink` message should then be sent to the `IndexActor` to determine if the link has been visited already.
* `Word(url, word)`: this message is sent from a `ParseActor` containing a word that was parsed from a page. This message should simply be forward to the `IndexActor` to be stored in the index (database).
* `QueueLink(url)`: this message is sent from the `IndexActor` in response to a `CheckLink` message. If we receive a `QueueLink` message we know that the link is a new link and we can forward the message to the `LinkQueueActor`.

The `ParseQueueActor` is initialized with a reference to the `IndexActor` through its constructor. As mentioned above, the `LinkQueueActor` is determined dynamically with the first receiving of a `Page` message (again, we provided a bit of code to handle this for you).

### ParseActor

The `ParseActor` is responsible for parsing/extracting out URL links and words from an HTML document. You can use Scala regular expressions to do this, however, you are welcome to use anything (including libraries) you find on the web to do this (we used regular expressions). The goal of the `ParseActor` is to keep asking the `ParseQueueActor` for a page. If a page is sent then the `ParseActor` parses it and sends back the links and words. It is responsible for handling the following messages:

* `ParsePage(url, html)`: this message is sent from the `ParseQueueActor` containing the `url` and `html` retrieved from the web. You will need to extract the links and words from the `html` string. You should then respond to the sender with a `Link` message for each of the links you extract and a `Word` message for each of the words you extract. The final message sent should be a `NeedPage` message to request another page.
* `NoPages`: this message is sent from the `ParseQueueActor` in response to a `NeedPage` message. The response is simply to request another page.

This is likely the most difficult of the actors to implement in that it requires some work to extract out the links and words. Here are regular expressions from our solution that *removes* unimportant parts of the document:

* `(?s)<head.*>.*</head>`: removes the `<head>` tag and its contents which are not useful for links or words.
* `(?s)<script.*>.*</script>`: removes the `<script>` tags and their contents which are not useful for links or words.
* `(?s)<style.*>.*</style>`: removes the `<style>` tags and their contents which are not useful for links or words.
* `(?s)<!--.*-->`: removes HTML comments which are not useful for links or words.
* `\"(https?://[^\"]+)`: this extracts links from the HTML document.
* `[ \t\x0B\f]+`: this is used to match multiple whitespace characters which is useful to ensure that we are able to split a string into words (non-whitespace characters).
* `</?[^>]+>`: this removes HTML tags completly - which is useful when you are ready to only extract useful *information* (e.g., words) from an HTML document.

The general approach is to eliminate HTML tags that are not useful at all first. For links, you next want to extract the links from the link tags. For words, you want to eliminate all HTML tags and then extract actual words. You will find both the [findAllIn] and [replaceAllIn] methods useful on regular expressions - we used both of these in addition to the `split` method on `String`s to split a string of words separated by whitespace into an array/list of words. You may find [this blog post][regexpost] useful.

We recommend that you implement each of the above as smaller functions for each regular expression and use those functions to build up larger functionality. We should note, that using regular expressions for parsing HTML is [not the way you typically want to do this]. At the same time it shows us how well you understand regular expressions and how to use them - and most certainly their limitations which you dig deeper into in a compiler course. In any case - it is a very useful exercise!

You are welcome to search the web for ways of parsing out HTML. That research is most certainly useful as practive and prepartion for software engineering courses such as CMPSCI 320.

[not the way you typically want to do this]: http://stackoverflow.com/questions/1732348/regex-match-open-tags-except-xhtml-self-contained-tags

[findAllIn]: http://www.scala-lang.org/api/2.11.4/index.html#scala.util.matching.Regex@findAllMatchIn(source:CharSequence):Iterator[scala.util.matching.Regex.Match]
[replaceAllIn]: http://www.scala-lang.org/api/2.11.4/index.html#scala.util.matching.Regex@replaceAllIn(target:CharSequence,replacement:String):String
[scalaj]: https://github.com/scalaj/scalaj-http
[regexpost]: http://daily-scala.blogspot.com/2010/01/regular-expression-1-basics-and.html

### IndexActor

The `IndexActor` is responsible for indexing words with respect to the documents they originate from. It communicates only with the `ParseQueueActor` waiting for messages for checking links, pages, as well as indexing words. It uses a relational database (H2) to store/query the data retrieved from the web. It is responsible for handling the following messages:

* `CheckPage(url, html)`: this message is received from the `ParseQueueActor` to determine if we have already visited this `url`. You will need to check the database to determine if the document already exists (url). If it does, not response is necessary. If we haven't visited this URL before we respond with a `ParsePage` message back to the `ParseQueueActor` - this will intern queue it up to be parsed by the `ParseActor`s.
* `CheckLink(url)`: this message is received from the `ParseQueueActor` to determine if we have already visited this `url`. If we have no response is necessary. If not, then we should respond with a `QueueLink` message - which is then forwarded by the `ParseQueueActor` to the `LinkQueueActor` to the queue the URL to be fetched from the web.
* `Word(url, word)`: this message is received from the `ParseQueueActor` indicating that we need to index this word in the database. You should query the database to find the document ID and word ID and insert it into the `index` table.

We have provided startup code for working with the database. In particular, we include the necessary database connection code as well as queries to drop/create tables if the do not exist. You will not need to modify this code. A call to the `createDatabase` function occurs when the actor is constructed. You will note that the tables are destroyed each time the application is executed. This simplifies the debugging process as it starts the application with a fresh database each time we run.

The schema is simple and consists of the following tables:

* `words`: this stores a `wordid` and `word` for each word found in an HTML document fetched from the web.
* `documents`: this stores a `docid` and `url` for each HTML document retrieved from the web.
* `index`: this associates a `wordid` and `docid` representing the inverted index. Unlike a complete inverted index we do not store the line and position of the word in the document. Our inverted index only knows that the word was found in the document - not what position.

We suggest creating small functions that perform the basic queries for checking if a word exists, if a document exists, if a word has already been indexed with an associated document. You will more easily be able to test your interaction with the database by running these functions from the scala repl.

## Your Task

Your task is to complete the implementation of the inverted indexer. You should focus on implementing each actor in isolation. As demonstrated you can instantiate a single actor and send it messages from the scala repl to see if it is responding to your messages correctly. In most cases you will need to provide an actor to the constructor. You should do this as you play around with the code.

You should implement as much as possible as functions that you can test from the Scala repl. This will make it easier to test before adding messages into the mix.

You should also try to test the actors with empty implementations to ensure that messages are being sent and received appropriately. Once you are convinced that your actor system is "hooked up" appropriately you can start to add in actual work (i.e., fetch a web page from the web, insert words into the database).

## Task 4: Submit Your Work

You should make frequent git adds, commits, and pushes as you do your
work. You should make sure that your final implementation has been
pushed to the remote git server by the assigned due date.
