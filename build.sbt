import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayScala
import play.sbt.PlayImport._
import play.sbt.routes.RoutesKeys._

name := "finance"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  ws,
  guice,
  "mysql" % "mysql-connector-java" % "5.1.38",
  "com.typesafe" % "config" % "1.3.2",
  "net.sourceforge.htmlunit" % "htmlunit" % "2.27",
  "junit" % "junit" % "4.12" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test",
  "org.scalamock" %% "scalamock-core" % "3.6.0" % "test",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
  "com.github.nscala-time" %% "nscala-time" % "2.16.0",
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3",
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.hamcrest" % "hamcrest-all" % "1.3",
  "com.h2database" % "h2" % "1.4.192"
)

val circeVersion = "0.9.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics",
  "io.circe" %% "circe-literal"
).map(_ % circeVersion)

resolvers += ("scalaz-bintray" at "http://dl.bintray.com/scalaz/releases").withAllowInsecureProtocol(true)

// Play routers expect their actions to be injected
routesGenerator := InjectedRoutesGenerator

routesImport += "utils.Binders._"

fork in run := false

// Enable cached dependency resolution
updateOptions := updateOptions.value.withCachedResolution(true)