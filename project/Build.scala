import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "belgiantv"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "org.jsoup" % "jsoup" % "1.7.1",
      "commons-lang" % "commons-lang" % "2.6",
      "net.vz.mongodb.jackson" %% "play-mongo-jackson-mapper" % "1.0.0"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
