package services;

import java.util.Comparator;

import models.Movie2;

public final class DescendingMovieRatingComparator implements Comparator<Movie2> {
	@Override
	public int compare(Movie2 o1, Movie2 o2) {
		return o2.getAssembledPercentRating() - o1.getAssembledPercentRating();
	}
}