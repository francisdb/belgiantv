package services

import play.api.Play

object PlayUtil {

  def config(property:String) = {
    Play.current.configuration.getString(property)
      .map(_ match { case "null" => throw new RuntimeException(property + " = \"null\"") case v => v})
      .getOrElse(throw new RuntimeException("Missing configuration: " + property))
  }

  def configExists(property: String) = Play.current.configuration.getString(property).isDefined

}
