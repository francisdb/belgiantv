package models.helper

import play.api.Logger

import scala.util.Try

case class ImdbApiMovie(
  imdbID: String,
	Title: String,
	imdbRating: Option[String],
	imdbVotes: String,
	Runtime: String,
	Poster: String,
	Genre: String,
	Plot: String,
	Year: String,
	Released: String,
	Director: String
){

  val id = imdbID
  val title = Title
  val rating = imdbRating
  val votes = imdbVotes
  val runtime = Runtime
  val poster = Poster
  val genre = Genre
  val plot = Plot
  val year = Try(Year.takeWhile(_ != '-').toInt).getOrElse(0) // year can be in format 2013 or 2013–2015
  val released = Released
  val director = Director
	
  lazy val getPercentRating = {
    rating.flatMap{ rat =>
      Try((rat.toDouble * 10).toInt).toOption
    }
	}
	
	lazy val getDecimalRating = {
		getPercentRating.map(_ / 10)
	}
	
	lazy val getUrl = "http://www.imdb.com/title/" + id
}