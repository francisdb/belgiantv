package models;

import play.Logger;

import com.google.gson.annotations.SerializedName;

public class ImdbApiMovie{
	
	@SerializedName("imdbID")
	public String id;
	
	@SerializedName("Title")
	public String title;
	
	@SerializedName("imdbRating")
	public String rating;
	
	@SerializedName("imdbVotes")
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
	public Integer year;
	
	@SerializedName("Released")
	public String released;
	
	@SerializedName("Director")
	public String director;
	
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
	
	@Override
	public String toString() {
		return id + " " + title;
	}

}
