package services.tomatoes

import play.api.Configuration
import services.PlayUtil
import services.tomatoes.TomatoesConfig._

object TomatoesConfig {
  final val configKey = "tomatoes.apiKey"
}

class TomatoesConfig(config: Configuration) {
  lazy val isApiEnabled: Boolean = PlayUtil.configExists(configKey)
  lazy val apiKey: String        = PlayUtil.config(configKey)
}
