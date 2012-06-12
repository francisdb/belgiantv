package models.helper

import models.Broadcast
import models.Movie

case class BroadcastInfo(broadcast:Broadcast, movie:Option[Movie])