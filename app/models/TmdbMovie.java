package models;

import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import play.Logger;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.embed.Embedded;

import com.google.gson.annotations.SerializedName;

public class TmdbMovie extends Model{
	
	@Id(Generator.NONE)
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
	
	public static Query<TmdbMovie> all() {
        return Model.all(TmdbMovie.class);
    }
	
    public static TmdbMovie findById(String id) {
        return all().filter("id", id).get();
    }
 
    public static TmdbMovie findByTitle(String title) {
        return all().filter("name", title).get();
    }
    
    public static TmdbMovie findByTitleAndYear(String title, Integer year) {
    	// TODO implement year search
        return all().filter("name", title).get();
    }
    
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
