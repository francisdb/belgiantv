package models.helper

import models.Movie

case class BroadcastInfo(broadcast:models.Broadcast, movie:Option[Movie]){
  /**
    *
    * @return the average rating in range [0-100]
    */
  def rating:Option[Int] = {
    val imdbRatingOption = movie.flatMap(m => m.rating.map( rat => rat * 10))
    val tomatoesRatingOption = broadcast.tomatoesRating.map(_.toDouble)
    val tmdbRatingOption = broadcast.tmdbRating.map( rat => rat.toDouble * 10)
    val traktRating = broadcast.traktRating.map( rat => rat * 10)

    // TODO clean up with Seq + avg

    // imdb and tomatoes count more
    val ratings = List(
      imdbRatingOption,
      imdbRatingOption,
      tomatoesRatingOption,
      tomatoesRatingOption,
      tmdbRatingOption,
      traktRating
    ).flatten

    ratings match {
      case Nil => None
      case list => Some(BroadcastInfo.average(list).toInt)
    }
  }
}

object BroadcastInfo{
  
  def scoreSorter = { (i:BroadcastInfo, i2:BroadcastInfo) =>
    i.rating.getOrElse(0) > i2.rating.getOrElse(0)
  }

  private def average[T]( ts: Iterable[T] )( implicit num: Numeric[T] ) = {
    num.toDouble( ts.sum ) / ts.size
  }

}
