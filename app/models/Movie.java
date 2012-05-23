package models;

import java.util.Date;

import models.helper.TmdbMovie;

public class Movie{

	public String id;

	public String url;
	public String title;
	public Integer year;
	public String channel;
	public Date start;
	public Date end;

	public ImdbApiMovie imdb;

	public TmdbMovie tmdb;


	public int getAssembledPercentRating() {
		int rating = -1;
		if (imdb != null && imdb.rating != null) {
			rating = imdb.getPercentRating();
		} else if (tmdb != null && tmdb.rating != null) {
			rating = tmdb.getPercentRating();
		}
		return rating;
	}

	@Override
	public String toString() {
		return String.format("[%s] at %s", title, url);
	}
}
