package services

import play.api.{Configuration, Environment}

object PlayUtil {

  def config(property:String): String = {
    configOpt(property).getOrElse(throw new RuntimeException(s"Missing configuration: $property"))
  }

  def configOpt(property:String): Option[String] = {
    // TODO not sure this is correct for production use
    val configuration = Configuration.load(Environment.simple())
    configuration.getOptional[String](property).map{
      case "null" => throw new RuntimeException(property + " = \"null\"")
      case v => v
    }
  }

  def configExists(property: String): Boolean = configOpt(property).isDefined

}
