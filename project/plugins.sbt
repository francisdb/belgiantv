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

// advanced stylesheets
// addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")

// FIXME remove once this is fixed: https://github.com/sbt/sbt-js-engine/issues/56
//https://stackoverflow.com/questions/18065982/how-to-override-the-dependency-of-an-sbt-plugin
//conflictManager := ConflictManager.strict
//libraryDependencies ++= Seq(
//  //"io.apigee.trireme" % "trireme-jar" % "0.8.8" force(), // 0.8.9
//  //"org.mozilla" % "rhino" % "1.7.7.1",
//  //"rhino" % "rhino" % "1.5R4.1" force()
//  "com.typesafe.sbt" % "sbt-js-engine" % "1.1.2" force()
//)


// web asset pipeline
// normally not needed but from
// https://github.com/sbt/sbt-js-engine/issues/56#issuecomment-322953837
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.1")

// there is a compatibility issue with sbt-less and the latest sbt-js-engine - downgrade version until fixed
// https://github.com/sbt/sbt-less/issues/95
dependencyOverrides += "com.typesafe.sbt" % "sbt-js-engine" % "1.1.4"