package services

import play.api.{Configuration, Environment}

object PlayUtil {

  def config(property:String) = {
    configOpt(property).getOrElse(throw new RuntimeException("Missing configuration: " + property))
  }

  def configOpt(property:String) = {
    // TODO not sure this is correct for production use
    val configuration = Configuration.load(Environment.simple())
    configuration.getString(property).map{
      case "null" => throw new RuntimeException(property + " = \"null\"")
      case v => v
    }
  }

  def configExists(property: String) = configOpt(property).isDefined

}
