name := "belgiantv"
version := "1.0-SNAPSHOT"
scalaVersion := "2.11.8"

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

val reactiveMongoVersion = "0.12.0"
val reactiveMongoPluginVersion = "0.12.0"
val akkaVersion = "2.4.11" // TODO update when migrating to newer play
val specs2Version = "3.8.5"

libraryDependencies ++= Seq(
  // Add your project dependencies here,
  //jdbc,
  ws,
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  "org.jsoup" % "jsoup" % "1.9.1",
  "commons-lang" % "commons-lang" % "2.6",
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVersion,
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoPluginVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "org.webjars" %% "webjars-play" % "2.5.0-3",
  "org.webjars" % "bootstrap" % "3.3.5",
  "org.webjars" % "jquery" % "2.1.4",

  // play comes with 3.6 that depends on unavailable scalaz-streams
  specs2 % Test,
  "org.specs2" %% "specs2-core" % specs2Version % Test,
  "org.specs2" %% "specs2-mock" % specs2Version % Test,
  "org.specs2" %% "specs2-junit" % specs2Version % Test
)

// for specs2, see https://etorreborre.github.io/specs2/website/SPECS2-3.7.2/quickstart.html
scalacOptions in Test ++= Seq("-Yrangepos")


lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    routesGenerator := InjectedRoutesGenerator
  )
