package models

import reactivemongo.bson._
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler

import play.api.Logger
import controllers.Application
import concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

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

  private lazy val movieCollection = Application.db("movies")

  implicit object MovieBSONReader extends BSONReader[Movie] {
    def fromBSON(document: BSONDocument) :Movie = {
      val doc = document.toTraversable
      Movie(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[BSONString]("name").get.value,
        doc.getAs[BSONString]("imdbId").get.value,
        doc.getAs[BSONString]("imdbRating").get.value,
        doc.getAs[BSONInteger]("year").get.value,
        doc.getAs[BSONString]("imgUrl").get.value
      )
    }
  }

  implicit object MovieBSONWriter extends BSONWriter[Movie] {
    def toBSON(movie: Movie) = {
      BSONDocument(
        "_id" -> movie.id.getOrElse(BSONObjectID.generate),
        "name" -> BSONString(movie.name),
        "imdbId" -> BSONString(movie.imdbId),
        "imdbRating" -> BSONString(movie.imdbRating),
        "year" -> BSONInteger(movie.year),
        "imgUrl" -> BSONString(movie.imgUrl)
      )
    }
  }
  
//  private lazy val db = MongoDB.collection("movies", classOf[Movie], classOf[String])

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
    movieCollection.find(BSONDocument("name" -> BSONString(name))).headOption()
  }

  def findByImdbId(imdbId: String): Future[Option[Movie]] = {
    movieCollection.find(BSONDocument("imdbId" -> BSONString(imdbId))).headOption()
  }

  def findByNameAndYear(name: String, year:Int): Future[Option[Movie]] = {
    movieCollection.find(
      BSONDocument("name" -> BSONString(name), "year" -> BSONInteger(year))).headOption()
  }

  private def ratingToDouble(rating:String) = try{
    rating.toDouble
  } catch {
     case e : NumberFormatException => 0.0
  }
}