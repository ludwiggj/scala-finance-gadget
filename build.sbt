name := "finance"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.filippodeluca.ssoup" %% "ssoup" % "1.0-SNAPSHOT"

libraryDependencies += "com.typesafe" % "config" % "1.0.2"

libraryDependencies += "net.sourceforge.htmlunit" % "htmlunit" % "2.14"

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.2.1" % "test"

libraryDependencies += "org.scalamock" %% "scalamock-core" % "3.2.1" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "1.2.0"

libraryDependencies += "com.typesafe.slick" %% "slick" % "2.1.0"

libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "2.1.0"

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.6.4"

libraryDependencies += "mysql" % "mysql-connector-java" % "latest.release"

libraryDependencies += "com.dbdeploy" % "dbdeploy-cli" % "3.0M3"

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value