package controllers;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import models.Movie;

import org.joda.time.DateTime;

import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import services.DescendingMovieRatingComparator;

public class Application extends Controller {

	@Before
	public static void log() {
		Logger.debug("Application call:" + request.url);

		//renderArgs.put("imdbQueueSize", imdbQueueSize);
	}

	public static void index() {
		date(new DateTime());
	}

	public static void date(DateTime date) {
		DateTime actualDate = new DateTime();
		if (date != null) {
			actualDate = date;
		}

		List<Movie> movies = Movie.findByDate(actualDate);
		Collections.sort(movies, new DescendingMovieRatingComparator());
		
		Date before = actualDate.minusDays(1).toDate();
		Date after = actualDate.plusDays(1).toDate();
		Date day = actualDate.toDate();
		render(movies, day, before, after);
	}

	public static void week(DateTime date) {
		DateTime firstDay = new DateTime();
		if (date != null) {
			firstDay = date;
		}
		List<Movie> movies = Movie.findByWeek(firstDay);
		Collections.sort(movies, new DescendingMovieRatingComparator());
		Date day = firstDay.toDate();
		render(movies, day);
	}

}