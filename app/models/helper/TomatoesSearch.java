package models.helper;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TomatoesSearch {

	//@SerializedName("imdbID")
	public int total;
	
	public List<TomatoesMovie> movies;
	
	public static class TomatoesMovie {
		public int id;
		public String title;
		public String year;
		public String runtime;
		
		public TomatoesRatings ratings;
		
		@SerializedName("alternate_ids")
		public TomatoesAlternateIds alternateIds;
		
		@SerializedName("critics_consensus")
		public String criticsConsensus;
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return id + " " + year + " " + title + " (" + ratings + ")";
		}
	}
	
	public static class TomatoesRatings{
		public String critics_rating;
		public Integer critics_score;
		public String audience_rating;
		public Integer audience_score;
		
		@Override
		public String toString() {
			return "audience " + audience_score;
		}
	}
	
	public static class TomatoesAlternateIds{
		public String imdb;
	}

}
