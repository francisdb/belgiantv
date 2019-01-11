package services

import TomatoesConfig._
import play.api.Configuration

object TomatoesConfig{
  final val configKey = "tomatoes.apiKey"
}

class TomatoesConfig(config: Configuration) {
  lazy val isApiEnabled: Boolean = PlayUtil.configExists(configKey)
  lazy val apiKey: String = PlayUtil.config(configKey)
}
