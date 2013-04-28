package models

// Reactive Mongo imports
import reactivemongo.api._
import reactivemongo.bson._

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
  imdbRating: String,
  year: Int,
  imgUrl: String
){

  def imdbUrl() = "http://www.imdb.com/title/" + imdbId
  
  def rating() = {
    Movie.ratingToDouble(imdbRating)
  }
}

object Movie extends MongoSupport{

  protected override val logger = Logger("application.db")

  private lazy val movieCollection = Application.db.collection[JSONCollection]("movies")

  implicit val movieFormat = Json.format[Movie]

  def create(movie: Movie) = {
    val id = BSONObjectID.generate
    val withId = movie.copy(id = Some(id))
    movieCollection.insert(withId)
      .onComplete(le => mongoLogger(le, "saved movie " + withId + " with id " + id))
    withId
  }


  def find(name:String, year:Option[Int] = None) = {
    year match {
      case None => findByName(name)
      case Some(y) => findByNameAndYear(name, y)
    }
  }

  def findByName(name: String): Future[Option[Movie]] = {
    movieCollection.find(
      Json.obj("name" -> name)
    ).cursor[Movie].headOption
  }

  def findByImdbId(imdbId: String): Future[Option[Movie]] = {
    movieCollection.find(
      Json.obj("imdbId" -> imdbId)
    ).cursor[Movie].headOption
  }

  def findByNameAndYear(name: String, year:Int): Future[Option[Movie]] = {
    movieCollection.find(
      Json.obj("name" -> name, "year" -> year)
    ).cursor[Movie].headOption
  }

  private def ratingToDouble(rating:String) = try{
    rating.toDouble
  } catch {
     case e : NumberFormatException => 0.0
  }
}