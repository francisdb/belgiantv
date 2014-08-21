package util

import models.helper.BroadcastInfo

object Css {
  implicit class MovieInfoExt(info: BroadcastInfo) {
    def progressBarStyle: Option[String] = {
      info.rating.flatMap{
        case i if i >= 70 => Some("progress-bar-success")
        case i if i >= 60 => Some("progress-bar-warning")
        case i => Some("progress-bar-danger")
      }
    }
  }
}
