name := "belgiantv"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.5"

scalacOptions ++= Seq("-feature")

// copying jvm parameters for testing:
// http://play.lighthouseapp.com/projects/82401/tickets/981-overriding-configuration-for-tests
javaOptions in test ++= List("TMDB_API_KEY", "TOMATOES_API_KEY", "MONGOLAB_URI").map( property =>
  Option(System.getProperty(property)).map{value =>
    "-D" + property + "=" + value
  }).flatten

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"

val reactiveMongoVersion = "0.10.5.0.akka23"

val reactiveMongoPluginVersion = "0.10.5.0.akka23"

//resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  // Add your project dependencies here,
  //jdbc,
  //anorm,
  ws,
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0",
  "org.jsoup" % "jsoup" % "1.8.1",
  "commons-lang" % "commons-lang" % "2.6",
  //"net.vz.mongodb.jackson" %% "play-mongo-jackson-mapper" % "1.0.0"
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.5.1", // TODO get rid of this dependency
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVersion,
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoPluginVersion,
  //"com.foursquare" % "fongo" % "1.0.6" % "test",
  "com.typesafe.akka" %% "akka-contrib" % "2.3.4", // TODO update when migrating to newer play
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "jquery" % "2.1.1"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
