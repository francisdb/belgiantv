import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "belgiantv"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "com.google.code.gson" % "gson" % "2.2.1",
      "org.jsoup" % "jsoup" % "1.6.2"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
