package models.helper

import org.codehaus.jackson.annotate.JsonProperty
import org.codehaus.jackson.annotate.JsonIgnoreProperties
import org.joda.time.format.DateTimeFormat

@JsonIgnoreProperties(ignoreUnknown = true)
case class TmdbMovie(
    
	id:Long,
	
	@JsonProperty("imdb_id")
	imdbId:String,
	
	name:String,
	url:String,
	popularity:Int,
	translated:Boolean,
	adult:Boolean,
	language:String,
	@JsonProperty("original_name")
	originalName:String,
	@JsonProperty("alternative_name")
	alternativeName:String,
	@JsonProperty("movie_type")
	movieType:String,
	
	votes:Int,
	rating:String,
	certification:String,
	overview:String,
	released:String,

	version:Int,
	@JsonProperty("last_modified_at")
	lastModifiedAt:String,
	
	posters:List[TmdbPoster],
	
	backdrops:List[TmdbBackDrop]
) {

  def getFirstPoster() = {
    if (posters != null && !posters.isEmpty) {
      posters.head.image.url
    } else {
      null
    }
  }
    
    def getYear() = {
    	if(released == null){
    		null
    	}else{
    		val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
			try{
				fmt.parseDateTime(released).getYear();
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
		    case _ => -1 
		    // Logger.warn(e.getMessage())
		  }
		}
	}
	
	def getDecimalRating() = {
		getPercentRating() / 10
	}
  
  
  
}