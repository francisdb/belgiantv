package services

import play.api.Play

object PlayUtil {

  def config(property:String) = {
    configOpt(property).getOrElse(throw new RuntimeException("Missing configuration: " + property))
  }

  def configOpt(property:String) = {
    Play.current.configuration.getString(property)
      .map(_ match {
      case "null" => throw new RuntimeException(property + " = \"null\"")
      case v => v}
    )
  }

  def configExists(property: String) = configOpt(property).isDefined

}
