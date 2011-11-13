package controllers;

import java.util.Calendar;
import java.util.List;

import models.ImdbApiMovie;
import models.Movie;
import models.Queue;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import services.ImdbApiService;
import services.YeloReader;
import siena.SienaException;

public class Trigger extends Controller{
	
	private static final int DAYS_TO_FETCH = 7;

	@Before
	public static void log(){
		Logger.info("Trigger call:" + request.url);
	}

	public static void imdb() {
		
		List<Queue> queues = Queue.all().fetch();
		for(Queue queue:queues){
			try{
				queue.movie.get();
				loadMovie( queue.movie);
			}catch(SienaException ex){
				Logger.warn("Movie with id %s not found", queue.movie.id);
			}
			queue.delete();
		}
		
		renderText("IMDB fetching done for " + queues.size());
	}

	private static void loadMovie(Movie movie) {
		ImdbApiService service = new ImdbApiService();
		if(movie.title != null){
			ImdbApiMovie imdbMovie = service.readImdb(movie.title, null);
			if(imdbMovie.id == null || imdbMovie.id.isEmpty()){
				Logger.warn("Movie id is null for: %s", movie.title);
			}else{
				imdbMovie.save();
				movie.imdb = imdbMovie;
				movie.save();
			}
		}else{
			Logger.warn("No title for movie %s", movie);
		}
	}

	public static void yelo() {
		YeloReader reader = new YeloReader();
		Calendar cal = Calendar.getInstance();
		for(int i = 0;i < DAYS_TO_FETCH;i++){
			reader.read(cal.getTime());
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		renderText("Yelo fetching done for next % days", DAYS_TO_FETCH);
	}

}
