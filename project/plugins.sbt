// Comment to get more information during initialization
logLevel := Level.Warn

// fast dependency download
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.1.0-M9")

// better compiler error messages
// addSbtPlugin("org.duhemm" % "sbt-errors-summary" % "0.6.3")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.4")

//addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

// advanced stylesheets
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")
