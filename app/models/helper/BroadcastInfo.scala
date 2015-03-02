package models.helper

import com.sun.org.apache.xml.internal.security.c14n.implementations.Canonicalizer20010315ExclOmitComments
import models.Movie

case class BroadcastInfo(broadcast:models.Broadcast, movie:Option[Movie]){
  def rating:Option[Int] = {
    val imdbRatingOption = movie.flatMap(m => m.rating.map( rat => (rat * 10).toInt))
    val tomatoesRatingOption = broadcast.tomatoesRating.map(_.toInt)
    val tmdbRatingOption = broadcast.tmdbRating.map( rat => (rat.toDouble * 10).toInt)

    // TODO clean up with Seq + avg

    // imdb and tomatoes count more
    val ratings = List(
      imdbRatingOption, imdbRatingOption,
      tomatoesRatingOption, tomatoesRatingOption,
      tmdbRatingOption
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

  def average[T]( ts: Iterable[T] )( implicit num: Numeric[T] ) = {
    num.toDouble( ts.sum ) / ts.size
  }

}