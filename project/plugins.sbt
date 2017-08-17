// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// fast dependency download
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC10")

// better compiler error messages
addSbtPlugin("org.duhemm" % "sbt-errors-summary" % "0.5.0")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.3")

//addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.0")

// advanced stylesheets (1.1.1 is currently broken)
// https://github.com/sbt/sbt-js-engine/issues/56#issuecomment-322953837
//addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")

// there is a compatibility issue with sbt-less and the latest sbt-js-engine - downgrade version until fixed
// https://github.com/sbt/sbt-less/issues/95
dependencyOverrides += "com.typesafe.sbt" % "sbt-js-engine" % "1.1.4"