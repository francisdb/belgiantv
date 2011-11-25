package models;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import play.Logger;
import siena.Generator;
import siena.Id;
import siena.Index;
import siena.Model;
import siena.Query;
import siena.SienaException;

public class Movie extends Model {

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

	public static List<Movie> findByDate(DateTime date) {
		DateTime start = date.withTime(0, 0, 0, 0);
		DateTime end = start.plusDays(1);
		List<Movie> movies = all().filter("start>", start.toDate()).filter("start<", end.toDate()).order("-start").fetch();
		// TODO find cleaner way?
		for (Movie movie : movies) {
			movie.preLoad();
		}
		return movies;
	}

	public static List<Movie> findByWeek(DateTime firstDate) {
		DateTime start = firstDate.withTime(0, 0, 0, 0);
		DateTime end = start.plusDays(8);
		List<Movie> movies = all().filter("start>", start.toDate()).filter("start<", end.toDate()).order("-start").fetch();
		// TODO find cleaner way?
		for (Movie movie : movies) {
			movie.preLoad();
		}
		return movies;
	}

	public int getAssembledPercentRating() {
		int rating = -1;
		if (imdb != null && imdb.rating != null) {
			rating = imdb.getPercentRating();
		} else if (tmdb != null && tmdb.rating != null) {
			rating = tmdb.getPercentRating();
		}
		return rating;
	}

	private void preLoad() {
		if (imdb != null) {
			try {
				imdb.get();
			} catch (SienaException ex) {
				Logger.warn(ex.getMessage());
			}
		}
		if (tmdb != null) {
			try {
				tmdb.get();
			} catch (SienaException ex) {
				Logger.warn(ex.getMessage());
			}
		}
	}

	public String toString() {
		return String.format("[%s] at %s", title, url);
	}
}
