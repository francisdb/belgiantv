package models.helper

import org.joda.time.format.DateTimeFormat

import scala.util.Try

case class TmdbMovie(
    
	id:Long,

  imdb_id:String,
	
	name:String,
	url:String,
	popularity:Int,
	translated:Boolean,
	adult:Boolean,
	language:String,
  original_name:String,
  alternative_name:String,
  movie_type:String,
	
	votes:Int,
	rating:Option[String],
	certification:String,
	overview:String,
	released: Option[String],

	version:Int,
  last_modified_at:String,
	
	posters:List[TmdbPoster],
	
	backdrops:List[TmdbBackDrop]
) {

  val imdbId = imdb_id
  val originalName = original_name
  val alternativeName = alternative_name
  val movieType = movie_type
  val lastModifiedAt = last_modified_at

  def getFirstPoster = posters.headOption.map(_.image.url)

  def getYear = {
    released.flatMap { rel =>
      val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
      Try(fmt.parseDateTime(rel).getYear).toOption
    }
  }
    
	def getPercentRating = {
    rating.flatMap(rat => Try(rat.toDouble).toOption)
	}
	
	def getDecimalRating = getPercentRating.map(_ / 10)

}