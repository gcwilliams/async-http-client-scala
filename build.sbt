import sbt.Keys._

lazy val root = (project in file(".")).
  settings(
    organization := "uk.co.gcwilliams",
    name := "AsyncHttpClient",
    version := "1.0",
    scalaVersion := "2.11.7",
    libraryDependencies += "io.netty" % "netty-all" % "4.0.30.Final",
    libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.0-M12" % "test"
  )
