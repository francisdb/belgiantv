package models.helper

import org.joda.time.format.DateTimeFormat

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
	rating:String,
	certification:String,
	overview:String,
	released:String,

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

  def getFirstPoster = {
    if (posters != null && posters.nonEmpty) {
      posters.head.image.url
    } else {
      null
    }
  }
    
    def getYear = {
    	if(released == null){
    		null
    	}else{
    		val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
			try{
				fmt.parseDateTime(released).getYear
			}catch{
			    case ex:IllegalArgumentException => null
			}
    	}
	}
    
	def getPercentRating() = {
		if(rating == null){
			-1
		}else{
		  try { rating.toDouble } catch { 
		    case e:NumberFormatException => -1
		    // Logger.warn(e.getMessage())
		  }
		}
	}
	
	def getDecimalRating() = {
		getPercentRating() / 10
	}
  
  
  
}