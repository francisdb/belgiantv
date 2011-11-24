package models;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import play.Logger;
import services.TimeUtil;
import siena.Generator;
import siena.Id;
import siena.Index;
import siena.Model;
import siena.Query;
import siena.SienaException;

public class Movie extends Model{
	
	@Id(Generator.UUID)
	public String id;

	public String url;
	public String title;
	public Integer year;
	public String channel;
	public Date start;
	public Date end;
	
	@Index("imdb_index")
	public ImdbApiMovie imdb;
	
	@Index("tmdb_index")
	public TmdbMovie tmdb;
	
	public static Query<Movie> all() {
        return Model.all(Movie.class);
    }
	
    public static Movie findByUrl(String url) {
        return all().filter("url", url).get();
    }
    

	public static List<Movie> findWithoutImdb() {
		return all().filter("imdb", null).fetch();
	}
	
	public static List<Movie> findWithoutTmdb() {
		return all().filter("tmdb", null).fetch();
	}
    
    public static List<Movie> findByDate(Date date) {
    	Calendar start = Calendar.getInstance();
    	start.setTime(date);
    	TimeUtil.midnight(start);
    	Calendar end = Calendar.getInstance();
    	end.setTime(date);
    	end.add(Calendar.DAY_OF_MONTH, 1);
    	TimeUtil.midnight(end);
    	List<Movie> movies = all().filter("start>", start.getTime()).filter("start<", end.getTime())
    			.order("-start").fetch();
    	// TODO find cleaner way?
    	for(Movie movie:movies){
    		if(movie.imdb != null){
    			try{
    				movie.imdb.get();
    			}catch(SienaException ex){
    				Logger.warn(ex.getMessage());
    			}
    		}
    	}
    	// TODO find cleaner way?
    	for(Movie movie:movies){
    		if(movie.tmdb != null){
    			try{
    				movie.tmdb.get();
    			}catch(SienaException ex){
    				Logger.warn(ex.getMessage());
    			}
    		}
    	}
    	return movies;
    }

    
    public String toString() {
    	return String.format("[%s] at %s", title, url);
    }
}
