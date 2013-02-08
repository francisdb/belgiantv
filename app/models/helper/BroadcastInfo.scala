package models.helper

import models.Movie

case class BroadcastInfo(broadcast:models.Broadcast, movie:Option[Movie])

object BroadcastInfo{
  
  def scoreSorter = { (i:BroadcastInfo,i2:BroadcastInfo) =>
    i.movie.map(m => m.rating).getOrElse(0.0) > i2.movie.map(m => m.rating).getOrElse(0.0)
  }

}