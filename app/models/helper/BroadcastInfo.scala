package models.helper

import models.Movie

case class BroadcastInfo(broadcast:models.Broadcast, movie:Option[Movie])