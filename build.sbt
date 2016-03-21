import play.ebean.sbt.PlayEbean
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.{PlayJava, PlayScala}
import play.sbt.PlayImport._
import play.sbt.routes.RoutesKeys._

name := "finance"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  evolutions,
  "mysql" % "mysql-connector-java" % "latest.release",
  "org.filippodeluca.ssoup" %% "ssoup" % "1.0-SNAPSHOT",
  "com.typesafe" % "config" % "1.0.2",
  "net.sourceforge.htmlunit" % "htmlunit" % "2.20",
  "junit" % "junit" % "4.11" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.1" % "test",
  "org.scalamock" %% "scalamock-core" % "3.2.1" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.4.0" % "test",
  "com.github.nscala-time" %% "nscala-time" % "1.2.0",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.typesafe.slick" %% "slick-codegen" % "2.1.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.hamcrest" % "hamcrest-all" % "1.3"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.

routesGenerator := InjectedRoutesGenerator

routesImport += "utils.Binders._"

lazy val myProject = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

fork in run := false

// Uncomment the following line if working offline
// offline := true