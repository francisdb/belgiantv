package models;

import play.Logger;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

import com.google.gson.annotations.SerializedName;

public class ImdbApiMovie extends Model{
	
	@Id(Generator.NONE)
	@SerializedName("ID")
	public String id;
	
	@SerializedName("Title")
	public String title;
	
	@SerializedName("Rating")
	public String rating;
	
	@SerializedName("Votes")
	public String votes;
	
	@SerializedName("Runtime")
	public String runtime;
	
	@SerializedName("Poster")
	public String poster;
	
	@SerializedName("Genre")
	public String genre;
	
	@SerializedName("Plot")
	public String plot;
	
	@SerializedName("Year")
	public String year;
	
	@SerializedName("Released")
	public String released;
	
	static Query<ImdbApiMovie> all() {
        return Model.all(ImdbApiMovie.class);
    }
	
    public static ImdbApiMovie findById(String id) {
        return all().filter("id", id).get();
    }
 
    public static ImdbApiMovie findByTitle(String title) {
        return all().filter("title", title).get();
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
	
	public String getUrl(){
		return "http://www.imdb.com/title/" + id;
	}

}
