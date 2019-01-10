package models

import play.api.libs.json._

import reactivemongo.bson._
import reactivemongo.play.json.BSONFormats._

import scala.util.Try


case class Movie(
  id: Option[BSONObjectID],
  name: String,
  imdbId: String,
  imdbRating: Option[String],
  year: Int,
  imgUrl: String
){

  val imdbUrl = "http://www.imdb.com/title/" + imdbId
  
  lazy val rating = imdbRating.flatMap(rat => Try(rat.toDouble).toOption)
}

object Movie{

  implicit val movieFormat = Json.format[Movie]


}
