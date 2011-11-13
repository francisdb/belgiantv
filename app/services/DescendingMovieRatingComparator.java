package services;

import java.util.Comparator;

import models.Movie;

public final class DescendingMovieRatingComparator implements Comparator<Movie> {
	@Override
	public int compare(Movie o1, Movie o2) {
		if(o1.imdb == null || o2.imdb == null){
			return o1.title.compareTo(o2.title);
		}
		return o2.imdb.getPercentRating() - o1.imdb.getPercentRating();
	}
}