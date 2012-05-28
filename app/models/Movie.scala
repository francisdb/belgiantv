package models

import play.api.Play.current
import net.vz.mongodb.jackson.{Id, ObjectId}
import org.codehaus.jackson.annotate.JsonProperty
import play.modules.mongodb.jackson.MongoDB
import reflect.BeanProperty

import scala.collection.JavaConversions._


class Movie(
    @ObjectId @Id val id: String, 
    @BeanProperty @JsonProperty("name") val name: String,
    @BeanProperty @JsonProperty("imdbId") val imdbId: String,
    @BeanProperty @JsonProperty("imdbRating") val imdbRating: String) {
  @ObjectId
  @Id
  def getId = id;
}

object Movie {
  private lazy val db = MongoDB.collection("movies", classOf[Movie], classOf[String])

  def create(movie: Movie) { db.save(movie) }
  def findAll() = { db.find().toArray.toList }
}