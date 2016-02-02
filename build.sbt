name := "asn-inverted-index"

organization := "cs220"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalaj" %% "scalaj-http" % "1.1.4",
  "org.scalikejdbc" %% "scalikejdbc"       % "2.2.5",
  "com.h2database" % "h2" % "1.4.186",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9"
)
