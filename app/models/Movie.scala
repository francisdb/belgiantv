package models

import scala.collection.JavaConversions._
import play.api.Play.current
import play.modules.mongodb.jackson.MongoDB
import net.vz.mongodb.jackson.{Id, ObjectId}
import org.codehaus.jackson.annotate.JsonProperty
import reflect.BeanProperty
import com.mongodb.DBObject
import play.api.Logger


class Movie(
    @ObjectId @Id val id: String, 
    @BeanProperty @JsonProperty("name") val name: String,
    @BeanProperty @JsonProperty("imdbId") val imdbId: String,
    @BeanProperty @JsonProperty("imdbRating") val imdbRating: String,
    @BeanProperty @JsonProperty("year") val year: Int,
    @BeanProperty @JsonProperty("imgUrl") val imgUrl: String) {
  @ObjectId
  @Id
  def getId = id;
  
  def imdbUrl() = "http://www.imdb.com/title/" + imdbId
}

object Movie {
  private val logger = Logger("application.db")
  
  private lazy val db = MongoDB.collection("movies", classOf[Movie], classOf[String])

  def create(movie: Movie) = {
    val result = db.save(movie);
    val id = result.getSavedId();
    logger.debug("saved " + movie + " with id " + id)
    db.findOneById(id)
  }
  
  def createIfDoesNotExist(movie: Movie) = {
    val fromDB = Movie.findByNameAndYear(movie.name, movie.year)
    fromDB.getOrElse{
      Movie.create(movie)
      movie
    }
  }
  
  def findAll() = { db.find().toArray.toList }

  def findById(id: String) = Option(db.findOneById(id))
  
  def findByName(name: String) = db.find().is("name", name).headOption
  
  def findByImdbId(imdbId: String) = db.find().is("imdbId", imdbId).headOption
  
  def findByNameAndYear(name: String, year:Int) = {
    val movie = db.find().is("name", name).is("year", year).headOption
    val result = movie.orElse(findByName(name))
    logger.debug("Search for %s %s gives %s".format(name, year, result))
    result
  }
}