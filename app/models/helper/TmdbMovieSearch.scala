package models.helper

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class TmdbMovieSearch(
  backdrop_path: Option[String],
  id: Long,
  original_title: String,
  popularity: Double,
  poster_path: String,
  //"1980-05-21"
  release_date: String,
  title: String,
  vote_average:String,
  vote_count:Long,
  adult:Boolean) {

  def posterUrl = {
    // http://help.themoviedb.org/kb/api/configuration
    Option(poster_path).map("http://cf2.imgobject.com/t/p/w154/" + _)
  }
}