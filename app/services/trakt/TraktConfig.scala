package services.trakt

import play.api.Configuration
import services.PlayUtil
import services.trakt.TraktConfig._

object TraktConfig {
  final val configId     = "trakt.client.id"
  final val configSecret = "trakt.client.secret"
}

class TraktConfig(config: Configuration) {
  lazy val isApiEnabled: Boolean = PlayUtil.configExists(configId)
  lazy val clientId: String      = PlayUtil.config(configId)
  lazy val clientSecret: String  = PlayUtil.config(configSecret)
}
