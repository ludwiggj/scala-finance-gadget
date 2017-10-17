resolvers += Classpaths.sbtPluginReleases

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.6")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")