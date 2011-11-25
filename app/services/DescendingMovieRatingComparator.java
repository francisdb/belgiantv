package services;

import java.util.Comparator;

import models.Movie;

public final class DescendingMovieRatingComparator implements Comparator<Movie> {
	@Override
	public int compare(Movie o1, Movie o2) {
		return o2.getAssembledPercentRating() - o1.getAssembledPercentRating();
	}
}