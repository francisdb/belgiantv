package controllers;

import java.util.Calendar;
import java.util.List;

import models.ImdbApiMovie;
import models.Movie;
import models.TmdbMovie;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import services.ImdbApiService;
import services.TmdbApiService;
import services.YeloReader;

public class Task extends Controller{
	
	
	private static final int DAYS_TO_FETCH = 8;
	
	@Before
	public static void log(){
		Logger.info("Task call:" + request.url);
	}
	
	public static void imdb() {
		int found = 0;
		List<Movie> moviesWithoutImdb = Movie.findWithoutImdb();
		for(Movie movie:moviesWithoutImdb){
			ImdbApiMovie imdbApiMovie = loadMovie(movie);
			if(imdbApiMovie != null){
				found++;
			}
		}
		Logger.info("TMDB fetching done, found %s of %s" , found, moviesWithoutImdb.size());
		ok();
	}
	
	public static void tmdb() {
		int found = 0;
		List<Movie> moviesWithoutTmdb = Movie.findWithoutTmdb();
		for(Movie movie:moviesWithoutTmdb){
			TmdbMovie tmdbMovie = loadMovieTmdb(movie);
			if(tmdbMovie != null){
				found++;
			}else{
				
			}
		}
		Logger.info("TMDB fetching done, found %s of %s" , found, moviesWithoutTmdb.size());
		ok();
	}

	public static void yelo() {
		YeloReader reader = new YeloReader();
		Calendar cal = Calendar.getInstance();
		for(int i = 0;i < DAYS_TO_FETCH;i++){
			reader.read(cal.getTime());
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		Logger.info("Yelo fetching done for next %s days", DAYS_TO_FETCH);
		ok();
	}

	public static void clear() {
		Movie.all().delete();
		ImdbApiMovie.all().delete();
		TmdbMovie.all().delete();
		Logger.info("Database cleared");
		ok();
	}
	
	private static ImdbApiMovie loadMovie(Movie movie) {
		ImdbApiMovie imdbMovie = null;
		ImdbApiService service = new ImdbApiService();
		if(movie.title != null){
			imdbMovie = service.findOrRead(movie.title, movie.year);
			if(imdbMovie == null || imdbMovie.id == null || imdbMovie.id.isEmpty()){
				Logger.warn("IMDB Movie is null, or it's id is null for: %s", movie.title);
			}else{
				movie.imdb = imdbMovie;
				movie.save();
			}
		}else{
			Logger.warn("No title for movie %s", movie);
		}
		return imdbMovie;
	}
	
	private static TmdbMovie loadMovieTmdb(Movie movie) {
		TmdbMovie tmdbMovie = null;
		TmdbApiService service = new TmdbApiService();
		if(movie.title != null){
			tmdbMovie = service.findOrRead(movie.title, movie.year);
			if(tmdbMovie == null){
				Logger.warn("TMDB Movie is null, or it's id is null for: %s", movie.title);
			}else{
				movie.tmdb = tmdbMovie;
				movie.save();
			}
		}else{
			Logger.warn("No title for movie %s", movie);
		}
		return tmdbMovie;
	}
}
