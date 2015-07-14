name := "belgiantv"
version := "1.0-SNAPSHOT"
scalaVersion := "2.11.6"

scalacOptions ++= Seq("-feature")
// copying jvm parameters for testing:
// http://play.lighthouseapp.com/projects/82401/tickets/981-overriding-configuration-for-tests
javaOptions in test ++= List("TMDB_API_KEY", "TOMATOES_API_KEY", "MONGOLAB_URI").flatMap(property =>
  Option(System.getProperty(property)).map { value =>
    "-D" + property + "=" + value
  }
)

includeFilter in (Assets, LessKeys.less) := "*.less"
excludeFilter in (Assets, LessKeys.less) := "_*.less"

val reactiveMongoVersion = "0.11.2"
val reactiveMongoPluginVersion = "0.11.2.play24"
val akkaVersion = "2.3.11" // TODO update when migrating to newer play

libraryDependencies ++= Seq(
  // Add your project dependencies here,
  //jdbc,
  //anorm,
  ws,
  specs2 % Test,
  "com.typesafe.play" %% "play-mailer" % "3.0.1",
  "org.jsoup" % "jsoup" % "1.8.2",
  "commons-lang" % "commons-lang" % "2.6",
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVersion,
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoPluginVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.5",
  "org.webjars" % "jquery" % "2.1.4"
)

// for specs2 :-(
// see https://github.com/scalaz/scalaz-stream/issues/258#issuecomment-95975632
resolvers += Resolver.bintrayRepo("scalaz", "releases")

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(
  routesGenerator := InjectedRoutesGenerator
)
