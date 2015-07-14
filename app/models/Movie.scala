package models

// Reactive Mongo imports
import reactivemongo.api._
import reactivemongo.bson._

import scala.util.Try

// Reactive Mongo plugin
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.BSONFormats._

import play.api.Logger
import controllers.Application
import concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json._

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