package models

import play.api.Logger
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future


class MovieRepository(reactiveMongoApi: ReactiveMongoApi) extends MongoSupport {
  // TODO inject MongoSupport to get rid of the mailer dependency?

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  protected override val logger = Logger("application.db")

  private lazy val dbFuture = reactiveMongoApi.connection.database(dbName, herokuMLabFailover)
  private lazy val movieCollectionFuture = dbFuture.map(_.collection[JSONCollection]("movies"))

  //private lazy val movieCollection:JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("movies")


  def create(movie: Movie) = movieCollectionFuture.flatMap{ movieCollection =>
    val id = BSONObjectID.generate
    val withId = movie.copy(id = Some(id))
    val insert = movieCollection.insert(withId)
    insert.onComplete(le => mongoLogger(le, s"saved movie $withId with id $id"))
    insert.map(_ => withId)
  }


  def find(name:String, year:Option[Int] = None) = movieCollectionFuture.flatMap{ movieCollection =>
    year match {
      case None => findByName(name)
      case Some(y) => findByNameAndYear(name, y)
    }
  }

  def findByName(name: String): Future[Option[Movie]] = movieCollectionFuture.flatMap{ movieCollection =>
    movieCollection.find(
      Json.obj("name" -> name)
    ).cursor[Movie]().headOption
  }

  def findByImdbId(imdbId: String): Future[Option[Movie]] = movieCollectionFuture.flatMap{ movieCollection =>
    movieCollection.find(
      Json.obj("imdbId" -> imdbId)
    ).cursor[Movie]().headOption

  }

  def findByNameAndYear(name: String, year:Int): Future[Option[Movie]] = movieCollectionFuture.flatMap{ movieCollection =>
    movieCollection.find(
      Json.obj("name" -> name, "year" -> year)
    ).cursor[Movie]().headOption
  }

  private def ratingToDouble(rating:String) = try{
    rating.toDouble
  } catch {
    case e : NumberFormatException => 0.0
  }

}
