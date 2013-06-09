package models.helper

import models.Movie

case class BroadcastInfo(broadcast:models.Broadcast, movie:Option[Movie]){
  def rating:Option[Int] = {
    val imdbRatingOption = movie.map(m => (m.rating() * 10).toInt)
    val tomatoesRatingOption = broadcast.tomatoesRating.map(_.toInt)

    imdbRatingOption.map{ imdb =>
      tomatoesRatingOption.map{ tomatoes =>
        if(tomatoes == 0){
          imdb
        }else{
          (imdb + tomatoes) / 2
        }
      }.getOrElse(
        imdb
      )
    }.orElse(tomatoesRatingOption)
  }
}

object BroadcastInfo{
  
  def scoreSorter = { (i:BroadcastInfo, i2:BroadcastInfo) =>
    i.rating.getOrElse(0) > i2.rating.getOrElse(0)
  }

}