package controllers;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import models.Movie;
import models.Queue;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import services.DescendingMovieRatingComparator;

public class Application extends Controller {

	@Before
	public static void log() {
		Logger.debug("Application call:" + request.url);

		long imdbQueueSize = Queue.all().count();
		renderArgs.put("imdbQueueSize", imdbQueueSize);
	}

	public static void index(Date date) {
		Date day = new Date();
		if (date != null) {
			day = date;
		}

		List<Movie> movies = Movie.findByDate(day);
		Collections.sort(movies, new DescendingMovieRatingComparator());

		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		cal.add(Calendar.DAY_OF_WEEK, -1);
		Date before = cal.getTime();
		cal.setTime(day);
		cal.add(Calendar.DAY_OF_WEEK, +1);
		Date after = cal.getTime();

		render(movies, day, before, after);
	}

}