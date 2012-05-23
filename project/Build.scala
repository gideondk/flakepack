import sbt._
import Keys._

object ApplicationBuild extends Build {

  val appName = "FlakePack"
  val appVersion = "0.1"

  override lazy val settings = super.settings ++
    Seq(
      name := "flakepack",
      version := "0.1",
      scalaVersion := "2.9.1",
      resolvers ++= Seq(Resolver.mavenLocal,
        "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")
    )

  val appDependencies = Seq(
    "org.msgpack" %% "msgpack-scala" % "0.6.7-SNAPSHOT",
    "org.msgpack" % "msgpack-rpc" % "0.7.0-SNAPSHOT",
    "log4j" % "log4j" % "1.2.16",
    "org.slf4j" % "slf4j-api" % "1.6.4",
     "org.scalatest" %% "scalatest" % "1.7.2" % "test"
  )

  lazy val root = Project(id = "FlakePack",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      libraryDependencies ++= appDependencies,
      mainClass := Some("Main")
    )
  )
}