package jobs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import models.ImdbApiMovie;
import models.Movie;
import models.Queue;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.WS;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

@Every("1m")
public class ImdbJob extends Job {

	public ImdbJob() {
		Logger.info("Stringing " + this.getClass().getSimpleName());
		List<Queue> queues = Queue.all().fetch();
		for(Queue queue:queues){
			Movie movie = queue.movie;
			ImdbApiMovie imdbMovie = readImdb(movie.title);
			if(imdbMovie.id == null || imdbMovie.id.isEmpty()){
				Logger.warn("Movie id is null for:" + movie.title);
			}else{
				imdbMovie.save();
				movie.imdb = imdbMovie;
				movie.save();
			}
			queue.delete();
		}
	}
	
	private ImdbApiMovie readImdb(final String title){
		if(title==null){
			throw new IllegalArgumentException("NULL title");
		}
		String url = null;
		try {
			url = "http://www.imdbapi.com/?t=" + URLEncoder.encode(title, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		Logger.info(url);
		JsonElement json = WS.url(url).get().getJson();
		
		Gson gson = new Gson();
		ImdbApiMovie imdbMovie = gson.fromJson(json, ImdbApiMovie.class);
		return imdbMovie;
	}
}
