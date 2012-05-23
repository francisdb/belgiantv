package models.helper;

import java.util.List;

import javax.persistence.Embedded;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import play.Logger;

import com.google.gson.annotations.SerializedName;

public class TmdbMovie{
	
	public long id;
	
	@SerializedName("imdb_id")
	public String imdbId;
	public String name;
	public String url;
	public int popularity;
	public boolean translated;
	public boolean adult;
	public String language;
	@SerializedName("original_name")
	public String originalName;
	@SerializedName("alternative_name")
	public String alternativeName;
	@SerializedName("movie_type")
	public String movieType;
	
	public int votes;
	public String rating;
	public String certification;
	public String overview;
	public String released;

	public int version;
	@SerializedName("last_modified_at")
	public String lastModifiedAt;
	
	@Embedded
	public List<TmdbPoster> posters;
	
	@Embedded
	public List<TmdbBackDrop> backdrops;
    
    public Integer getYear() {
    	if(released == null){
    		return null;
    	}
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		try{
			return fmt.parseDateTime(released).getYear();
		}catch(IllegalArgumentException ex){
			return null;
		}
	}
    
	public int getPercentRating(){
		if(rating == null){
			return -1;
		}
		try{
			return (int)(Double.parseDouble(rating) * 10);
		} catch (NumberFormatException e) {
			Logger.warn(e.getMessage());
		}
		return -1;
	}
	
	public int getDecimalRating(){
		return getPercentRating() / 10;
	}
	
	@Override
	public String toString() {
		return id + " " + name;
	}
}
